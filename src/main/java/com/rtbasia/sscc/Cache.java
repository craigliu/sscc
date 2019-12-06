package com.rtbasia.sscc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Cache {
    private TaskBuilder taskBuilder = new TaskBuilder();
    private TaskQueue taskQueue = new TaskQueue();

    public <T> T get(ReadOperation<T> readOps, int timeout) throws ReadTimeoutException,
            InterruptedException, ExecutionException {
        FutureTask<T> task = taskBuilder.buildReadTask(readOps, timeout);
        taskQueue.offerRead(task, readOps.key());

        try {
            return task.get(); // TODO: 处理超时的情况, 可能需要直接从DB读旧的数据
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ReadTimeoutException) {
                ReadTimeoutException rte = new ReadTimeoutException(cause.getMessage());
                rte.initCause(cause);

                throw rte;
            }

            throw e;
        }
    }

    public void write(WriteOperation writeOps) throws ExecutionException, InterruptedException {
        FutureTask task = writeAsync(writeOps);

        task.get();
    }

    public FutureTask writeAsync(WriteOperation writeOps) {
        FutureTask task = taskBuilder.buildWriteTask(writeOps);
        taskQueue.offerWrite(task, writeOps.key());

        return task;
    }
}
