package com.ycx.gateway.client.support.dubbo;

import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;
import com.ycx.common.constants.BasicConst;
import com.ycx.common.constants.GatewayConst;
import com.ycx.common.utils.NetUtils;
import com.ycx.common.utils.TimeUtil;
import com.ycx.gateway.client.core.ApiAnnotationScanner;
import com.ycx.gateway.client.core.ApiProperties;
import com.ycx.gateway.client.support.AbstractClientRegisterManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Dubbo27ClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent> {

    private Set<Object> set = new HashSet<>();

    public Dubbo27ClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ServiceBeanExportedEvent event) {
            try {
                ServiceBean serviceBean = event.getServiceBean();
                doRegisterDubbo(serviceBean);
            } catch (Exception e) {
                log.error("doRegisterDubbo error", e);
                throw new RuntimeException(e);
            }
        } else if (applicationEvent instanceof ApplicationStartedEvent) {
            log.info("dubbo api started");
        }
    }

    void doRegisterDubbo(ServiceBean serviceBean) {
        Object bean = serviceBean.getRef();
        if (set.contains(bean)) {
            return;
        }

        ServiceDefinition definition = ApiAnnotationScanner.getInstance().scanner(bean, serviceBean);

        if (definition == null) {
            return;
        }

        definition.setEnvType(getApiProperties().getEnv());
        ServiceInstance serviceInstance = new ServiceInstance();
        String localIp = NetUtils.getLocalIp();
        int port = serviceBean.getProtocol().getPort();
        String version = definition.getVersion();
        String uniqueId = definition.getUniqueId();
        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
        serviceInstance.setIp(localIp);
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setUniqueId(uniqueId);
        serviceInstance.setPort(port);
        serviceInstance.setVersion(version);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);
        register(definition, serviceInstance);
        set.add(bean);
    }
}
