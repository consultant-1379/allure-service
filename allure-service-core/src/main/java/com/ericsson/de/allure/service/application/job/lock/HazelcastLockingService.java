package com.ericsson.de.allure.service.application.job.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.ericsson.de.allure.service.infrastructure.Profiles.PRODUCTION;

@Service
@Profile(PRODUCTION)
public class HazelcastLockingService implements LockingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastLockingService.class);

    private final HazelcastInstance hazelcast;

    @Autowired
    public HazelcastLockingService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @Override
    public void lock(String jobName, Runnable job) {
        ILock lock = hazelcast.getLock(jobName);
        if (lock.tryLock()) {
            LOGGER.debug("Acquired lock '{}'", jobName);
            try {
                job.run();
            } finally {
                lock.unlock();
                LOGGER.debug("Released lock '{}'", jobName);
            }
        } else {
            LOGGER.debug("Could not acquire lock '{}'", jobName);
        }
    }
}
