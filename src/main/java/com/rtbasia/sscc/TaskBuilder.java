package com.rtbasia.sscc;

import java.time.Instant;
import java.util.concurrent.FutureTask;

public class TaskBuilder {
    public static int SLEEP_INTERVAL_MILLI_SECS = 10;

    public <T> FutureTask<T> buildReadTask(ReadOperation<T> readOps) {
        FutureTask<T> futureTask = new FutureTask<T>(() -> {
            long startTs = Instant.now().toEpochMilli();

            String key = readOps.key();
            T dbObj = readOps.readFromDB(key);
            readOps.setCache(key, dbObj);

            return null;
        });

        return futureTask;
    }

    public <E> FutureTask buildWriteTask(WriteOperation<E> writeOps) {
        FutureTask<E> futureTask = new FutureTask<E>(() -> {
            String key = writeOps.key();

            writeOps.clearCache(key);
            writeOps.writeDB();

            return null;
        });

        return futureTask;
    }
}
