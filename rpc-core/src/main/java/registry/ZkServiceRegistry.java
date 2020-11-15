package registry;

import org.apache.curator.framework.CuratorFramework;
import utils.CuratorUtil;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry{
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.createPersistentNode(zkClient, servicePath);
    }
}
