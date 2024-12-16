package com.ycx.core.helper;

import com.ycx.common.config.HttpServiceInvoker;
import com.ycx.common.config.Rule;
import com.ycx.common.config.ServiceDefinition;
import com.ycx.common.constants.BasicConst;
import com.ycx.common.constants.GatewayConst;
import com.ycx.common.constants.GatewayProtocol;
import com.ycx.core.context.GatewayContext;
import com.ycx.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RequestHelper {

    public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
        GatewayRequest gatewayRequest = doRequest(request, ctx);

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
                .serviceId("demo")
                .enable(true)
                .version("v1")
                .patternPath("**")
                .envType("dev")
                .protocol(GatewayProtocol.HTTP)
                .build();

        HttpServiceInvoker serviceInvoker = new HttpServiceInvoker();
        serviceInvoker.setInvokerPath(gatewayRequest.getPath());
        serviceInvoker.setTimeout(500);

        GatewayContext context = new GatewayContext.Builder()
                .protocol(serviceDefinition.getProtocol())
                .nettyCtx(ctx)
                .request(gatewayRequest)
                .rule(new Rule())
                .keepAlive(HttpUtil.isKeepAlive(request))
                .build();

        // TODO 服务发现
        context.getRequest().setModifyHost("127.0.0.1:8080");

        return context;
    }

    public static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullHttpRequest.headers();
        String uniqueId = headers.get(GatewayConst.UNIQUE_ID);
        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = Optional.ofNullable(HttpUtil.getMimeType(fullHttpRequest))
                .map(CharSequence::toString).orElse(null);
        Charset charset = HttpUtil.getCharset(fullHttpRequest);
        return new GatewayRequest(uniqueId, charset, clientIp, host, uri, method, contentType, headers, fullHttpRequest);
    }

    public static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        String xForwardValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardValue)) {
            List<String> values = Arrays.asList(xForwardValue.split(","));
            if (!values.isEmpty() && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = socketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }
}
