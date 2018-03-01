package e.e.e.echoclient;

import java.net.InetSocketAddress;

import e.e.e.echoclient.handler.TestClientHandler0001;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 代码清单 2-4 客户端的主类
 * 
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoClient {
	private final String host;
	private final int port;
	private SimpleChannelInboundHandler handler;
	

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public EchoClient setHandler(SimpleChannelInboundHandler handler) {
		this.handler = handler;
		return this;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 创建 Bootstrap
			Bootstrap b = new Bootstrap();
			// 指定 EventLoopGroup 以处理客户端事件；需要适用于 NIO 的实现
			b.group(group)
					// 适用于 NIO 传输的Channel 类型
					.channel(NioSocketChannel.class)
					// 设置服务器的InetSocketAddress
					.remoteAddress(new InetSocketAddress(host, port))
					// 在创建Channel时，向 ChannelPipeline中添加一个 EchoClientHandler实例
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(handler);
						}
					});
			// 连接到远程节点，阻塞等待直到连接完成
			ChannelFuture f = b.connect().sync();
			// 阻塞，直到Channel 关闭
			f.channel().closeFuture().sync();
		} finally {
			// 关闭线程池并且释放所有的资源
			group.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
//		NettyServerConfig config = NettyServerConfig.getInstance();
		
		Thread[] tr = new Thread[1];
		for (int i = 0; i < tr.length; i++) {
			tr[i] = new Thread(() -> {
				try {
					new EchoClient("localhost", 65535).start();
				} catch (Exception e) {
				}
			});
		}

		for (int i = 0; i < tr.length; i++) {
			tr[i].start();
		}

	}
}
