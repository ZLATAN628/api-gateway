package com.ycx.core.netty.processor;

import com.ycx.common.enums.ResponseCode;
import com.ycx.common.exception.BaseException;
import com.ycx.common.exception.ConnectException;
import com.ycx.common.exception.ResponseException;
import com.ycx.core.ConfigLoader;
import com.ycx.core.context.GatewayContext;
import com.ycx.core.context.HttpRequestWrapper;
import com.ycx.core.helper.AsyncHttpHelper;
import com.ycx.core.helper.RequestHelper;
import com.ycx.core.helper.ResponseHelper;
import com.ycx.core.response.GatewayResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

    public NettyCoreProcessor() {
    }

    @Override
    public void process(HttpRequestWrapper requestWrapper) {
        FullHttpRequest request = requestWrapper.getRequest();
        ChannelHandlerContext ctx = requestWrapper.getCtx();
        try {
            GatewayContext context = RequestHelper.doContext(request, ctx);
            route(context);
        } catch (BaseException e) {
            FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(ctx, request, response);
            log.error("process error {} {}", e.getCode(), e.getCode().getMessage());
        } catch (Throwable t) {
            FullHttpResponse response = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx, request, response);
            log.error("process unkown error");
        }

    }

    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ReferenceCountUtil.release(request);
    }

    private void route(GatewayContext context) {
        Request request = context.getRequest().build();
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if (whenComplete) {
            future.whenComplete((response, throwable) -> {
                complete(request, response, throwable, context);
            });
        } else {
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request, response, throwable, context);
            }));
        }

    }

    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          GatewayContext context) {
        context.releaseRequest();
        try {
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    context.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    context.setThrowable(new ConnectException(throwable, context.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                context.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } finally {
            context.writtened();
            ResponseHelper.writeResponse(context);
        }

    }
}
