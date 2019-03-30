package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.Const;
import com.geekhalo.redpackage.application.RedPackageApplication;
import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.RedPackageRepository;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class RedPackageApplicationV5
        extends AbstractRedPackageAppliction
        implements RedPackageApplication, SmartLifecycle {
    private static final int DUMP_THREAD_THREAD = 2;
    private static final int LOADER_THREAD = 1;

    private static final int BATCH_SIZE = 500;

    private static final String RED_PACKAGE_QUEUE_KEY = "red.package.batch.queue.%s";

    @Autowired
    private RedPackageRepository redPackageRepository;

    @Autowired
    private RedisTemplate<String, List<RedPackage>> redisTemplate;

    @Autowired
    private RedisTemplate<String, UserRedPackage> userRedPackageRedisTemplate;

    private final BlockingQueue<UserRedPackage> localUserRedPackageQueue = new LinkedBlockingQueue<>(BATCH_SIZE);

    private final ConcurrentMap<String, RedPackageLoader> redPackageLoaderMap = new ConcurrentHashMap<>();

    private ExecutorService executorService;


    @Override
    public RedPackageActivity createActivity(int total, int size) {
        RedPackageActivity redPackageActivity = new RedPackageActivity();
        redPackageActivity.init(total, size);

        while (redPackageActivity.hasBalance()){
            RedPackageCollector collector = new RedPackageCollector();
            redPackageActivity.draw(BATCH_SIZE, collector);
            List<RedPackage> redPackages = collector.redPackages.stream()
                    .filter(redPackage -> redPackage != null)
                    .collect(Collectors.toList());
            save(redPackageActivity.getId(), redPackages);
        }
        return redPackageActivity;
    }

    class RedPackageCollector implements Consumer<RedPackage> {
        private final List<RedPackage> redPackages = Lists.newArrayList();
        @Override
        public void accept(RedPackage redPackage) {
            if (redPackage != null) {
                redPackages.add(redPackage);
            }
        }
    }

    private void save(String activityId, List<RedPackage> redPackages){
        // RedPackage 入库
        this.redPackageRepository.save(redPackages);
        // 入队列
        String queueKey = String.format(RED_PACKAGE_QUEUE_KEY, activityId);
        this.redisTemplate.boundListOps(queueKey).leftPush(redPackages);
    }

    @Override
    public UserRedPackage draw(String activityId, String userId) {
        // 从加载器中获取本地 RedPackage
        RedPackageLoader loader = this.redPackageLoaderMap.computeIfAbsent(activityId, key->new RedPackageLoader(String.format(RED_PACKAGE_QUEUE_KEY, key)));
        RedPackage redPackage = loader.getRedPackage();
        if (redPackage == null){
            return null;
        }
        UserRedPackage userRedPackage = new UserRedPackage();
        userRedPackage.setActivityId(activityId);
        userRedPackage.setUserId(userId);
        userRedPackage.setRedPackageId(redPackage.getId());
        userRedPackage.setAmount(redPackage.getAmount());

        try {
            // 将 UserRedPackage 提交到本地队列
            this.localUserRedPackageQueue.put(userRedPackage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userRedPackage;
    }




    @Override
    public void start() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("user-red-package-dump-thread-%d")
                .daemon(true)
                .build();
        executorService = Executors.newFixedThreadPool(DUMP_THREAD_THREAD, basicThreadFactory);
        for (int i = 0; i< DUMP_THREAD_THREAD; i++){
            executorService.submit(new UserRedPackageDumpTask());
        }
    }

    @Override
    public void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private class UserRedPackageDumpTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                List<UserRedPackage> userRedPackages = new ArrayList<>(BATCH_SIZE);
                // 将本地队列中的数据，读取到 List 中
                localUserRedPackageQueue.drainTo(userRedPackages, BATCH_SIZE);
                if (!CollectionUtils.isEmpty(userRedPackages)) {
                    // 批量提交数据
                    userRedPackageRedisTemplate.boundListOps(Const.USER_RED_PACKAGE_DUMP_QUEUE).leftPushAll(userRedPackages.toArray(new UserRedPackage[userRedPackages.size()]));
                }
            }
        }
    }


    class RedPackageLoader{
        private final String indexQueueKey;
        private final BlockingQueue<RedPackage> redPackages = new LinkedBlockingQueue<>(BATCH_SIZE);
        private final AtomicBoolean hasRedPackage = new AtomicBoolean(true);
        private final ExecutorService executorService;

        RedPackageLoader(String indexQueueKey) {
            // 创建后台线程
            this.indexQueueKey = indexQueueKey;
            BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
                    .namingPattern("red-package-loader-Thread-%d")
                    .daemon(true)
                    .build();
            this.executorService = Executors.newFixedThreadPool(LOADER_THREAD, threadFactory);
            for (int i = 0; i< LOADER_THREAD; i++){
                this.executorService.submit(new RedPackageLoadTask(this.indexQueueKey, redPackages, hasRedPackage));
            }
            this.executorService.shutdown();
        }

        /**
         * 获取本地 RedPackage
         * @return
         */
        public RedPackage getRedPackage(){
            // 如何还有红包，或者本地队列不为空，从本地队列中获取红包
            if (hasRedPackage.get() || !this.redPackages.isEmpty()){
                try {
                    // 从本地 Queue 中获取 RedPackage
                    return redPackages.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * RedPacke 批量加载任务
         */
        private class RedPackageLoadTask implements Runnable {
            private final String queueKey;
            private final BlockingQueue<RedPackage> redPackages;
            private final AtomicBoolean hasNext;
            public RedPackageLoadTask(String queueKey, BlockingQueue<RedPackage> redPackages, AtomicBoolean hasRedPackage) {
                this.queueKey = queueKey;
                this.redPackages = redPackages;
                this.hasNext = hasRedPackage;
            }

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    // 从队列中获取 List<RedPackage>
                    List<RedPackage> redPackages = redisTemplate.boundListOps(queueKey).rightPop();
                    // 已经没有红包
                    if (redPackages == null){
                        hasNext.set(false);
                        return;
                    }
                    // 遍历所有红包，将其放入本地队列中
                    for (RedPackage o : redPackages){
                        try {
                            this.redPackages.put(o);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        }
    }


}
