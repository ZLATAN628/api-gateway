package com.ycx.core;

import com.alibaba.fastjson.JSON;
import com.ycx.common.config.DynamicConfigManager;
import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;
import com.ycx.common.utils.NetUtils;
import com.ycx.common.utils.TimeUtil;
import com.ycx.gateway.config.center.api.ConfigCenter;
import com.ycx.gateway.config.center.nacos.NacosConfigCenter;
import com.ycx.gateway.register.center.api.RegisterCenter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;

import static com.ycx.common.constants.BasicConst.COLON_SEPARATOR;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        // 加载网关核心静态配置
        final Config config = ConfigLoader.getInstance().load(args);
        System.out.println("working on " + config.getPort());
        // 插件初始化

        // 配置中心管理初始化，连接配置中心，监听配置的新增、修改、删除
        ConfigCenter configCenter = new NacosConfigCenter();
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRulesChange(rules -> {
            DynamicConfigManager.getInstance().putAllRule(rules);
        });

        // 启动容器
        Container container = new Container(config);
        container.start();

        // 连接注册中心，将注册中心的实例加载到本地
        final RegisterCenter registerCenter = registerAndSubscribe(config);

        // 服务优雅关机
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> registerCenter.deregister(buildGatewayServiceDefinition(config), buildGatewayServiceInstance(config))
        ));

    }

    public static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(config.getRegistryAddress(), config.getEnv());

        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        registerCenter.register(serviceDefinition, serviceInstance);

        registerCenter.subscribeAllServices((definition, serviceInstanceSet) -> {
            log.info("refresh service and instance {} {}", definition.getUniqueId(), JSON.toJSON(serviceInstanceSet));
            DynamicConfigManager manager = DynamicConfigManager.getInstance();
            manager.addServiceInstance(definition.getUniqueId(), serviceInstanceSet);
        });

        return registerCenter;
    }

    public static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        Integer port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setServiceInstanceId(localIp + COLON_SEPARATOR + port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    public static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(Map.of());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
