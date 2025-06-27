package com.submiliseconds.jobscheduler;

public interface JobExecutor {

    int maxConcurrentJobs();

    void execute(Job job);
}
