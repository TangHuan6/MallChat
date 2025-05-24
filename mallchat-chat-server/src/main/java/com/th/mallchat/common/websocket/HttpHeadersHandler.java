package com.th.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import com.th.mallchat.common.websocket.utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpHeadersHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());
            CharSequence charSequence = urlBuilder.getQuery().get("token");
            if (charSequence != null) {
                String token = charSequence.toString();
                NettyUtil.setAttr(ctx.channel(),NettyUtil.TOKEN,token);
            }

            //如果 URI 包含 query 参数，它就匹配失败
            request.setUri(urlBuilder.getPath().toString());
            //取用户ip
            /**
             * "X-Real-IP" 是很多 Nginx 或反向代理服务器设置的一个自定义头，用于标识原始客户端的 IP 地址，防止客户端真实 IP 被代理服务器替代。
             */
            String ip = request.headers().get("X-Real-IP");
            if (StringUtils.isBlank(ip)){
                InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
                System.out.println("ip:"+ip);
            }
            NettyUtil.setAttr(ctx.channel(),NettyUtil.IP,ip);
            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }
}


//           Optional<String> tokenOptional = Optional.of(urlBuilder)
//                    .map(UrlBuilder::getQuery)      // 获取查询字符串部分（UrlQuery）
//                    .map(k -> k.get("token"))       // 获取名为 token 的参数值
//                    .map(CharSequence::toString);   // 转成 String
//
//            // 如果 token 存在
//           tokenOptional.ifPresent(s ->
//                    NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, s)
//            );
