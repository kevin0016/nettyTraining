package com.example.demo.nettyclient.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class NettyConfig {
    //读取yml中配置
    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${tcp.url}")
    private String url;

    @Value("${so.keepalive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    @Autowired
    @Qualifier("springProtocolInitializer")
    private StringProtocolInitalizer protocolInitalizer;
    //bootstrap配置
    @SuppressWarnings("unchecked")
    @Bean(name = "bootstrap")
    public Bootstrap bootstrap() {
        //对server端进行一系列的配置
        Bootstrap b = new Bootstrap();
        //把两个工作线程组加进来
        b.group( workerGroup())
                //我要制定使用的NioServerSocketChannel这种类型通道
                .channel(NioSocketChannel.class)
                //一定要使用childHandler 去绑定具体的事件处理器
                .handler(protocolInitalizer)
                /**
                 * 服务器端TCP内核模块维护有2个队列，暂且称之为A，B
                 * 客户端想服务端Connect的时候，会发送带有SYN标志的包（第一次握手）
                 * 服务器收到客户端发来的SYN时，想客户端发送SYN ACK确认（第二次握手）
                 * 此时TCP内核模块把客户端链接加入到A队列中，然后服务器收到客户端发来的ACK时（第三次握手）
                 * TC片内核模块把客户端链接从A队列转移到B队列，链接完成应用程序的accept会返回
                 * 也就是说accept从B队列中取出完成三次握手的链接
                 * A队列和B队列的长度只和是backlog，当A，B队列的长度只和大于backlog时，新链接会被TCP内核拒绝
                 * 所以，如果backlog过小，可能会出现accept链接速度跟不上，A，B队列满了，导致新的客户端无法连接
                 * 要注意的是，backlog对程序的链接数并无影响，backlog影响的只是还没有被accept取出的链接
                 */
                //设置TCP缓冲区
                .option(ChannelOption.SO_BACKLOG,128);
        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for ( ChannelOption option : keySet) {
            b.option(option, tcpChannelOptions.get(option));
        }
        return b;
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort() {
        return new InetSocketAddress(tcpPort);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        return options;
    }

    @Bean(name = "stringEncoder")
    public StringEncoder stringEncoder() {
        return new StringEncoder();
    }

    @Bean(name = "stringDecoder")
    public StringDecoder stringDecoder() {
        return new StringDecoder();
    }

    /**
     * Necessary to make the Value annotations work.
     *
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
