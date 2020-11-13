package remoting;

import remoting.dto.RpcRequest;

public interface ClientTransport {
    /**
     * send rpc request to server and get result
     * @param rpcRequest
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
