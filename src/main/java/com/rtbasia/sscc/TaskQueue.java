package com.rtbasia.sscc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TaskQueue {
    static int DEFAULT_NUMBER_OF_QUEUES = 10;

    private ExecutorService[] queues;

    private int numOfQueues;

    public TaskQueue() {
        this(DEFAULT_NUMBER_OF_QUEUES);
    }

    public TaskQueue(int numOfQueues) {
        queues = new ExecutorService[numOfQueues];
        allocateExecutors(queues, numOfQueues);
        this.numOfQueues = numOfQueues;
    }

    /**
     * NOTICE: 这里直接使用newSingleThreadExecutor是个不好的实现。在实际项目中应该用thread pool代替
     *
     * @param queues
     * @param size
     */
    private void allocateExecutors(ExecutorService[] queues, int size) {
        for (int i = 0; i < size; i++) {
            queues[i] = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 根据key散列到对应的处理队列，保证同一个key的写操作一定排在之后的读操作之前。同时多个处理队列可以保证一定的并发度
     *
     * @param futureTask
     * @param key
     */
    public void offer(FutureTask futureTask, String key) {
        int hash = Math.abs(key.hashCode());
        int pos = hash % numOfQueues;

        queues[pos].execute(futureTask);
    }
}
