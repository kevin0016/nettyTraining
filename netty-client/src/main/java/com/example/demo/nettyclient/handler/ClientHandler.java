package com.example.demo.nettyclient.handler;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
//@Component
//@Qualifier("clientHandler")
//@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        log.info("server msg:" + msg);
        String clientIdToLong = ctx.channel().id().asLongText();
        log.info("server long id:" + clientIdToLong);
        String clientIdToShort = ctx.channel().id().asShortText();
        log.info("server short id:" + clientIdToShort);
        if (msg.indexOf("bye") != -1) {
            //close
            ctx.channel().close();
        } else {
            //send to client
            ctx.channel().writeAndFlush("server msg is:" + msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
        ctx.channel().writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }


}
