package com.worldline.isa.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.worldline.isa.service.PackageAdapter;
import com.worldline.isa.util.TraceGenerator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 数据解包Handler 类, 负责接收pos 消息并解包。
 * 
 * @author Belong.
 */
// 标示一个ChannelHandler可以被多个 Channel 安全地共享
@Sharable
public class UnpackHandler extends ChannelInboundHandlerAdapter {
	//log
	private static Logger logger = Logger.getLogger(UnpackHandler.class);
	//Attribute 数据通信使用的对象
//	public static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey
//			.valueOf("netty.channel");
	//全局INDEX
	public static final AttributeKey<String> ISO_INDEX_KEY = AttributeKey
			.valueOf("iso.index");
	//生成随机终端交易流水号
	private static TraceGenerator tg;
	
	static{
		tg = TraceGenerator.getInstance();
	}
	
	/**
	 * 接收消息, 数据解包处理类 
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//No.
		String thisIndex = new SimpleDateFormat("yyyyMMdd").format(new Date()) + tg.nextTrace();
		logger.info("[receiveMessage] NO." + thisIndex + " . ");
		
		ByteBuf in = (ByteBuf) msg;
		byte[] req = new byte[in.readableBytes()];
		in.readBytes(req);
		Map<String, String> unpackAdapter = PackageAdapter.unpackAdapter(req,"iso8583-DPI.xml");
		
		logger.info("[unPackMessage] NO." + thisIndex + " Finished. ");
		// 直接获取AttributeMap 但是是线程安全，本Channel 创建的key 对应的value
		
		// 只能这个Channel 修改，其他Channel 不能直接修改
		Attribute<String> attr2 = ctx.attr(ISO_INDEX_KEY);
		//存No.
		attr2.getAndSet(thisIndex);
		// Attribute 依附于channelHandlerContext， 本身结构和Map 类似
		// 通过Channel 获取Attribute， 本Channel 创建的key 对应的value，
		// 其他Channel 能直接修改
//		Attribute<NettyChannel> attr = ctx.channel().attr(NETTY_CHANNEL_KEY);
//		attr.getAndSet(new NettyChannel("unpackAdapter", unpackAdapter));
		
		
//		ctx.write(unpackAdapter);
		// 通知执行下一个InboundHandler
		ctx.fireChannelRead(unpackAdapter);
	}

	/**
	 * Channel 关闭前触发, 将要返回的数据Flush (可以在此记录log, 或通过事务配置log)
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// 将未决消息冲刷到远程节点，并且关闭该 Channel
		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
				ChannelFutureListener.CLOSE);
		//log 待修改, 分配事务处理, 
		logger.info("[completeMessage] NO." + ctx.channel().attr(ISO_INDEX_KEY).get() + " finished. ");
	}

	/**
	 * 捕获异常
	 * 
	 * @param ctx
	 * @param cause
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 打印异常栈跟踪
		logger.error("[MessageError] NO." + ctx.channel().attr(ISO_INDEX_KEY).get() + " " + cause.getLocalizedMessage() + ".");
//		cause.printStackTrace();
		// 关闭该Channel
		ctx.close();
	}
}

/**
 * io.netty 中Channel 间数据通信操作对象, 类似Map<k, v> 模式
 * 
 * @author Belong.
 */
class NettyChannel {

	private String name;

	private Map<String, String> Data;

	NettyChannel(String name, Map<String, String> data) {
		super();
		this.name = name;
		Data = data;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	Map<String, String> getData() {
		return Data;
	}

	void setData(Map<String, String> data) {
		Data = data;
	}

}
