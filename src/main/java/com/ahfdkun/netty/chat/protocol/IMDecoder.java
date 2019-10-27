package com.ahfdkun.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IMDecoder extends ByteToMessageDecoder {

    private Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s-\\s(.*))?");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final int length = in.readableBytes();
        final byte[] array = new byte[length];
        // 网络传输过来的内容，变成字符串
        String content = new String(array, in.readerIndex(), length);

        // 空消息不解析
        if (!"".equals(content.trim())) {
            if (!IMP.isIMP(content)) {
                ctx.channel().pipeline().remove(this);
                return;
            }
        }

        // 把字符串变成我们能识别出来的IMMessage对象
        in.getBytes(in.readerIndex(), array, 0, length);

        // 利用序列化框架，将网络流信息直接转化为IMMessage对象
        out.add(new MessagePack().read(array, IMMessage.class));

        in.clear();
    }

    // [LOGIN][123123123][WebSocket][消息体]
    public IMMessage decode(String msg) {
        if (msg == null || "".equals(msg.trim())) {
            return null;
        }
        Matcher m = pattern.matcher(msg);

        String header = null, content = null;
        if (m.matches()) {
            header = m.group(1);
            content = m.group(3);
        }
        String[] headers = header.split("\\]\\[");

        long time = Long.parseLong(headers[1]);
        String sender = headers[2];

        if (msg.startsWith("[" + IMP.LOGIN.getName() + "]")) {
            return new IMMessage(headers[0], headers[3], time, sender);
        } else if (msg.startsWith("[" + IMP.CHAT.getName() + "]")) {
            return new IMMessage(headers[0], time, sender, content);
        } else if (msg.startsWith("[" + IMP.FLOWER.getName() + "]")) {
            return new IMMessage(headers[0], headers[3], time, sender);
        } else {
            return null;
        }

    }
}

