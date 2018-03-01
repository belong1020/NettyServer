package com.worldline.isa.socketio;

import org.apache.log4j.Logger;
//import org.apache.log4j.spi.LoggerFactory;
import org.springframework.stereotype.Service;

import com.worldline.isa.handler.AuthHandler;
import com.worldline.isa.handler.PackHandler;
import com.worldline.isa.handler.UnpackHandler;
import com.worldline.isa.schedular.CancelableScheduler;
import com.worldline.isa.schedular.HashedWheelTimeoutScheduler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
@Service
public class SocketIOChannelInitializer extends ChannelInitializer<Channel> {

	// handler name
	public static final String UNPACK_HANDLER = "UnpackHandler";
	public static final String AUTHORIZE_HANDLER = "AuthHandler";
	public static final String PACK_HANDLER = "PackHandler";

	private static final Logger log = Logger.getLogger(SocketIOChannelInitializer.class);

	// handler
	private UnpackHandler unpackHandler;
	private AuthHandler authHandler;
	private PackHandler packHandler;

	private CancelableScheduler scheduler = new HashedWheelTimeoutScheduler();

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		scheduler.update(ctx);
	}

	/**
	 * init Channel method
	 * 
	 * @param ch
	 * @throws Exception
	 */
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		unpackHandler = new UnpackHandler();
		authHandler = new AuthHandler();
		packHandler = new PackHandler();
		
		pipeline.addLast(UNPACK_HANDLER, unpackHandler);
		pipeline.addLast(AUTHORIZE_HANDLER, authHandler);
		pipeline.addLast(PACK_HANDLER, packHandler);

	}

	public void stop() {
		// StoreFactory factory = configuration.getStoreFactory();
		// factory.shutdown();
		scheduler.shutdown();
	}

}
