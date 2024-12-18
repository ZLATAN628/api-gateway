package com.ycx.gateway.register.center.api;

import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;

public interface RegisterCenter {

    void init(String registerAddress, String env);

    void register(ServiceDefinition serviceDefinition, ServiceInstance instance);

    void deregister(ServiceDefinition serviceDefinition, ServiceInstance instance);

    void subscribeAllServices(RegisterCenterListener listener);
}
