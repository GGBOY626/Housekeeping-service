package com.jzo2o.market.service;

import cn.hutool.core.thread.ThreadUtil;
import com.jzo2o.redis.model.SyncMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
public class SyncThreadPoolTest {

    @Autowired
    @Qualifier("syncThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testThreadPool() {
        // 循环调用10次线程池的execute方法，从线程池中获取10个线程执行任务
        // i 为当前任务对应的同步队列编号 0-9
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.execute(new RunnableSimple(i));
        }

        // 模拟每间隔5秒后执行第二次定时任务
        ThreadUtil.sleep(5000);

        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.execute(new RunnableSimple(i));
        }

        // 防止主线程提前结束，让其运行2分钟
        ThreadUtil.sleep(2, TimeUnit.MINUTES);
    }

    /**
     * 线程任务类：使用分布式锁保证同一队列同一时刻只被一个线程处理
     */
    public class RunnableSimple implements Runnable {
        private final int index;

        public RunnableSimple(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            String queueName = String.format("QUEUE:COUPON:SEIZE:SYNC:{%s}", index);
            // 锁名称：每个队列只有一个线程处理，锁粒度为队列
            String lockName = String.format("LOCK:QUEUE:COUPON:SEIZE:SYNC:{%s}", index);
            RLock lock = redissonClient.getLock(lockName);

            try {
                // 尝试获取锁：最多等待3秒，锁自动释放时间10秒
                boolean isLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
                if (isLock) {
                    // 从指定队列扫描数据
                    Cursor<Map.Entry<String, Object>> cursor = redisTemplate.opsForHash()
                            .scan(queueName, ScanOptions.scanOptions().count(10).build());

                    List<SyncMessage<Object>> collect = cursor.stream()
                            .map(e -> new SyncMessage<Object>(e.getKey().toString(), e.getValue(), null))
                            .collect(Collectors.toList());

                    // 模拟1号队列数据多，执行耗时10秒
                    if (index == 1) {
                        ThreadUtil.sleep(10000);
                    }

                    log.info("{} 获取 {} 数据 {} 条", Thread.currentThread().getId(), queueName, collect.size());
                    collect.forEach(System.out::println);

                    if (cursor != null) {
                        cursor.close();
                    }
                } else {
                    log.info("!!!!!! {} 获取 {} 队列的锁失败", Thread.currentThread().getId(), queueName);
                }
            } catch (Exception e) {
                log.error("sync error", e);
            } finally {
                if (lock != null && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
