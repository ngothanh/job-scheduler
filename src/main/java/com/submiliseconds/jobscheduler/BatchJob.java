package com.submiliseconds.jobscheduler;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BatchJob implements Job {

    private List<Job> jobs;
    private Job callBack;
    private JobExecutor executor;

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
            executor.execute(new Job() {
                @Override
                public String getName() {
                    return job.getName();
                }

                @Override
                public void run() {
                    try {
                        job.run();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        executor.execute(
                new Job() {
                    @Override
                    public String getName() {
                        return getName() + "-Callback";
                    }

                    @Override
                    public void run() {
                        try {
                            latch.await();
                            callBack.run();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
        );

    }
}
