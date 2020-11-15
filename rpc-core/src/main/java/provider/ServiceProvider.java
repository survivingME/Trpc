package provider;

import entity.RpcServiceProperties;

public interface ServiceProvider {
    /**
     *
     * @param service
     * @param serviceClass
     * @param rpcServiceProperties
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     *
     * @param rpcServiceProperties
     * @return
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     *
     * @param service
     * @param rpcServiceProperties
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     *
     * @param service
     */
    void publishService(Object service);
}
