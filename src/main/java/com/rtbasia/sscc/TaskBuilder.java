package com.rtbasia.sscc;

import java.time.Instant;
import java.util.concurrent.FutureTask;

public class TaskBuilder {
    public static int SLEEP_INTERVAL_MILLI_SECS = 10;

    public <T> FutureTask<T> buildReadTask(ReadOperation<T> readOps, int timeout) {
        FutureTask<T> futureTask = new FutureTask<T>(() -> {
            long startTs = Instant.now().toEpochMilli();

            String key = readOps.key();

            while (true) {
                long nowTs = Instant.now().toEpochMilli();
                long timeElapse = nowTs - startTs;

                if (timeElapse > timeout) { //TODO: 这里可以根据策略选择从数据库取值或者直接返回超时异常
                    throw new ReadTimeoutException(key);
                }

                //1. 尝试从缓存中取结果，能取到则直接返回。
                T cachedObj = readOps.readFromCache(key);

                if (cachedObj != null) {
                    return cachedObj;
                }

                //2. 如果取得结果为空，可能前面有尚未完成的写操作。则等待一下
                Thread.sleep(SLEEP_INTERVAL_MILLI_SECS);
            }
        });

        return futureTask;
    }

    public <E> FutureTask buildWriteTask(WriteOperation<E> writeOps) {
        FutureTask<E> futureTask = new FutureTask<E>(() -> {
            String key = writeOps.key();

            writeOps.clearCache(key);
            writeOps.writeDB();

            E obj = (E)writeOps.readFromDB(key);
            writeOps.setCache(key, obj);

            return null;
        });

        return futureTask;
    }
}
