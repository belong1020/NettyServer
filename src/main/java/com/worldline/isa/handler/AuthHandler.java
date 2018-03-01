package com.worldline.isa.handler;

import static com.worldline.isa.handler.UnpackHandler.ISO_INDEX_KEY;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ddup.dubbo.service.api.PersonService;
import com.worldline.isa.service.api.ServiceAdapter;
import com.worldline.isa.util.ServerApplicationContext;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

/**
 * 业务处理Handler 类
 * 
 * @author Belong.
 */
@Sharable
public class AuthHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = Logger.getLogger(AuthHandler.class);
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

		// 通过Channel 获取Attribute， 本Channel 创建的key 对应的value
		// 其他Channel 能直接修改
//		Attribute<NettyChannel> attr = ctx.channel().attr(NETTY_CHANNEL_KEY);
//		NettyChannel nChannel = attr.get();
		Map<String, String> unpackMap = (Map<String, String>) msg;

		Map<String, String> AuthMap = null;
		try {
//			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-dubbo.xml");
//			context.start();
			ClassPathXmlApplicationContext context = ServerApplicationContext.getDubboContext();
			ServiceAdapter serviceAdapter = (ServiceAdapter)context.getBean("serviceAdapter");
			// 授权servlet
			AuthMap = serviceAdapter.doService(unpackMap);
		} catch (InstantiationException e) {
			//
			logger.error("[MessageError] NO." + ctx.attr(ISO_INDEX_KEY).get() + "	" + e.getMessage() + ".");
		} catch (ClassNotFoundException e) {
			// 未找到serverImp 类
			logger.error("[MessageError] NO." + ctx.attr(ISO_INDEX_KEY).get() + "	" + e.getMessage() + ".");
		} catch (NoSuchMethodException e) {
			// subString() mti号
			logger.error("[MessageError] NO." + ctx.attr(ISO_INDEX_KEY).get() + "	" + e.getMessage() + ".");
		} catch (Exception e) {
//			throw new Simple8583Exception("AuthHandler.java 类 " + "遇到错误 :"
//					+ e.getMessage());
			logger.error("[MessageError] NO." + ctx.attr(ISO_INDEX_KEY).get() + "	" + e.getMessage() + ".");
		}
		logger.info("[authMessage] NO." + ctx.attr(ISO_INDEX_KEY).get() + " Finished. ");
		
		//Channel 间通信
//		NettyChannel newNChannel = new NettyChannel("AuthMap", AuthMap);
//		attr.set(newNChannel);

//		com.alibaba.dubbo.remoting.transport.AbstractClient.connect(); aop 全拦截, 
//		dubbo 宕时报com.alibaba.dubbo.remoting.RemotingException  AuthMap 会nullpointexception
		
		
		ctx.fireChannelRead(AuthMap);
	}

	/**
	 * Channel 关闭前触发, 将要返回的数据Flush (可以在此记录log, 或通过事务配置日志)
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// 将未决消息冲刷到远程节点，并且关闭该 Channel
//		 ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
//		 .addListener(ChannelFutureListener.CLOSE);
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
		logger.error("[MessageError] NO." + ctx.channel().attr(ISO_INDEX_KEY).get() + "	" + cause.getMessage() + ".");
		cause.printStackTrace();
		// 关闭该Channel
		ctx.close();
	}

}
