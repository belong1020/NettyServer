package com.worldline.isa.handler;

import static com.worldline.isa.handler.UnpackHandler.ISO_INDEX_KEY;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.isa.service.PackageAdapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 数据打包Handler 类
 * 
 * @author Belong.
 */
@Sharable
public class PackHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(PackHandler.class);
	/**
	 * 接收消息, 将业务处理完成后结果返回pos 端
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Map<String, String> returMap = (Map<String, String>) msg;
		
		byte[] res = PackageAdapter.packAdapter(returMap);

		ByteBuf in = Unpooled.wrappedBuffer(res);
		
		ctx.write(in);
		
//		ReferenceCountUtil.release(msg);
	}

	/**
	 * Channel 关闭前触发, 将要返回的数据Flush (可以在此记录log, 或通过事务配置日志)
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
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
		logger.error("Isa_Msg_PackageHandler has RunTimeError whith {}", cause);
		// 关闭该Channel
		ctx.close();
	}

}
