package proxy;


import entity.RpcServiceProperties;
import lombok.extern.slf4j.Slf4j;
import remoting.ClientTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String interfaceName = "interfaceName";

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


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
