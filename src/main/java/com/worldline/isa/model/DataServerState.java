package com.worldline.isa.model;

import java.util.Map;

import org.jboss.netty.channel.ChannelLocal;

import io.netty.channel.Channel;

/**
 * org.jboss.netty 中数据交换组件Local 类似ThreadLocal
 * 
 * @author Belong.
 */
@Deprecated
public final class DataServerState {

    public static final ChannelLocal<Boolean> loggedIn = new ChannelLocal() {
        protected Map<String, String> initialValue(Channel channel) {
            return null;
        }
    };
    
    
    
}