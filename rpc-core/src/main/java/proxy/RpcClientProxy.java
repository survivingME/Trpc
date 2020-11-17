package proxy;


import entity.RpcServiceProperties;
import enums.RpcErrorMessageEnum;
import enums.RpcResponseCodeEnum;
import exception.RpcException;
import io.netty.util.concurrent.CompleteFuture;
import lombok.extern.slf4j.Slf4j;
import remoting.ClientTransport;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.transport.netty.client.NettyClientTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String INTERFACE_NAME = "interfaceName";

    private final ClientTransport clientTransport;
    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy(ClientTransport clientTransport, RpcServiceProperties rpcServiceProperties) {
        this.clientTransport = clientTransport;
        if(rpcServiceProperties.getGroup() == null) {
            rpcServiceProperties.setGroup("");
        }
        if(rpcServiceProperties.getVersion() == null) {
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    /**
     * get proxy object
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }


    /**
     * rpc client call this method to connect the netty server
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoke method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion()).build();
        RpcResponse<Object> rpcResponse = null;
        if(clientTransport instanceof NettyClientTransport) {
            CompleteFuture<RpcResponse<Object>> completeFuture = (CompleteFuture<RpcResponse<Object>>) clientTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completeFuture.get();
        }
        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if(rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + " : " + rpcRequest.getInterfaceName());
        }
        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + " : " + rpcRequest.getInterfaceName());
        }
        if(rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + " : " + rpcRequest.getInterfaceName());
        }
    }
}
