package com.worldline.isa.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.InitBinder;

@Configuration
public class ServerApplicationContext {

	private final static ClassPathXmlApplicationContext dubbo_context = new ClassPathXmlApplicationContext("spring-dubbo.xml");

//	@InitBinder
	public void init() {
//		dubbo_context = new ClassPathXmlApplicationContext("spring-dubbo.xml");
	}
	
	@InitBinder
	public static void start() {
		if(!dubbo_context.isRunning()) {
			dubbo_context.start();
		}
	}
	
	private static Object getBean(Object Key){
		if (Key instanceof String) {
			return dubbo_context.getBean((String) Key);
		} else if (Key instanceof Class) {
			return dubbo_context.getBean((Class) Key);
		}
		return "Error";
	}
	
	public static ClassPathXmlApplicationContext getDubboContext() {
		if(dubbo_context.isRunning()) {
			return dubbo_context;
		}
		return null;
	}
	
}
