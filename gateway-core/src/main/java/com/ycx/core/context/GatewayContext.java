package com.ycx.core.context;

import com.ycx.common.config.Rule;
import com.ycx.common.utils.AssertUtil;
import com.ycx.core.request.GatewayRequest;
import com.ycx.core.response.GatewayResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class GatewayContext extends BasicContext {

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        super(protocol, nettyCtx, keepAlive);
    }

    private GatewayRequest request;

    private GatewayResponse response;

    private Rule rule;

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive, GatewayRequest request, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
    }

    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;
        private GatewayRequest request;
        private Rule rule;
        private boolean keepAlive;

        public Builder() {

        }

        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder request(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder nettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol can not be null");
            AssertUtil.notNull(nettyCtx, "nettyCtx can not be null");
            AssertUtil.notNull(request, "request can not be null");
            AssertUtil.notNull(rule, "rule can not be null");
            return new GatewayContext(protocol, nettyCtx, keepAlive, request, rule);
        }
    }

    public <T> T getRequireAttribute(String key) {
        T value = (T) getAttribute(key);
        AssertUtil.notNull(value, "缺乏必要参数");
        return value;
    }

    public <T> T getRequireAttribute(String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    public String getUniqueId() {
        return request.getUniqueId();
    }

    @Override
    public void releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }

    public GatewayRequest getOriginalRequest() {
        return request;
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    public void setResponse(GatewayResponse response) {
        this.response = response;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
