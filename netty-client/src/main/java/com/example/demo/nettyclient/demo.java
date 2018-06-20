package com.example.demo.nettyclient;

import com.example.demo.nettyclient.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class demo {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);
        Bootstrap b = new Bootstrap();
        //把两个工作线程组加进来
        b.group(workerGroup)
                //我要制定使用的NioServerSocketChannel这种类型通道
                .channel(NioSocketChannel.class)
                //一定要使用childHandler 去绑定具体的事件处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientHandler());
                    }
                });

        ChannelFuture channelFuture = b.connect("127.0.0.1", 8090).sync();
        while (true){
            Scanner sc = new Scanner(System.in);
            String str = sc.next();
            //写缓冲
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(str.getBytes()));

        }
//        channelFuture.channel().closeFuture().sync();
//        workerGroup.shutdownGracefully();
    }
}
