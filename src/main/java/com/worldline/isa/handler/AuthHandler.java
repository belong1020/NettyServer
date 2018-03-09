package com.worldline.isa.handler;

import static com.worldline.isa.handler.UnpackHandler.ISO_INDEX_KEY;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.isa.model.IsoPackage;
import com.worldline.isa.service.api.ServiceAdapter;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 业务处理Handler 类
 * 
 * @author Belong.
 */
@Sharable
public class AuthHandler extends ChannelInboundHandlerAdapter {

	private ServiceAdapter serviceAdapter;
	
	public AuthHandler(ServiceAdapter serviceAdapter) {
		this.serviceAdapter = serviceAdapter;
	}
	private static Logger logger = LoggerFactory.getLogger(AuthHandler.class);
	/**
	 * 接收消息, 接收EchoServerHandler 发来的解包后的map 类型数据unpackMap , 
	 * 调用ServletAdapter.servlet1() 方法, 方法内部自动调用不同mti 对应service
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Map<String, String> unpackMap = (Map<String, String>) msg;
		Map<String, String> AuthMap = null;
		try {
			// 授权servlet
			AuthMap = this.serviceAdapter.doService(unpackMap);
		} catch (Exception e) {
//			logger.error("[MessageError] NO.{} {}", ctx.channel().attr(ISO_INDEX_KEY).get() + "	" + e.getMessage() + ".");
		}
		
		ctx.fireChannelRead(AuthMap);
	}

	/**
	 * 捕获异常
	 * 
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// 打印异常栈跟踪
		logger.error("[MessageError] NO.{}" + ctx.channel().attr(ISO_INDEX_KEY).get() + "	" + cause.getMessage() + ".");
		// 关闭该Channel
		ctx.close();
	}

}
