package com.th.mallchat.common.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class NettyWebSocketServer {
    //WebSocket端口
    public static final int WEB_SOCKET_PORT = 8090;
    public static final NettyWebSocketServerHandler NETTY_WEB_SOCKET_SERVER_HANDLER = new NettyWebSocketServerHandler();

    /**
     * 创建线程池执行器
     * bossGroup：处理 连接请求（accept）
     * workerGroup：处理 读写数据（IO读写、事件派发）
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    /**
     * 启动 websocket server
     * @retrun void
     * @throws InterruptedException
     *
     * @PostConstruct 是 Java 标准注解（JSR-250）
     * 用于修饰一个 非静态的 void 方法
     * 表示：当依赖注入完成后自动执行该方法
     * 也就是：Spring Bean 初始化完成后自动调用
     */
    @PostConstruct
    public void start() throws InterruptedException {
        run();
    }

    public void run() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //设置服务器 ServerSocketChannel允许的等待连接队列最大长度为 128
                .option(ChannelOption.SO_BACKLOG,128)
                //启用 TCP 的 KeepAlive 机制：保持长连接，让底层 TCP 定期发送探活包 检测对方是否还在线，避免“假连接”
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //30秒客户端没有向服务器发送消息心跳则关闭连接
//                        pipeline.addLast(new IdleStateHandler(30,0,0));
                        //使用的是HTTP协议 需要HTTP解码器和编码器
                        pipeline.addLast(new HttpServerCodec());
                        // 以块方式写，添加 chunkedWriter 处理器
                        pipeline.addLast(new ChunkedWriteHandler());
                        /**
                         * 说明：
                         *  1. http数据在传输过程中是分段的，比如
                         *  HttpRequest（请求头）
                         * HttpContent（请求体）
                         * LastHttpContent（请求体结尾）
                         * HttpObjectAggregator可以把多个段聚合起来；FullHttpRequest
                         *  2. 这就是为什么当浏览器发送大量数据时，就会发出多次 http请求的原因
                         */
                        pipeline.addLast(new HttpObjectAggregator(8192));
//                        pipeline.addLast(new HttpHeadersHandler());
                        /**
                         * 说明：
                         *  1. 对于 WebSocket，它的数据是以帧frame 的形式传递的；
                         *  2. 可以看到 WebSocketFrame 下面有6个子类
                         *  3. 浏览器发送请求时： ws://localhost:7000/hello 表示请求的uri
                         *  4. WebSocketServerProtocolHandler 核心功能是自动处理 HTTP 升级（Upgrade）请求 并将HTTP协议升级为 WebSocket 协议，保持长连接；
                         *      是通过一个状态码 101 来切换的
                         *      Netty 的 WebSocketServerProtocolHandler 内部已经自动处理好 解决了粘包半包
                         *
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                        //业务逻辑处理Handler
                        pipeline.addLast(NETTY_WEB_SOCKET_SERVER_HANDLER);
                    }
                });
        serverBootstrap.bind(WEB_SOCKET_PORT).sync();
    }


    /**
     * 销毁
     * 它用于 标注一个方法；
     * 表示在 Spring Bean 被销毁之前，自动调用这个方法；
     * 常用于 资源释放、连接关闭、线程池关闭、清理工作等操作。
     */
    @PreDestroy
    public void destroy() {
        Future<?> future = bossGroup.shutdownGracefully();
        Future<?> future1 = workerGroup.shutdownGracefully();
        future.syncUninterruptibly();
        future1.syncUninterruptibly();
        log.info("关闭 websocket server 成功");
    }


}
