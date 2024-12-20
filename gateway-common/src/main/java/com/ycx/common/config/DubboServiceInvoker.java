package com.ycx.common.config;

import lombok.Data;

@Data
public class DubboServiceInvoker extends AbstractServiceInvoker {

    private String registerAddress;

    private String interfaceClass;

    private String methodName;

    private String[] parameterTypes;

    private String version;

}
