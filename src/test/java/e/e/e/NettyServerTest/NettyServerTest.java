package e.e.e.NettyServerTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.worldline.isa.socketio.NettyServer;

public class NettyServerTest {
	
	NettyServer server ;
	@Before
	public void setUp() throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		server = (NettyServer) ctx.getBean("nettyServer");
	}

	@Test
	public void testStart() {
		server.start();
	}

	@Test
	public void testStop() {
//		server.stop();
	}

}
