package com.worldline.isa.handler;

import static com.worldline.isa.handler.UnpackHandler.ISO_INDEX_KEY;

import java.util.Map;

import org.apache.log4j.Logger;

import com.worldline.isa.service.PackageAdapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;

/**
 * 数据打包Handler 类
 * 
 * @author Belong.
 */
@Sharable
public class PackHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = Logger.getLogger(PackHandler.class);
	/**
	 * 接收消息, 将业务处理完成后结果返回pos 端
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
		Map<String, String> returMap = (Map<String, String>) msg;
		
		byte[] res = PackageAdapter.packAdapter(returMap);

		ByteBuf in = Unpooled.copiedBuffer(new String(), CharsetUtil.UTF_8);
		in.writeBytes(res);
		//write 结果
//		ctx.writeAndFlush(Unpooled.copiedBuffer(new String(res), CharsetUtil.UTF_8));
		ctx.write(in);
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
		ctx.flush();
		logger.info("[replyMessage] NO." + ctx.channel().attr(ISO_INDEX_KEY).get() + " Finished. ");
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
		cause.printStackTrace();
		// 关闭该Channel
		ctx.close();
	}

}
