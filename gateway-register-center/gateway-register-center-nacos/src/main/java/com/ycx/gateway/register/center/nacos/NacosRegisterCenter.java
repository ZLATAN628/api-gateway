package com.ycx.gateway.register.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;
import com.ycx.common.constants.GatewayConst;
import com.ycx.gateway.register.center.api.RegisterCenter;
import com.ycx.gateway.register.center.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class NacosRegisterCenter implements RegisterCenter {

    private String registerAddress;

    private String env;

    private NamingService namingService;

    private NamingMaintainService namingMaintainService;

    private List<RegisterCenterListener> listeners;


    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env = env;

        try {
            this.namingService = NamingFactory.createNamingService(registerAddress);
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance instance) {
        Instance nacosInstance = new Instance();
        nacosInstance.setInstanceId(instance.getServiceInstanceId());
        nacosInstance.setPort(instance.getPort());
        nacosInstance.setIp(instance.getIp());
        nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(instance)));
        try {
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0,
                    Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceDefinition)));

            log.info("register {} {}", serviceDefinition, instance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance instance) {
        try {
            namingService.deregisterInstance(serviceDefinition.getServiceId(), env, instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener listener) {
        listeners.add(listener);
        doSubscribeAllServices();
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        threadPool.scheduleWithFixedDelay(this::doSubscribeAllServices, 10, 10, TimeUnit.SECONDS);
    }

    private void doSubscribeAllServices() {
        try {
            Set<String> subscribeServices = namingService.getSubscribeServices()
                    .stream().map(ServiceInfo::getName).collect(Collectors.toSet());

            int pageNo = 1;
            int pageSize = 100;

            NacosRegisterListener eventListener = new NacosRegisterListener();
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();

            while (CollectionUtils.isNotEmpty(serviceList)) {
                log.info("service list size {}", serviceList.size());
                for (String service : serviceList) {
                    if (subscribeServices.contains(service)) {
                        continue;
                    }
                    namingService.subscribe(service, eventListener);
                    log.info("subscribe service {} {}", service, env);
                }
                serviceList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent namingEvent) {
                String serviceName = namingEvent.getServiceName();
                try {
                    Service service = namingMaintainService.queryService(serviceName, env);
                    ServiceDefinition serviceDefinition = JSON.parseObject(service.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);
                    List<Instance> allInstances = namingService.getAllInstances(serviceName, env);
                    HashSet<ServiceInstance> set = new HashSet<>();

                    for (Instance instance : allInstances) {
                        ServiceInstance serviceInstance = JSON.parseObject(instance.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        set.add(serviceInstance);
                    }
                    listeners.forEach(listener -> listener.onChange(serviceDefinition, set));
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
