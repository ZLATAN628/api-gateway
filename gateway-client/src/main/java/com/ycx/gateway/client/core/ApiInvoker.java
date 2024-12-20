package com.ycx.gateway.client.core;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();
}
