package com.ycx.gateway.client.support;

import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;
import com.ycx.gateway.client.core.ApiProperties;
import com.ycx.gateway.register.center.api.RegisterCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class AbstractClientRegisterManager {
    @Getter
    private ApiProperties apiProperties;

    private RegisterCenter registerCenter;

    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(apiProperties.getRegisterAddress(), apiProperties.getEnv());
    }

    protected void register(ServiceDefinition definition, ServiceInstance instance) {
        registerCenter.register(definition, instance);
    }
}
