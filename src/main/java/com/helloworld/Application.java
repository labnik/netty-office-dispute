package com.helloworld;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Application {


    public static final int HTTP_PORT = 9090;

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup boss = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
        EventLoopGroup workers = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);

        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(boss, workers)
                    .childHandler(new HttpServerInitializer())
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(HTTP_PORT).sync();
            f.channel().closeFuture().sync();
            System.out.println("Server started in port: " + HTTP_PORT);
        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }

    public static class HttpServerInitializer extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new HttpServerCodec(), new HttpServerHandler());
        }
    }

    public static class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";

        private final StringBuilder buf = new StringBuilder();

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {


            if (msg instanceof HttpRequest) {
                buf.setLength(0);
                QueryStringDecoder decoder = new QueryStringDecoder(((HttpRequest) msg).uri());
                final double a = Double.valueOf(decoder.parameters().getOrDefault("a", List.of("1")).get(0));
                final double b = Double.valueOf(decoder.parameters().getOrDefault("b", List.of("1")).get(0));
                buf.append(a / b);
            }

            if (msg instanceof LastHttpContent) {

                ByteBuf content = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                response.headers().set(CONTENT_TYPE, "text/plain");
                response.headers().set(CONTENT_LENGTH, String.valueOf(content.readableBytes()));
                ctx.writeAndFlush(response);
            }
        }
    }
}
