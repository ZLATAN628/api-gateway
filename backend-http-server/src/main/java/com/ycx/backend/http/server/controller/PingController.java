package com.ycx.backend.http.server.controller;

import com.ycx.gateway.client.core.ApiInvoker;
import com.ycx.gateway.client.core.ApiProperties;
import com.ycx.gateway.client.core.ApiProtocol;
import com.ycx.gateway.client.core.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patternPath = "/http-server/**")
public class PingController {

    @Autowired
    private ApiProperties apiProperties;

    @GetMapping("/http-server/ping")
    @ApiInvoker(path = "/http-server/ping")
    public String ping() {
        log.info("{}", apiProperties);
        return "pong";
    }
}
