package com.submiliseconds.jobscheduler;

import java.time.Duration;

public interface JobScheduler {

    void submit(Job job);

    void submit(Job job, Duration delay);
}
