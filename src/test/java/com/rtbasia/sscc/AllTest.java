package com.rtbasia.sscc;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class AllTest {

    @Test
    public void testGet() {
        Cache cache = new Cache();

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public String readFromCache(String key) {
                return "value";
            }

            @Override
            public String readFromDB(String key) {
                return null;
            }

            @Override
            public void setCache(String key, String obj) {

            }
        }, 1000);

        Assert.assertEquals("value", value);
    }

    @Test
    public void testGetTimeout() {
        Cache cache = new Cache();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        cache.writeAsync(new WriteOperation() {
            @Override
            public void writeDB() {
                // 模拟写数据库延时
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void clearCache(String key) {
            }

            @Override
            public String key() {
                return "test-key";
            }
        });

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public void setCache(String key, String obj) {
                countDownLatch.countDown(); // 写缓存完成
            }

            @Override
            public String readFromCache(String key) {
                if (countDownLatch.getCount() == 0) {
                    return "cache value";
                } else {
                    return null;
                }
            }

            @Override
            public String readFromDB(String key) {
                return "db value";
            }
        }, 1000);

        Assert.assertEquals("db value", value);
    }

    @Test
    public void testGetAfterWrite() {
        Cache cache = new Cache();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        cache.writeAsync(new WriteOperation() {
            @Override
            public void writeDB() {
                try {
                    Thread.sleep(1000);
                    System.out.println("写完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void clearCache(String key) {
            }

            @Override
            public String key() {
                return "test-key";
            }
        });

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public String readFromCache(String key) {
                // 模拟写操作未完成时取得为空值的情况
                if (countDownLatch.getCount() > 0) {
                    System.out.println("读缓存为空，等待重试");
                    return null;
                } else {
                    System.out.println("读到缓存啦");
                    return "value";
                }
            }

            @Override
            public String readFromDB(String key) {
                return "new db value";
            }

            @Override
            public void setCache(String key, String obj) {
                countDownLatch.countDown();
            }
        }, 5000);

        Assert.assertEquals("value", value);
    }
}
