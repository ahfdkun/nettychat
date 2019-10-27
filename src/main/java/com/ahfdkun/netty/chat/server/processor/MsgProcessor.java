package com.ahfdkun.netty.chat.server.processor;

import com.ahfdkun.netty.chat.protocol.IMDecoder;
import com.ahfdkun.netty.chat.protocol.IMEncoder;
import com.ahfdkun.netty.chat.protocol.IMMessage;
import com.ahfdkun.netty.chat.protocol.IMP;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MsgProcessor {

    // 存储用户的容器
    private static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 自定义扩展属性
    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    public static final AttributeKey<String> FROM = AttributeKey.valueOf("from");

    private IMDecoder decoder = new IMDecoder();
    private IMEncoder encoder = new IMEncoder();

    public void process(Channel client, IMMessage msg) {
        process(client, encoder.encode(msg));
    }

    public void process(Channel client, String msg) {
        // 统一这里处理逻辑

        IMMessage request = decoder.decode(msg);

        if (request == null) {
            return;
        }

        if (request.getCmd().equals(IMP.LOGIN.getName())) {
            // 保存用户信息
            client.attr(NICK_NAME).getAndSet(request.getSender());
            client.attr(IP_ADDR).getAndSet(getAddress(client));
            client.attr(FROM).getAndSet(request.getTerminal());
            // 把这个用户保存到一个统一容器中，要给所有在线的推送消息
            onlineUsers.add(client);

            // 通知所有在线用户此用户上线了
            for (Channel channel : onlineUsers) {
                if (channel == client) {
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), getNickName(client));
                } else {
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), "已与服务端建立连接");
                }

                // 消息准备好了 准备推送消息
                // 如果终端是控制台，推送MMessage
                if ("Console".equals(channel.attr(FROM).get())) {
                    channel.writeAndFlush(request);
                    continue;
                }
                // 否则推送WebSocket自定义协议
                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        } else if (request.getCmd().equals(IMP.CHAT.getName())) {

        } else if (request.getCmd().equals(IMP.FLOWER.getName())) {

        }
    }

    private String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    private long sysTime() {
        return System.currentTimeMillis();
    }

    private String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }

    private JSONObject getAttrs(Channel client) {
        return client.attr(ATTRS).get();
    }

    private void setAttrs(Channel client, String key, Object value) {
        JSONObject jsonObject = client.attr(ATTRS).get();
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        jsonObject.put(key, value);
        client.attr(ATTRS).set(jsonObject);
    }

    public void logout(Channel client) {
        if (getNickName(client) == null) {
            return;
        }
        for (Channel channel : onlineUsers) {
            IMMessage request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), getNickName(client) + "离开");
            String content = encoder.encode(request);
            channel.writeAndFlush(new TextWebSocketFrame(content));
        }
        onlineUsers.remove(client);
    }

}
