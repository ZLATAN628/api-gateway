package com.ycx.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ycx.common.enums.ResponseCode;
import com.ycx.common.utils.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

@Data
public class GatewayResponse {

    private HttpHeaders headers = new DefaultHttpHeaders();

    private HttpHeaders extraHeaders = new DefaultHttpHeaders();

    private String content;

    private HttpResponseStatus status;

    private Response futureResponse;

    public GatewayResponse() {

    }

    public void putHeader(CharSequence key, CharSequence val) {
        headers.add(key, val);
    }

    public static GatewayResponse buildGatewayResponse(Response response) {
        GatewayResponse res = new GatewayResponse();
        res.setFutureResponse(response);
        res.setStatus(HttpResponseStatus.valueOf(response.getStatusCode()));
        return res;
    }

    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());

        GatewayResponse response = new GatewayResponse();
        response.setStatus(code.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }

    public static GatewayResponse buildGatewayResponse(Object obj) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, obj);

        GatewayResponse response = new GatewayResponse();
        response.setStatus(ResponseCode.SUCCESS.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }

}
