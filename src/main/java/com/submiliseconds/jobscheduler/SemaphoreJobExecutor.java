package com.submiliseconds.jobscheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreJobExecutor implements JobExecutor {

    private final int maxConcurrency;
    private final Semaphore semaphore;
    private final ExecutorService executor;

    public SemaphoreJobExecutor(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        semaphore = new Semaphore(maxConcurrency);
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int maxConcurrentJobs() {
        return maxConcurrency;
    }

    @Override
    public void execute(Job job) {
        executor.submit(
                () -> {
                    try {
                        boolean acquired = semaphore.tryAcquire();
                        if (acquired) {
                            job.run();
                        }
                    } finally {
                        semaphore.release();
                    }
                }
        );
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
