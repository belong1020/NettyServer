package com.worldline.isa.Listener;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.remoting.RemotingException;

/**
 * <p>框架运行时异常.</p>
 *
 * @author Magic Joey
 * @version Simple8583Exception.java 1.0 Created@2015-06-15 22:18 $
 */
public class Simple8583Exception extends RuntimeException{

    public Simple8583Exception() {
        super();
    }

    public Simple8583Exception(String s) {
        super(s);
    }

    public Simple8583Exception(String s, Throwable throwable) {
        super(s, throwable);
    }

    public Simple8583Exception(Throwable throwable) {
        super(throwable);
    }
    
}
