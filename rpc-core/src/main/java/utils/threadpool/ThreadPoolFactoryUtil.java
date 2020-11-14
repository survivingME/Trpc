package utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolFactoryUtil {
    /**
     * 通过threadNamePrefix区分不同线程池，做缓存进行加速
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {}

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(threadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(ThreadPoolConfig threadPoolConfig,
                                                                 String threadNamePrefix, Boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix,
                k -> createThreadPool(threadPoolConfig, threadNamePrefix, daemon));
        if(threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool =  createThreadPool(threadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    private static ExecutorService createThreadPool(ThreadPoolConfig threadPoolConfig,
                                                    String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(), threadPoolConfig.getUnit(), threadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建Thread factory， 如果需要使用线程名称前缀则使用ThreadFactoryBuilder, 否则使用defaultThreadFactory
     * @param threadNamePrefix
     * @param daemon
     * @return
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if(threadNamePrefix != null) {
            if(daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 打印线程池的状态
     *
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
