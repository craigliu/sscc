package com.rtbasia.sscc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class PerformanceTest {
    ConnectionPool connectionPool = new ConnectionPool();
    JedisSSCCache cache = new JedisSSCCache("localhost", 6379);
    ObjectMapper objectMapper = new ObjectMapper();

    static int RECORDS_COUNT = 1000;
    static int WR_TIMES = 10000;

    @Before
    public void setup() throws SQLException, JsonProcessingException {
        //1. 准备数据，向缓存和数据库中插入10000条数据。
        String sqlTpl = "insert into cache(key, value) values (%s, %s)";

        System.out.println("开始插入测试数据...");

        try (Connection conn = connectionPool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("insert into cache(`key`, value) values (?, ?)");

            for (int i = 0; i < RECORDS_COUNT; i++) {
                String key = "key_" + i;

                SampleObject object = new SampleObject(key, i);

                String jsonValue = objectMapper.writeValueAsString(object);

                statement.setString(1, key);
                statement.setString(2, jsonValue);

                statement.executeUpdate();

                cache.writeRaw(key, jsonValue);
            }
        }

        System.out.println("测试数据插入完成...");
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection conn = connectionPool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("delete from cache");

            statement.executeUpdate();
        }

        System.out.println("测试数据清理完成...");
    }

    @Test
    public void performanceTest() throws InterruptedException {
        // 模拟10个线程随机读和随机写，取和读的比例在5:1
        int threadNum = 10;

        CountDownLatch latch = new CountDownLatch(threadNum);
        Random rd = new Random();

        long startTs = Instant.now().toEpochMilli();

        AtomicInteger dbReadCount = new AtomicInteger();

        for (int i = 0; i < threadNum; i ++) {
            Thread thread = new Thread(() -> {
                System.out.println("线程开始 ...");

                int rounds = 0;

                try {
                    while (rounds < WR_TIMES) {
                        rounds++;

                        int randomNumber = rd.nextInt(5);

                        int keyIdx = rd.nextInt(RECORDS_COUNT - 1);
                        String key = "key_" + keyIdx;

                        // 1. 写数据
                        if (randomNumber == 0) {
                            try {
                                cache.write(key, SampleObject.class, () -> {
                                    SampleObject newObj = new SampleObject(key, 0);

                                    try(Connection conn = connectionPool.getConnection()) {
                                        PreparedStatement statement = conn.prepareStatement("update cache set value = ? where `key` = ?");
                                        statement.setString(1, objectMapper.writeValueAsString(newObj));
                                        statement.setString(2, key);
                                        statement.executeUpdate();
                                    } catch (SQLException | JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // 2. 读数据
                            cache.get(key, SampleObject.class, key1 -> {
                                try(Connection conn = connectionPool.getConnection()) {
                                    PreparedStatement statement = conn.prepareStatement("select value from cache where `key` = ?");

                                    statement.setString(1, key1);
                                    ResultSet rs =  statement.executeQuery();

                                    dbReadCount.incrementAndGet();

                                    if (rs.next()) {
                                        String json = rs.getString("value");

                                        return objectMapper.readValue(json, SampleObject.class);
                                    }
                                } catch (SQLException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }, 50);
                        }
                    }
                } finally {
                    // 线程结束
                    System.out.println("线程结束 ...");
                    latch.countDown();
                }
            });

            thread.start();
        }

        latch.await();

        long endTs = Instant.now().toEpochMilli();
        long timeCost = endTs - startTs;

        System.out.println(String.format("共消耗 %d ms", timeCost));

        long totalRequest = WR_TIMES * threadNum;
        double timeCostPerRequest = Double.valueOf(timeCost) / totalRequest;

        System.out.println(String.format(String.format("每个请求消耗 %f ms", timeCostPerRequest)));
        System.out.println(String.format("从DB读值 %d 个", dbReadCount.get()));
    }
}
