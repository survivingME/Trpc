package remoting.transport.netty.client;

import enums.CompressTypeEnum;
import enums.SerializationTypeEnum;
import extension.ExtensionLoader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import registry.ServiceDiscovery;
import remoting.ClientTransport;
import remoting.RpcConstants;
import remoting.dto.RpcMessage;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import utils.factory.SingletonFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class NettyClientTransport implements ClientTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;

    public NettyClientTransport() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return rpcResponse future
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // get service name
        String rpcServiceName = rpcRequest.toRpcProperties().getServiceName();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        //get channel of server address
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel != null && channel.isActive()) {
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
               if(future.isSuccess()) {
                   log.info("client has sent message: [{}]", rpcMessage);
               } else {
                   future.channel().close();
                   resultFuture.completeExceptionally(future.cause());
                   log.error("Send failed: ", future.cause());
               }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }
}
