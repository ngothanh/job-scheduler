package com.submiliseconds.jobscheduler;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

class ScheduledJob implements Job, Delayed {

    long scheduleTimeMillis;
    Job job;

    public ScheduledJob(Job job, long scheduleTimeMillis) {
        this.scheduleTimeMillis = scheduleTimeMillis;
        this.job = job;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remainingDelay = scheduleTimeMillis - System.currentTimeMillis();
        return unit.convert(remainingDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (this == other) return 0;
        long diff = this.scheduleTimeMillis - ((ScheduledJob) other).scheduleTimeMillis;
        return Long.compare(diff, 0);
    }

    @Override
    public String getName() {
        return job.getName();
    }

    @Override
    public void run() {
        job.run();
    }
}
