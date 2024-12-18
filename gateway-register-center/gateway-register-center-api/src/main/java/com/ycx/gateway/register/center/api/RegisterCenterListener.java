package com.ycx.gateway.register.center.api;

import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInstance;

import java.util.Set;

public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);

}
