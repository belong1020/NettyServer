package com.worldline.isa.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.isa.service.PackageAdapter;
import com.worldline.isa.util.TraceGenerator;

import io.netty.buffer.ByteBuf;
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
	private static Logger logger = LoggerFactory.getLogger(UnpackHandler.class);
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
		Map<String, String> unpackAdapter = PackageAdapter.unpackAdapter(req,"iso8583-DPI");
		
		logger.info("[unPackMessage] NO." + thisIndex + " Finished. ");
		// 直接获取AttributeMap 但是是线程安全，本Channel 创建的key 对应的value
		
		// 只能这个Channel 修改，其他Channel 不能直接修改
		Attribute<String> attr2 = ctx.attr(ISO_INDEX_KEY);
		//存No.
		attr2.getAndSet(thisIndex);

		// 通知执行下一个InboundHandler
		ctx.fireChannelRead(unpackAdapter);
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
		logger.error("Isa_Msg_UnpackHandler[MessageError] NO.{}  {}.", ctx.channel().attr(ISO_INDEX_KEY).get() , cause.getLocalizedMessage() );
		// 关闭该Channel
		ctx.close();
	}
}