package e.e.e.echoclient;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import e.e.e.echoclient.handler.TestClientHandler0001;
import e.e.e.echoclient.handler.TestClientHandler0002;
import e.e.e.echoclient.handler.TestClientHandler0003;
import e.e.e.echoclient.handler.TestClientHandler0004;
import e.e.e.echoclient.handler.TestClientHandler0005;

public class TestClient {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void test() {
	}
	
	@Test
	public void test0001() throws Exception {
		EchoClient client = new EchoClient("localhost", 65535);
		client.setHandler(new TestClientHandler0001());
			client.start();
	}
	
	@Test
	public void test0002() throws Exception {
		EchoClient client = new EchoClient("localhost", 65535);
		client.setHandler(new TestClientHandler0002());
			client.start();
	}
	
	@Test
	public void test0003() throws Exception {
		EchoClient client = new EchoClient("localhost", 65535);
		client.setHandler(new TestClientHandler0003());
			client.start();
	}
	
	@Test
	public void test0004() throws Exception {
		EchoClient client = new EchoClient("localhost", 65535);
		client.setHandler(new TestClientHandler0004());
			client.start();
	}
	
	@Test
	public void test0005() throws Exception {
		EchoClient client = new EchoClient("localhost", 65535);
		client.setHandler(new TestClientHandler0005());
			client.start();
	}
	
	
	@Test
	public void testThread() {
		Thread[] tr = new Thread[10];
		for (int i = 0; i < tr.length; i++) {
			tr[i] = new Thread(() -> {
				try {
					new EchoClient("localhost", 65535).setHandler(new TestClientHandler0005()).start();
				} catch (Exception e) {
				}
			});
		}

		for (int i = 0; i < tr.length; i++) {
			tr[i].start();
		}
	}
	
	
	
	
	
}
