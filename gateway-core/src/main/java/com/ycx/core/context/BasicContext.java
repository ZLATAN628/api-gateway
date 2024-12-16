package com.ycx.core.context;

import com.ycx.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BasicContext implements IContext {
    protected final String protocol;

    protected volatile int status = IContext.Running;

    protected final ChannelHandlerContext nettyCtx;

    protected final Map<String, Object> attributes = new HashMap<>();

    protected Throwable throwable;

    protected final boolean keepAlive;

    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    protected List<Consumer<IContext>> completedCallbacks;

    public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public void runned() {
        status = IContext.Running;
    }

    @Override
    public void writtened() {
        status = IContext.Written;
    }

    @Override
    public void completed() {
        status = IContext.Completed;
    }

    @Override
    public void terminated() {
        status = IContext.Terminated;
    }

    @Override
    public boolean isRunning() {
        return status == IContext.Running;
    }

    @Override
    public boolean isWritten() {
        return status == IContext.Written;
    }

    @Override
    public boolean isCompleted() {
        return status == IContext.Completed;
    }

    @Override
    public boolean isTerminated() {
        return status == IContext.Terminated;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public void setResponse(Object response) {

    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public Rule getRule() {
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setRule() {

    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public void releaseRequest() {
        this.requestReleased.set(true);
    }

    @Override
    public void setCompletedCallBack(Consumer<IContext> consumer) {
        if (completedCallbacks == null) {
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallBack() {
        if (CollectionUtils.isNotEmpty(completedCallbacks)) {
            completedCallbacks.forEach(c -> c.accept(this));
        }
    }
}
