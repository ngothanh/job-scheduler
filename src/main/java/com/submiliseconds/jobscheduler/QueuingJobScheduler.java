package com.submiliseconds.jobscheduler;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

public class QueuingJobScheduler implements JobScheduler {

    private final JobExecutor executor;
    private final BlockingQueue<ScheduledJob> delayedQueue = new DelayQueue<>();
    private final Thread dispatcherThread;
    private volatile boolean isRunning = true;

    public QueuingJobScheduler(int maxConcurrency) {
        this.executor = new SemaphoreJobExecutor(maxConcurrency);
        this.dispatcherThread = new Thread(this::dispatchLoop, "dispatcher-thread");
        this.dispatcherThread.start();
    }

    private void dispatchLoop() {
        while (isRunning || !delayedQueue.isEmpty()) {
            ScheduledJob job = delayedQueue.peek();
            executor.execute(job);
        }
    }

    @Override
    public void submit(Job job) {
        submit(job, Duration.ZERO);
    }

    @Override
    public void submit(Job job, Duration delay) {
        var scheduledJob = new ScheduledJob(
                job,
                System.currentTimeMillis() + delay.toMillis()
        );
        delayedQueue.offer(scheduledJob);
    }
}
