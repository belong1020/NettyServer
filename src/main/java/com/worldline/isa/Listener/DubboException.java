package com.worldline.isa.Listener;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.remoting.RemotingException;

@ControllerAdvice
public class DubboException {
	
	Logger logger = Logger.getLogger(DubboException.class);
	
	@ExceptionHandler( RemotingException.class )
//    @ResponseBody
    public String RemotingException(Exception e) {
    	logger.error(e.getStackTrace());
		return "can't connection Dubbo service. ";
    }
}
