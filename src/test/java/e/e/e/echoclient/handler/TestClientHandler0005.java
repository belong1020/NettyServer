package e.e.e.echoclient.handler;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.worldline.isa.model.IsoPackage;
import com.worldline.isa.service.PackageAdapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * 代码清单 2-3 客户端的 ChannelHandler
 * 
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
// 标记该类的实例可以被多个 Channel 共享
public class TestClientHandler0005 extends SimpleChannelInboundHandler<ByteBuf> {

	IsoPackage pendPack;
	
	private long start;
	private long end = System.currentTimeMillis();

	/**
	 * ChannelHandlerContext 管道控制器
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {

		Map<String, String> message = new HashMap<String, String>();
		message.put("HeaderLength", "46");
		message.put("HeaderFlagandVersion", "82");
		message.put("TotalMessageLength", "0434");
		message.put("DestinationID", "01040001");
		message.put("Blank01", "   ");
		message.put("SourceID", "00010344");
		message.put("Blank02", "   ");
		message.put("ReservedforUse", "000000");
		message.put("BatchNumber", "01");
		message.put("TransactionInformation", "10000000");
		message.put("UserInformation", "00");
		message.put("RejectCode", "00000");
		message.put("mti", "0420");
		message.put("BitMap",
				"11110010001110100110010010000001100011001111000010000000000100000000000000000000000000000100000000010000000000000000000010000001");
		message.put("2", "6210943000010001");
		message.put("3", "000000");
		message.put("4", "000000004000");
		message.put("7", "0130092713");
		message.put("11", "107489");
		message.put("12", "141202");
		message.put("13", "0227");
		message.put("15", "0527");
		message.put("18", "7011");
		message.put("19", "344");
		message.put("22", "021");
		message.put("25", "00");
		message.put("32", "12345678");
		message.put("33", "12345678");
		message.put("35", "12345678");
		message.put("37", "000000006046");
		message.put("38", "000000");
		message.put("41", "00010001");
		message.put("42", "001584054110005");
		message.put("43", "Testing Hotel            Hongkong    HKG");
		message.put("44", "4141313031");
		message.put("49", "156");
		message.put("60", "027");//占位
		message.put("60.1", "4003");
		message.put("60.2.1", "0");
		message.put("60.2.2", "2");
		message.put("60.2.3", "0");
		message.put("60.2.4", "0");
		message.put("60.2.5", "03");
		message.put("60.2.6", "0");
		message.put("60.2.7", "0");
		message.put("60.2.8", "00");
		message.put("60.2.9", "0");
		message.put("60.3.1", "00");
		message.put("60.3.2", "0");
		message.put("60.3.3", "000");
		message.put("60.3.4", "0");
		message.put("60.3.5", "1");
		message.put("60.3.6", "1");
		message.put("60.3.7", "1");
		message.put("60.3.8", "01");
		message.put("90", "010010748802271412020001234567800012345678");
		message.put("100", "01040001");
		message.put("121", "                                                                                 ");//(081)
		message.put("128", "3136333135314630");

		byte[] res;
		res = PackageAdapter.packAdapter(message, pendPack);

		ByteBuf copiedBuffer = Unpooled.copiedBuffer(new String(), CharsetUtil.UTF_8);

		copiedBuffer.writeBytes(res);

		ctx.writeAndFlush(copiedBuffer);
		start = System.currentTimeMillis();
		// System.out.println("first");
		// ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!",
		// CharsetUtil.UTF_8));

		// 当被通知 Channel是活跃的时候，发送一条消息
		// ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!",
		// CharsetUtil.UTF_8));
	}

	/**
	 * ChannelHandlerContext 管道控制器 ByteBuf 传出流数据
	 */
	@Override
	public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
		// 记录已接收消息的转储
		end = System.currentTimeMillis();
		System.out.println(this.getClass() +"is over, time --- " + (end - start));
		// System.out.println("second");
		// System.out
		// .println("Client received: " + in.toString(CharsetUtil.UTF_8));

	}

	/**
	 * ChannelHandlerContext 管道控制器 Throwable 错误Exception
	 */
	@Override
	// 在发生异常时，记录错误并关闭Channel
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
//	class TestPackAdap extends PackageAdapter {
//		
//		
//		public static byte[] packAdapter(Map<String, String> dataMap){
//			
//		}
//	}
}
