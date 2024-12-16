package com.ycx.core.netty;

import com.ycx.core.context.HttpRequestWrapper;
import com.ycx.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private NettyProcessor processor;

    public NettyHttpServerHandler(NettyProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest request) {
            HttpRequestWrapper wrapper = new HttpRequestWrapper();
            wrapper.setCtx(ctx);
            wrapper.setRequest(request);
            processor.process(wrapper);
        }
    }
}
