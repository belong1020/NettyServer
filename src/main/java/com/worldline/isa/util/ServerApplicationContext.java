package com.worldline.isa.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ServerApplicationContext {

	private final static ClassPathXmlApplicationContext dubbo_context = new ClassPathXmlApplicationContext("spring-dubbo.xml");

	private final static ApplicationContext spring_context = new ClassPathXmlApplicationContext("applicationContext.xml");
	
	public ServerApplicationContext() {
		if(!dubbo_context.isRunning()) {
			dubbo_context.start();
		}
	}
	
//	@InitBinder
	public void init() {
//		dubbo_context = new ClassPathXmlApplicationContext("spring-dubbo.xml");
	}
	
	public static Object getDubboBean(String Key){
		return dubbo_context.getBean(Key);
	}
	
	public static Object getSpringBean(String Key) {
		return spring_context.getBean(Key);
	}
	
}
