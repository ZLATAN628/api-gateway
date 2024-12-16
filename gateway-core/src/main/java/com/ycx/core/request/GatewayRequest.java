package com.ycx.core.request;

import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import com.ycx.common.constants.BasicConst;
import com.ycx.common.utils.TimeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import java.nio.charset.Charset;
import java.util.*;

public class GatewayRequest implements IGatewayRequest {
    @Getter
    private final String uniqueId;
    @Getter
    private final long beginTime;
    @Getter
    private long endTime;
    @Getter
    private final Charset charset;
    @Getter
    private final String clientIp;
    @Getter
    private final String host;
    @Getter
    private final String path;
    @Getter
    private final String uri;
    @Getter
    private final HttpMethod httpMethod;
    @Getter
    private final String contentType;
    @Getter
    private final HttpHeaders headers;
    @Getter
    private final QueryStringDecoder queryStringDecoder;
    @Getter
    private final FullHttpRequest fullHttpRequest;

    private String body;

    private Map<String, io.netty.handler.codec.http.cookie.Cookie> cookieMap;

    private Map<String, List<String>> postParameters;

    private String modifyScheme;

    private String modifyHost;

    private String modifyPath;

    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId, Charset charset, String clientIp,
                          String host, String uri, HttpMethod httpMethod, String contentType,
                          HttpHeaders headers, FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.headers = headers;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.fullHttpRequest = fullHttpRequest;
        this.path = this.queryStringDecoder.path();
        this.requestBuilder = new RequestBuilder();

        this.modifyHost = host;
        this.modifyPath = this.path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder.setMethod(getHttpMethod().name());
        this.requestBuilder.setHeaders(getHeaders());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());

        ByteBuf contentBuffer = fullHttpRequest.content();
        if (Objects.nonNull(contentBuffer)) {
            this.requestBuilder.setBody(contentBuffer.nioBuffer());
        }
    }

    public String getBody() {
        if (StringUtils.isEmpty(body)) {
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    public io.netty.handler.codec.http.cookie.Cookie getCookie(String name) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
            String cookieStr = getHeaders().get(HttpHeaderNames.COOKIE);
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                cookieMap.put(cookie.name(), cookie);
            }
        }
        return cookieMap.get(name);
    }

    public List<String> getQueryParametersMultiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    public List<String> getPostParametersMultiple(String name) {
        String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }

            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            return Lists.newArrayList(JSONPath.read(body, name).toString());
        }
        return null;
    }

    public boolean isFormPost() {
        return HttpMethod.POST.equals(httpMethod) && (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(httpMethod) && (contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString()));
    }


    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return this.modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addFormParam(String name, String value) {
        if (isFormPost()) {
            requestBuilder.addFormParam(name, value);
        }
    }

    @Override
    public void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void setTimeout(int timeout) {
        requestBuilder.setRequestTimeout(timeout);
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }
}
