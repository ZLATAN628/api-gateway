package com.ycx.core.helper;

import com.ycx.common.constants.BasicConst;
import com.ycx.common.enums.ResponseCode;
import com.ycx.core.context.BasicContext;
import com.ycx.core.context.IContext;
import com.ycx.core.response.GatewayResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

import java.util.Objects;

public class ResponseHelper {
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        GatewayResponse response = GatewayResponse.buildGatewayResponse(responseCode);
        DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(response.getContent().getBytes()));
        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
        return fullHttpResponse;
    }

    private static FullHttpResponse getHttpResponse(IContext ctx, GatewayResponse gatewayResponse) {
        ByteBuf content;
        if (Objects.nonNull(gatewayResponse.getFutureResponse())) {
            content = Unpooled.wrappedBuffer(gatewayResponse.getFutureResponse().getResponseBodyAsByteBuffer());
        } else if (gatewayResponse.getContent() != null) {
            content = Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes());
        } else {
            content = Unpooled.wrappedBuffer(BasicConst.BLANK_SEPARATOR_1.getBytes());
        }

        if (Objects.isNull(gatewayResponse.getFutureResponse())) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, gatewayResponse.getStatus(), content);
            httpResponse.headers().add(gatewayResponse.getHeaders());
            httpResponse.headers().add(gatewayResponse.getExtraHeaders());
            httpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            return httpResponse;
        } else {
            gatewayResponse.getFutureResponse().getHeaders().add(gatewayResponse.getExtraHeaders());
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(gatewayResponse.getFutureResponse().getStatusCode()),
                    content);
            httpResponse.headers().add(gatewayResponse.getFutureResponse().getHeaders());
            return httpResponse;
        }
    }


    public static void writeResponse(IContext context) {
        context.releaseRequest();

        if (context.isWritten()) {
            FullHttpResponse response = ResponseHelper.getHttpResponse(context, (GatewayResponse) context.getResponse());
            if (!context.isKeepAlive()) {
                context.getNettyCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                context.getNettyCtx().writeAndFlush(response);
            }
            context.completed();
        } else if (context.isCompleted()) {
            context.invokeCompletedCallBack();
        }
    }
}
