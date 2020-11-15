package utils;

import lombok.extern.slf4j.Slf4j;
import utils.threadpool.ThreadPoolFactoryUtil;

@Slf4j
public class CustomShutdownUtil {
    public static void clearAll() {
        log.info("addShutdownHook clear all");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtil.clearRegistry(CuratorUtil.getZkClient());
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
