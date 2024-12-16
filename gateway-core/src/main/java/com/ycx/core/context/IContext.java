package com.ycx.core.context;

import com.ycx.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

public interface IContext {
    /**
     * 运行时
     */
    int Running = 0;
    /**
     * 发生错误
     */
    int Written = 1;
    /**
     * 写回成功
     */
    int Completed = 2;
    /**
     * 请求结束
     */
    int Terminated = -1;

    void runned();

    void writtened();

    void completed();

    void terminated();

    boolean isRunning();

    boolean isWritten();

    boolean isCompleted();

    boolean isTerminated();

    String getProtocol();

    Object getRequest();

    Object getResponse();

    void setResponse(Object response);

    Throwable getThrowable();

    Rule getRule();

    Object getAttribute(String key);

    void setRule();

    void setThrowable(Throwable throwable);

    void setAttribute(String key, Object value);

    ChannelHandlerContext getNettyCtx();

    boolean isKeepAlive();

    void releaseRequest();

    void setCompletedCallBack(Consumer<IContext> consumer);

    void invokeCompletedCallBack();
}
