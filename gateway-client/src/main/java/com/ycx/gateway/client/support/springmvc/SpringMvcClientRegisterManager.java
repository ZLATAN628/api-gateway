package com.ycx.gateway.client.support.springmvc;

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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SpringMvcClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    private Set<Object> set = new HashSet<>();

    public SpringMvcClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            doRegisterSpringMvc();
        }
    }

    private void doRegisterSpringMvc() {
        Map<String, RequestMappingHandlerMapping> allRequestMapping =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);

        for (RequestMappingHandlerMapping handlerMapping : allRequestMapping.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> me : handlerMethods.entrySet()) {
                HandlerMethod handlerMethod = me.getValue();
                Class<?> beanType = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(beanType);

                if (set.contains(bean)) {
                    continue;
                }

                ServiceDefinition definition = ApiAnnotationScanner.getInstance().scanner(bean);
                if (definition == null) {
                    continue;
                }

                definition.setEnvType(getApiProperties().getEnv());

                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtils.getLocalIp();
                int port = serverProperties.getPort();
                String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
                String uniqueId = definition.getUniqueId();
                String version = definition.getVersion();

                serviceInstance.setServiceInstanceId(serviceInstanceId);
                serviceInstance.setUniqueId(uniqueId);
                serviceInstance.setIp(localIp);
                serviceInstance.setPort(port);
                serviceInstance.setVersion(version);
                serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);

                register(definition, serviceInstance);
                set.add(bean);
            }
        }
    }
}
