package remoting.transport.netty.server;

import entity.RpcServiceProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import provider.ServiceProvider;
import provider.ServiceProviderImpl;
import remoting.transport.netty.codec.RpcMessageDecoder;
import remoting.transport.netty.codec.RpcMessageEncoder;
import utils.CustomShutdownUtil;
import utils.RuntimeUtil;
import utils.factory.SingletonFactory;
import utils.threadpool.ThreadPoolFactoryUtil;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownUtil.clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //Nagle算法，尽可能发送大数据块，减少网络传输
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //TCP底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //TCP三次握手请求队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(host, PORT).sync();
            // 等待服务监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server: ", e);
        } finally {
            log.error("shutdown boss and worker group");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
