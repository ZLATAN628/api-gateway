package com.ycx.gateway.client.core;


import lombok.Getter;

@Getter
public enum ApiProtocol {
    HTTP("http", "http协议"),
    DUBBO("dubbo", "dubbo协议");

    String code;

    String desc;

    ApiProtocol(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
