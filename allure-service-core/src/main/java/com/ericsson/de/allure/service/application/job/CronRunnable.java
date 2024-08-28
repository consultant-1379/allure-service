package com.ericsson.de.allure.service.application.job;

/**
 * Allow any job which needs to be scheduled to be triggered in
 * {@link com.ericsson.de.allure.service.application.job.scheduling.Scheduler}
 */
public interface CronRunnable extends Runnable {
    String getExpression();
}
