package com.submiliseconds.jobscheduler;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BatchJob implements Job {

    private List<Job> jobs;
    private Job callBack;

    public BatchJob(List<Job> jobs, Job callBack) {
        this.jobs = jobs;
        this.callBack = callBack;
    }

    @Override
    public String getName() {
        return "BatchJob-" + System.currentTimeMillis();
    }

    @Override
    public void run() {
        CountDownLatch latch = new CountDownLatch(jobs.size());
        for (Job job : jobs) {
            try {
                job.run();
            } finally {
                latch.countDown();
            }
        }

        try {
            latch.await();
            callBack.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
