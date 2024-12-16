package com.ycx.core.netty.processor;

import com.ycx.core.context.HttpRequestWrapper;

@FunctionalInterface
public interface NettyProcessor {
    void process(HttpRequestWrapper requestWrapper);
}
