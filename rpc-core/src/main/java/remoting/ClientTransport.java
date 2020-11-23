package remoting;

import extension.SPI;
import remoting.dto.RpcRequest;

@SPI
public interface ClientTransport {
    /**
     * send rpc request to server and get result
     * @param rpcRequest
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
