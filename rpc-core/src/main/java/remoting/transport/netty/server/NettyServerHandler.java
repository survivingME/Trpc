package remoting.transport.netty.server;

import enums.CompressTypeEnum;
import enums.SerializationTypeEnum;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import remoting.RpcConstants;
import remoting.dto.RpcMessage;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.handler.RpcRequestHandler;
import utils.factory.SingletonFactory;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RpcMessage) {
                log.info("service receive msg: [{}]", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    RpcMessage rpcMessage = RpcMessage.builder()
                            .codec(SerializationTypeEnum.KYRO.getCode())
                            .compress(CompressTypeEnum.GZIP.getCode())
                            .messageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE)
                            .data(RpcConstants.PONG).build();
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    //execute target method
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    if(ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        RpcMessage rpcMessage = RpcMessage.builder()
                                .codec(SerializationTypeEnum.KYRO.getCode())
                                .compress(CompressTypeEnum.GZIP.getCode())
                                .messageType(RpcConstants.RESPONSE_TYPE)
                                .data(rpcResponse).build();
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail();
                        RpcMessage rpcMessage = RpcMessage.builder()
                                .codec(SerializationTypeEnum.KYRO.getCode())
                                .compress(CompressTypeEnum.GZIP.getCode())
                                .messageType(RpcConstants.RESPONSE_TYPE)
                                .data(rpcResponse).build();
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        log.error("not writable now, message dropped");
                    }
                }
            }
        } finally {
            //release bytebuf
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.READER_IDLE) {
                log.info("idle check happen, close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
