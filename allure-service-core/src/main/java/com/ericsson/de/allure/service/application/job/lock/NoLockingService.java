package com.ericsson.de.allure.service.application.job.lock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.ericsson.de.allure.service.infrastructure.Profiles.PRODUCTION;

@Service
@Profile("!" + PRODUCTION)
public class NoLockingService implements LockingService {

    @Override
    public void lock(String jobName, Runnable job) {
        job.run();
    }
}
