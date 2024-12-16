package com.ycx.core.netty;

import com.ycx.common.utils.RemotingUtil;
import com.ycx.core.Config;
import com.ycx.core.LifeCycle;
import com.ycx.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
@Data
public class NettyHttpServer implements LifeCycle {

    private final Config config;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private final NettyProcessor processor;

    public NettyHttpServer(Config config, NettyProcessor processor) {
        this.processor = processor;
        this.config = config;
        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(config.getEventLoopGroupNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.workerGroup = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-nio"));
        } else {
            this.bossGroup = new NioEventLoopGroup(config.getEventLoopGroupNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.workerGroup = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-nio"));
        }
    }

    public boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        this.serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(config.getMaxContentLength()),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler(processor));
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", config.getPort());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
