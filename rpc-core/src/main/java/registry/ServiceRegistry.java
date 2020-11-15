package registry;

import extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {
    /**
     * register service
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
