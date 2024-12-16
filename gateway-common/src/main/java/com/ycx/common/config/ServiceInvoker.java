package com.ycx.common.config;

public interface ServiceInvoker {

    String getInvokerPath();

    void setInvokerPath(String invokerPath);

    int getTimeout();

    void setTimeout(int timeout);
}
