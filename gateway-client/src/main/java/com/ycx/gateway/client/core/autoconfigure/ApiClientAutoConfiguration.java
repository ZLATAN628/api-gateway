package com.ycx.gateway.client.core.autoconfigure;

import com.ycx.gateway.client.core.ApiProperties;
import com.ycx.gateway.client.support.dubbo.Dubbo27ClientRegisterManager;
import com.ycx.gateway.client.support.springmvc.SpringMvcClientRegisterManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "api", name = {"registerAddress"})
public class ApiClientAutoConfiguration {

    private ApiProperties apiProperties;

    @Autowired
    public ApiClientAutoConfiguration(ApiProperties apiProperties) {
        log.info("api properties : {}", apiProperties);
        this.apiProperties = apiProperties;
    }

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMvcClientRegisterManager.class)
    public SpringMvcClientRegisterManager springMvcClientRegisterManager() {
        return new SpringMvcClientRegisterManager(apiProperties);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegisterManager.class)
    public Dubbo27ClientRegisterManager dubbo27ClientRegisterManager() {
        return new Dubbo27ClientRegisterManager(apiProperties);
    }


}
