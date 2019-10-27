package com.ahfdkun.netty.chat.server.handler;

import com.ahfdkun.netty.chat.protocol.IMMessage;
import com.ahfdkun.netty.chat.server.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TerminalServerHandler extends SimpleChannelInboundHandler<IMMessage> {

    private MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        processor.process(ctx.channel(), msg);
    }

}
