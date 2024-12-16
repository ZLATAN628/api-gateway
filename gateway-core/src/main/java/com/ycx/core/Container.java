package com.ycx.core;

import com.ycx.core.netty.NettyHttpServer;
import com.ycx.core.netty.client.NettyHttpClient;
import com.ycx.core.netty.processor.NettyCoreProcessor;
import com.ycx.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Container implements LifeCycle {

    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        this.init();
    }

    @Override
    public void init() {
        this.nettyProcessor = new NettyCoreProcessor();
        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getWorkerGroup());
    }

    @Override
    public void start() {
        this.nettyHttpServer.start();
        this.nettyHttpClient.start();
        log.info("api gateway started");
    }

    @Override
    public void shutdown() {
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
