package com.ahfdkun.netty.chat.server.handler;

import com.ahfdkun.netty.chat.server.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // 处理业务逻辑
        // 解析http请求的资源
    }
}
