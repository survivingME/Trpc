package registry;

import extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    /**
     * look up service by rpcServiceName
     * @param rpcServiceName
     * @return
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
