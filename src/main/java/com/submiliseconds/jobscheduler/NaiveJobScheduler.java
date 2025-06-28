package com.submiliseconds.jobscheduler;

import java.time.Duration;

public class NaiveJobScheduler implements JobScheduler {

    private JobExecutor executor;

    @Override
    public void submit(Job job) {
        executor.execute(job);
    }

    @Override
    public void submit(Job job, Duration delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay.toMillis());
                executor.execute(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
