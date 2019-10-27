package com.ahfdkun.netty.chat.server;

import com.ahfdkun.netty.chat.protocol.IMDecoder;
import com.ahfdkun.netty.chat.protocol.IMEncoder;
import com.ahfdkun.netty.chat.server.handler.HttpServerHandler;
import com.ahfdkun.netty.chat.server.handler.TerminalServerHandler;
import com.ahfdkun.netty.chat.server.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.rmi.registry.RegistryHandler;

public class ChatServer {

    private int port = 8080;

    // 启动类
    public void start() throws InterruptedException {
        // ServerSocket / ServerSocketChannel
        // Selector主线程，Work线程

        // 初始化主线程池 Selector
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 子线程池初始化，具体对应客户端的处理逻辑
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 在Netty中，把所有的业务逻辑全部归总到一个队列中
                        // 这个队列包含了各种各样的处理逻辑，对这些处理逻辑在Netty中有一个封装
                        // 封装成了一个对象，无锁化串行任务队列
                        // Pipeline
                        ChannelPipeline pipeline = ch.pipeline();

                        // 解析自定义协议
                        pipeline.addLast(new IMDecoder()); // Inbound
                        pipeline.addLast(new IMEncoder()); // Outbound
                        // 专门用来处理直接发送IMMessage对象到IDE控制台
                        pipeline.addLast(new TerminalServerHandler()); // Inbound

                        // 解析Http请求
                        pipeline.addLast(new HttpServerCodec()); // Outbound
                        // 将同一个http请求或响应的多个消息对象变成一个fullHttpRequest完整的消息对象
                        pipeline.addLast(new HttpObjectAggregator(64 * 1024)); // Inbound
                        // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输会撑爆JVM内存，加上这个handler
                        pipeline.addLast(new ChunkedWriteHandler()); // Inbound、Outbound
                        // 处理Web页面
                        pipeline.addLast(new HttpServerHandler()); // Inbound

                        // 解析WebSocket请求
                        pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                        // 用来处理WebSocket通信协议
                        pipeline.addLast(new WebSocketServerHandler()); // Inbound
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // 正式启动服务，相当于用一个死循环开始轮询
        ChannelFuture future = server.bind(this.port).sync();
        System.out.println("RPC Registry start listen at " + this.port);
        future.channel().closeFuture().sync();
    }

}
