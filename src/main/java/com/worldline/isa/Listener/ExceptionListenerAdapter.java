package com.worldline.isa.Listener;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;

/**
 * Base callback exceptions listener
 *
 *
 */
public abstract class ExceptionListenerAdapter implements ExceptionListener {

    @Override
    public void onEventException(Exception e, List<Object> data) {
    }

    @Override
    public void onDisconnectException(Exception e) {
    }

    @Override
    public void onConnectException(Exception e) {
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        return false;
    }

}
