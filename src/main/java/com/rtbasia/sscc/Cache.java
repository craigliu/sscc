package com.rtbasia.sscc;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Cache {
    private TaskBuilder taskBuilder = new TaskBuilder();
    private TaskQueue taskQueue = new TaskQueue();

    public <T> T get(ReadOperation<T> readOps, long timeout) {
        T cachedObj = readOps.readFromCache(readOps.key());

        // 缓存中有值，直接返回
        if (cachedObj != null) {
            return cachedObj;
        }

        FutureTask<T> task = taskBuilder.buildReadTask(readOps);
        taskQueue.offer(task, readOps.key());

        // 重复尝试从cache中取值直到超时
        long startMilliSecs = Instant.now().toEpochMilli();

        while (true) {
            long nowMilliSecs = Instant.now().toEpochMilli();
            long timeElapsed = nowMilliSecs - startMilliSecs;

            // 超时，直接从DB取值
            if (timeElapsed > timeout) {
                return readOps.readFromDB(readOps.key());
            }

            // 能从cache中取到值了，直接返回
            cachedObj = readOps.readFromCache(readOps.key());

            if (cachedObj != null) {
                return cachedObj;
            }
        }
    }

    public void write(WriteOperation writeOps) throws ExecutionException, InterruptedException {
        FutureTask task = writeAsync(writeOps);

        task.get();
    }

    public FutureTask writeAsync(WriteOperation writeOps) {
        FutureTask task = taskBuilder.buildWriteTask(writeOps);
        taskQueue.offer(task, writeOps.key());

        return task;
    }
}
