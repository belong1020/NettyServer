package com.worldline.isa.socketio;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

/**
 * Fully thread-safe.
 *
 */
@Controller
@Scope("prototype")
public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private Configuration configuration;
    
    @Autowired
    private ChannelInitializer<Channel> pipelineFactory ;//= new SocketIOChannelInitializer();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    
    public void setPipelineFactory(SocketIOChannelInitializer pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    /**
     * Start server
     */
    public void start() {
        startAsync().syncUninterruptibly();
    }

    /**
     * Start server asynchronously
     */
    public Future<Void> startAsync() {
        log.info("Session store / pubsub factory used: { }" );//+configuration.getStoreFactory()
        initGroups();

        //start() configCopy 配置pipeline, encode decode authHandler, 
//        pipelineFactory.start(configCopy);

        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        if (configuration.isUseLinuxNativeEpoll()) {
            channelClass = EpollServerSocketChannel.class;
        }

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
        .channel(channelClass)
        .childHandler(pipelineFactory);
        applyConnectionOptions(b);

        InetSocketAddress addr = new InetSocketAddress(configuration.getPort());
        if (configuration.getHostname() != null) {
            addr = new InetSocketAddress(configuration.getHostname(), configuration.getPort());
        }

        return b.bind(addr).addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (future.isSuccess()) {
                    log.info("SocketIO server started at port: { "+ configuration.getPort() +" }");
                } else {
                    log.error("SocketIO server start failed at port: { "+ configuration.getPort() +" }!");
                }
            }
        });
    }

    //bootstrap 配置
    protected void applyConnectionOptions(ServerBootstrap bootstrap) {
        SocketConfig config = configuration.getSocketConfig();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
        if (config.getTcpSendBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getTcpSendBufferSize());
        }
        if (config.getTcpReceiveBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getTcpReceiveBufferSize());
            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(config.getTcpReceiveBufferSize()));
        }
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.isTcpKeepAlive());
        bootstrap.childOption(ChannelOption.SO_LINGER, config.getSoLinger());

        bootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
        bootstrap.option(ChannelOption.SO_BACKLOG, config.getAcceptBackLog());
    }

    protected void initGroups() {
        if (configuration.isUseLinuxNativeEpoll()) {
            bossGroup = new EpollEventLoopGroup(configuration.getBossThreads());
            workerGroup = new EpollEventLoopGroup(configuration.getWorkerThreads());
        } else {
            bossGroup = new NioEventLoopGroup(configuration.getBossThreads());
            workerGroup = new NioEventLoopGroup(configuration.getWorkerThreads());
        }
    }

    /**
     * Stop server
     */
    public void stop() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();

        ((SocketIOChannelInitializer) pipelineFactory).stop();
        // 3. shutdown netty的线程执行器
//        factory.releaseExternalResources();
        log.info("SocketIO server stopped");
    }

    public void setConfiguration(Configuration configuration){
    	this.configuration = configuration;
    }
    
    /**
     * Allows to get configuration provided
     * during server creation. Further changes on
     * this object not affect server.
     *
     * @return Configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }


}
