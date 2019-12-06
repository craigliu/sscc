package com.rtbasia.sscc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ExecutionException;

public class JedisSSCCache {
    private Cache cache = new Cache();
    private ObjectMapper mapper = new ObjectMapper();

    JedisPool jedisPool;

    public JedisSSCCache(String host, int port) {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    /**
     * 仅供测试时使用
     *
     * @param key
     * @param value
     */
    public void writeRaw(String key, String value) {
        Jedis jedis = jedisPool.getResource();

        try {
            jedis.set(key, value);
        } finally {
            jedis.close();
        }
    }

    public <T> T get(String key, Class<T> clazz, int timeout) throws InterruptedException, ReadTimeoutException, ExecutionException {
        return cache.get(new ReadOperation<T>() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public Object readFromDB(String key) {
                return null; // TODO: 如果缓存取失败可能要fallback到数据库取
            }

            @Override
            public T readFromCache(String key) {
                Jedis jedis = jedisPool.getResource();

                try {
                    String json = jedis.get(key);
                    T obj = null;

                    if (json != null) {
                        try {
                            obj = mapper.readValue(json, clazz);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace(); // TODO: 处理缓存中数据无法反序列化的情况
                        }
                    }

                    return obj;
                } finally {
                    jedis.close();
                }
            }
        }, timeout);
    }

    public <T> void write(String key, Class<T> clazz, DBReader<T> reader, DBWriter<T> writer)
            throws ExecutionException, InterruptedException {
        cache.write(new WriteOperation<T>() {
            @Override
            public void writeDB() {
                writer.writeDB();
            }

            @Override
            public void clearCache(String key) {
                Jedis jedis = jedisPool.getResource();

                try {
                    jedis.del(key);
                } finally {
                    jedis.close();
                }
            }

            @Override
            public void setCache(String key, T obj) {
                String json = null;

                Jedis jedis = jedisPool.getResource();

                try {
                    json = mapper.writeValueAsString(obj);

                    jedis.set(key, json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace(); // TODO: 处理序列化失败情况
                } finally {
                    jedis.close();
                }
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public Object readFromDB(String key) {
                return reader.readFromDB(key);
            }
        });
    }
}
