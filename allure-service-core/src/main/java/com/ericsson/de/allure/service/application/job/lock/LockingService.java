package com.ericsson.de.allure.service.application.job.lock;

public interface LockingService {

    void lock(String jobName, Runnable job);

}
