package com.th.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import com.th.mallchat.common.websocket.utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

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
//           Optional<String> tokenOptional = Optional.of(urlBuilder)
//                    .map(UrlBuilder::getQuery)      // 获取查询字符串部分（UrlQuery）
//                    .map(k -> k.get("token"))       // 获取名为 token 的参数值
//                    .map(CharSequence::toString);   // 转成 String
//
//            // 如果 token 存在
//           tokenOptional.ifPresent(s ->
//                    NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, s)
//            );
            //如果 URI 包含 query 参数，它就匹配失败
            request.setUri(urlBuilder.getPath().toString());
        }
        super.channelRead(ctx, msg);
    }
}
