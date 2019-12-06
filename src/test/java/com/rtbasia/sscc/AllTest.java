package com.rtbasia.sscc;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@SpringBootTest
public class AllTest {

    @Test
    public void testGet() throws InterruptedException, ReadTimeoutException, ExecutionException {
        Cache cache = new Cache();

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public Object readFromDB(String key) {
                return null;
            }

            @Override
            public String readFromCache(String key) {
                return "value";
            }
        }, 1000);

        Assert.assertEquals("value", value);
    }

    @Test(expected = ReadTimeoutException.class)
    public void testGetTimeout() throws InterruptedException, ReadTimeoutException, ExecutionException {
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
            public void setCache(String key, Object obj) {
                countDownLatch.countDown(); // 模拟写入数据库后更新缓存
            }

            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public Object readFromDB(String key) {
                return null;
            }
        });

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public Object readFromDB(String key) {
                return null;
            }

            @Override
            public String readFromCache(String key) {
                // 模拟写操作未完成时取得为空值的情况
                if (countDownLatch.getCount() > 0) {
                    return null;
                } else {
                    return "value";
                }
            }
        }, 1000);

        Assert.assertEquals("value", value);
    }

    @Test
    public void testGetAfterWrite() throws InterruptedException, ReadTimeoutException, ExecutionException {
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
            public void setCache(String key, Object obj) {
                countDownLatch.countDown();
            }

            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public Object readFromDB(String key) {
                return null;
            }
        });

        String value = cache.get(new ReadOperation<String>() {
            @Override
            public String key() {
                return "test-key";
            }

            @Override
            public Object readFromDB(String key) {
                return null;
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
        }, 5000);

        Assert.assertEquals("value", value);
    }
}
