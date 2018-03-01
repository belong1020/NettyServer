package com.worldline.isa.bootstrap;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.worldline.isa.socketio.NettyServer;

/**
 * Netty Server bootstrap class
 * 
 * @author Belong.
 */
public class ServerBootstrap{

	public static Thread t1 ;
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("applicationContext.xml");
        NettyServer nettyServer=(NettyServer) ctx.getBean("nettyServer");
        
		nettyServer.start();
		
//		nettyServer.stop();
		
	}
	
	@Test
	public static void InitThread(){
		t1 = new Thread(() -> {
			try {
//				NettyServer.main(new String[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Test
	public static void startServer() {
		t1.start();

	}
	
	@Test
	public static void stop(){
		
	}
	
	
}
