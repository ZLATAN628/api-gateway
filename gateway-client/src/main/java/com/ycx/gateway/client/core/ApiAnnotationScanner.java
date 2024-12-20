package com.ycx.gateway.client.core;

import com.ycx.common.config.DubboServiceInvoker;
import com.ycx.common.config.HttpServiceInvoker;
import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.config.ServiceInvoker;
import com.ycx.common.constants.BasicConst;
import com.ycx.gateway.client.support.dubbo.DubboConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ApiAnnotationScanner {

    private ApiAnnotationScanner() {

    }

    private static class SingletonHolder {
        private static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }

    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ServiceDefinition scanner(Object bean, Object... args) {
        Class<?> clazz = bean.getClass();
        if (!clazz.isAnnotationPresent(ApiService.class)) {
            return null;
        }

        ApiService apiService = clazz.getAnnotation(ApiService.class);
        String serviceId = apiService.serviceId();
        ApiProtocol protocol = apiService.protocol();
        String patternPath = apiService.patternPath();
        AtomicReference<String> version = new AtomicReference<>(apiService.version());

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();

        List<Method> methods = Arrays.asList(clazz.getMethods());
        if (CollectionUtils.isEmpty(methods)) {
            return null;
        }

        methods.forEach(method -> {
            ApiInvoker invoker = method.getAnnotation(ApiInvoker.class);
            if (invoker == null) {
                return;
            }

            String path = invoker.path();

            switch (protocol) {
                case HTTP -> {
                    HttpServiceInvoker httpInvoker = new HttpServiceInvoker();
                    httpInvoker.setInvokerPath(path);
                    invokerMap.put(path, httpInvoker);
                }
                case DUBBO -> {
                    ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                    DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                    String dubboVersion = dubboServiceInvoker.getVersion();
                    if (StringUtils.isNotBlank(dubboVersion)) {
                        version.set(dubboVersion);
                    }
                    invokerMap.put(path, dubboServiceInvoker);
                }
            }
        });

        serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
        serviceDefinition.setServiceId(serviceId);
        serviceDefinition.setVersion(version.get());
        serviceDefinition.setProtocol(protocol.getCode());
        serviceDefinition.setPatternPath(patternPath);
        serviceDefinition.setEnable(true);
        serviceDefinition.setInvokerMap(invokerMap);

        return serviceDefinition;
    }

    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);
        String address = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(address);
        dubboServiceInvoker.setMethodName(method.getName());
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }

        dubboServiceInvoker.setParameterTypes(parameterTypes);
        dubboServiceInvoker.setTimeout(getDubboTimeout(serviceBean));

        String version = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(version);

        return dubboServiceInvoker;
    }

    private static Integer getDubboTimeout(ServiceBean<?> serviceBean) {
        Integer timeout = serviceBean.getTimeout();
        if (timeout == null || timeout == 0) {
            ProviderConfig provider = serviceBean.getProvider();
            if (provider != null) {
                Integer providerTimeout = provider.getTimeout();
                if (providerTimeout == null || providerTimeout == 0) {
                    timeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    timeout = providerTimeout;
                }
            }
        }
        return timeout;
    }
}
