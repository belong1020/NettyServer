package com.worldline.isa.bootstrap;

import com.worldline.isa.socketio.NettyServer;
import com.worldline.isa.util.ServerApplicationContext;

/**
 * Netty Server bootstrap class
 * 
 * @author Belong.
 */
public class ServerBootstrap{

	public static Thread t1 ;
	
	public static void main(String[] args) {
        NettyServer nettyServer=(NettyServer) ServerApplicationContext.getSpringBean("nettyServer");
        
		nettyServer.start();
		
//		System.out.println("llllllll");
//		nettyServer.stop();
		
	}
	
	public static void InitThread(){
		t1 = new Thread(() -> {
			try {
//				NettyServer.main(new String[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void startServer() {
		t1.start();

	}
	
	
	
}
