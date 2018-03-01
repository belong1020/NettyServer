package com.worldline.isa.Listener;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;


public interface ExceptionListener {

    void onEventException(Exception e, List<Object> args);

    void onDisconnectException(Exception e);

    void onConnectException(Exception e);

    boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception;

}
