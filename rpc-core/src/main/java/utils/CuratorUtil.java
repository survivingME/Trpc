package utils;

import enums.RpcConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CuratorUtil {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static String defaultZookeeperAddress = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";

    private CuratorUtil() {}

    /**
     * @return a started zkClient
     */
    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFilesUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        //check if user has set zk address
        if(properties != null) defaultZookeeperAddress = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        //if zkClient has been started, return directly
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) return zkClient;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(defaultZookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * create persistent nodes, not removed when the client disconnects
     * @param zkClient
     * @param path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is [{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * get children node under param
     * @param zkClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);

        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail: ", servicePath);
        }
        return result;
    }

    /**
     * register listener for changes of node
     * @param rpcServiceName
     * @param zkClient
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
            }
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * empty data
     * @param zkClient
     */
    public static void clearRegistry(CuratorFramework zkClient) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail: ", p);
            }
        });
        log.info("All registered services on the server are cleared: [{}]", REGISTERED_PATH_SET.toString());
    }
}
