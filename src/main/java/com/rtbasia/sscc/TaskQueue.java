package com.rtbasia.sscc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TaskQueue {
    static int DEFAULT_READ_QUEUE_SIZE = 10;
    static int DEFAULT_WRITE_QUEUE_SIZE = 5;

    private ExecutorService[] readQueues;
    private ExecutorService[] writeQueues;

    private int readQueueSize;
    private int writeQueueSize;

    public TaskQueue() {
        this(DEFAULT_READ_QUEUE_SIZE, DEFAULT_WRITE_QUEUE_SIZE);
    }

    public TaskQueue(int readQueueSize, int writeQueueSize) {
        readQueues = new ExecutorService[readQueueSize];
        allocateExecutors(readQueues, readQueueSize);
        this.readQueueSize = readQueueSize;

        writeQueues = new ExecutorService[writeQueueSize];
        allocateExecutors(writeQueues, writeQueueSize);
        this.writeQueueSize = writeQueueSize;
    }

    private void allocateExecutors(ExecutorService[] queues, int size) {
        for (int i = 0; i < size; i++) {
            queues[i] = Executors.newSingleThreadExecutor();
        }
    }

    public void offerRead(FutureTask readFuture, String key) {
        int hash = Math.abs(key.hashCode());
        int pos = hash % readQueueSize;

        readQueues[pos].execute(readFuture);
    }

    public void offerWrite(FutureTask writeFuture, String key) {
        int hash = Math.abs(key.hashCode());
        int pos = hash % writeQueueSize;

        writeQueues[pos].execute(writeFuture);
    }
}
