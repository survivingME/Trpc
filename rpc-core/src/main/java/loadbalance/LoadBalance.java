package loadbalance;

import extension.SPI;

import java.util.List;

@SPI
public interface LoadBalance {
    /**
     * choose one node from service address
     * @param serviceAddress
     * @param rpcServiceName
     * @return
     */
    String selectServiceAddress(List<String> serviceAddress, String rpcServiceName);
}
