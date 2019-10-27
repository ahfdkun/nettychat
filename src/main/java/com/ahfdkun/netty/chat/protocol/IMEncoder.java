package com.ahfdkun.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(new MessagePack().write(msg));
    }

    public String encode(IMMessage msg) {
        if (msg == null) {
            return null;
        }
        String prex = "[" + msg.getCmd() + "][" + msg.getTime() + "]";
        if (IMP.LOGIN.getName().equals(msg.getCmd()) || IMP.FLOWER.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getSender() + "][" + msg.getTerminal() + "]");
        } else if (IMP.CHAT.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getSender() + "]");
        } else if (IMP.SYSTEM.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getOnline() + "]");
        }

        if (msg.getContent() != null && !"".equals(msg.getContent().trim())) {
            prex += (" - " + msg.getContent());
        }
        return prex;
    }

}
