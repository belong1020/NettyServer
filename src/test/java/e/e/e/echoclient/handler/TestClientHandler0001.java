package e.e.e.echoclient.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

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
public class TestClientHandler0001 extends SimpleChannelInboundHandler<ByteBuf> {

	private long start;
	private long end = System.currentTimeMillis();

	/**
	 * ChannelHandlerContext 管道控制器
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		/**
		 * [48, 51, 54, 55, 46, -126, 48, 51, 54, 55, 48, 49, 48, 52, 48, 48,
		 * 48, 49, 32, 32, 32, 48, 48, 48, 49, 48, 51, 52, 52, 32, 32, 32, 0, 0,
		 * 0, 1, 49, 48, 48, 48, 48, 48, 48, 48, 0, 48, 48, 48, 48, 48, 48, 49,
		 * 48, 48, -14, 62, 100, -63, -88, -31, -104, 16, 0, 0, 0, 0, 16, 0, 0,
		 * 1, 49, 54, 54, 50, 49, 48, 57, 52, 51, 48, 48, 48, 48, 49, 48, 48,
		 * 48, 49, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 52,
		 * 48, 48, 48, 48, 49, 49, 48, 49, 52, 50, 57, 53, 52, 49, 48, 55, 49,
		 * 49, 50, 49, 52, 50, 57, 53, 52, 48, 49, 49, 48, 52, 57, 49, 50, 48,
		 * 53, 50, 55, 55, 48, 49, 49, 51, 52, 52, 48, 50, 49, 48, 48, 48, 54,
		 * 48, 56, 49, 50, 51, 52, 53, 54, 55, 56, 48, 56, 49, 50, 51, 52, 53,
		 * 54, 55, 56, 51, 55, 54, 50, 49, 48, 57, 52, 51, 48, 48, 48, 48, 49,
		 * 48, 48, 48, 49, 61, 52, 57, 49, 50, 49, 48, 49, 50, 53, 54, 51, 52,
		 * 56, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48, 48, 48, 48, 48, 53,
		 * 55, 50, 48, 48, 48, 48, 49, 48, 48, 48, 49, 48, 48, 49, 53, 56, 52,
		 * 48, 53, 52, 49, 49, 48, 48, 48, 53, 84, 101, 115, 116, 105, 110, 103,
		 * 32, 72, 111, 116, 101, 108, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		 * 32, 32, 72, 111, 110, 103, 107, 111, 110, 103, 32, 32, 32, 32, 72,
		 * 75, 71, 48, 48, 53, 65, 65, 49, 48, 49, 49, 53, 54, -56, -21, 41,
		 * -17, 53, -86, 88, 21, 50, 54, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48,
		 * 48, 48, 48, 48, 48, 50, 55, 48, 48, 48, 48, 48, 50, 48, 48, 48, 51,
		 * 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 48, 49,
		 * 48, 56, 48, 49, 48, 52, 48, 48, 48, 49, 69, 65, 51, 56, 53, 49, 51,
		 * 70]
		 */
		/*byte[] bb = { 48, 51, 54, 55, 46, (byte) 130, 48, 51, 54, 55, 48, 49, 48, 52, 48, 48, 48, 49, 32, 32, 32, 48,
				48, 48, 49, 48, 51, 52, 52, 32, 32, 32, 0, 0, 0, 1, 49, 48, 48, 48, 48, 48, 48, 48, 0, 48, 48, 48, 48,
				48, 48, 49, 48, 48, (byte) 242, 62, 100, (byte) 193, (byte) 168, (byte) 225, (byte) 152, 16, 0, 0, 0, 0,
				16, 0, 0, 1, 49, 54, 54, 50, 49, 48, 57, 52, 51, 48, 48, 48, 48, 49, 48, 48, 48, 49, 48, 48, 48, 48, 48,
				48, 48, 48, 48, 48, 48, 48, 48, 48, 52, 48, 48, 48, 48, 49, 49, 48, 49, 52, 50, 57, 53, 52, 49, 48, 55,
				49, 49, 50, 49, 52, 50, 57, 53, 52, 48, 49, 49, 48, 52, 57, 49, 50, 48, 53, 50, 55, 55, 48, 49, 49, 51,
				52, 52, 48, 50, 49, 48, 48, 48, 54, 48, 56, 49, 50, 51, 52, 53, 54, 55, 56, 48, 56, 49, 50, 51, 52, 53,
				54, 55, 56, 51, 55, 54, 50, 49, 48, 57, 52, 51, 48, 48, 48, 48, 49, 48, 48, 48, 49, 61, 52, 57, 49, 50,
				49, 48, 49, 50, 53, 54, 51, 52, 56, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48, 48, 48, 48, 48, 53, 55,
				50, 48, 48, 48, 48, 49, 48, 48, 48, 49, 48, 48, 49, 53, 56, 52, 48, 53, 52, 49, 49, 48, 48, 48, 53, 84,
				101, 115, 116, 105, 110, 103, 32, 72, 111, 116, 101, 108, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
				32, 72, 111, 110, 103, 107, 111, 110, 103, 32, 32, 32, 32, 72, 75, 71, 48, 48, 53, 65, 65, 49, 48, 49,
				49, 53, 54, (byte) 200, (byte) 235, 41, (byte) 239, 53, (byte) 170, 88, 21, 50, 54, 48, 48, 48, 48, 48,
				48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 50, 55, 48, 48, 48, 48, 48, 50, 48, 48, 48, 51, 48, 48, 48, 48,
				48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 48, 49, 48, 56, 48, 49, 48, 52, 48, 48, 48, 49, 69, 65, 51,
				56, 53, 49, 51, 70 };*/

		Map<String, String> message = new HashMap<String, String>();
		message.put("HeaderLength", "46");
		message.put("HeaderFlagandVersion", "82");
		message.put("TotalMessageLength", "0342");
		message.put("DestinationID", "01040001");
		message.put("Blank01", "   ");
		message.put("SourceID", "00010344");
		message.put("Blank02", "   ");
		message.put("ReservedforUse", "000000");
		message.put("BatchNumber", "01");
		message.put("TransactionInformation", "10000000");
		message.put("UserInformation", "00");
		message.put("RejectCode", "00000");
		message.put("mti", "0200");
		message.put("BitMap",
				"11100010001111100110010011000001101010001110000010011000000100000000000000000000000000000000000000010000000000000000000000000001");
		message.put("2", "6259064159691667");
		message.put("3", "300000");
		message.put("7", "0227140120");
		message.put("11", "107485");
		message.put("12", "140120");
		message.put("13", "0227");
		message.put("14", "2510");
		message.put("15", "0527");
		message.put("18", "6011");
		message.put("19", "344");
		message.put("22", "021");
		message.put("25", "02");
		message.put("26", "06");
		message.put("32", "12345678");
		message.put("33", "12345678");
		message.put("35", "6259064159691667=251000000000001");
		message.put("37", "000000006043");
		message.put("41", "00010001");
		message.put("42", "001584054110004");
		message.put("43", "TestBank                 Hongkong    HKG");
		message.put("49", "156");
		message.put("52", "104AD01589EA339B");
		message.put("53", "2600000000000000");
		message.put("60", "027");//占位
		message.put("60.1", "0000");
		message.put("60.2.1", "0");
		message.put("60.2.2", "2");
		message.put("60.2.3", "0");
		message.put("60.2.4", "0");
		message.put("60.2.5", "01");
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
		message.put("60.3.8", "02");
		message.put("100", "01040001");
		message.put("128", "3344393131373546");

		byte[] res;
		res = PackageAdapter.packAdapter(message);

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
		
		byte[] req = new byte[in.readableBytes()];
		in.readBytes(req);
		try {
			Map<String, String> unpackAdapter = PackageAdapter.unpackAdapter(req,"iso8583-DPI.xml");
			Set<Entry<String, String>> entrySet = unpackAdapter.entrySet();
			for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
				Entry<String, String> entry = (Entry<String, String>) iterator.next();
				System.out.println(" " + entry.getKey() + " : " + entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
