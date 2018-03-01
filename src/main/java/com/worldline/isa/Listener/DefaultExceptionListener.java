package com.worldline.isa.Listener;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;

public class DefaultExceptionListener extends ExceptionListenerAdapter {

    private static final Logger log = org.apache.log4j.Logger.getLogger(DefaultExceptionListener.class);

    @Override
    public void onEventException(Exception e, List<Object> args) {
        log.error(e.getMessage(), e);
    }

    @Override
    public void onDisconnectException(Exception e) {
        log.error(e.getMessage(), e);
    }

    @Override
    public void onConnectException(Exception e) {
        log.error(e.getMessage(), e);
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        log.error(e.getMessage(), e);
        return true;
    }

}
