package com.ycx.core.request;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

public interface IGatewayRequest {
    void setModifyHost(String host);

    String getModifyHost();

    void setModifyPath(String path);

    String getModifyPath();

    void addHeader(CharSequence name, String value);

    void setHeader(CharSequence name, String value);

    void addQueryParam(String name, String value);

    void addFormParam(String name, String value);

    void addOrReplaceCookie(Cookie cookie);

    void setTimeout(int timeout);

    String getFinalUrl();

    Request build();
}
