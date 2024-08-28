package com.ericsson.de.allure.service.application.job.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.ericsson.de.allure.service.application.job.cleanup.CleanupJob;
import com.ericsson.de.allure.service.application.job.CronRunnable;
import com.ericsson.de.allure.service.application.job.refresh.RefreshConfig;

/**
 * This class executes the specified {@link Runnable}s on the specified schedule.
 * The cron for each task is stored in configuration so can be updated at any time.
 * This class ensures the tasks executes with the latest cron.
 * {@link org.springframework.scheduling.annotation.Scheduled} can't be used as it stores the cron at startup and
 * won't be updated when the configuration is refreshed.
 */
@Configuration
@EnableScheduling
public class Scheduler implements SchedulingConfigurer {

    @Autowired
    private CleanupJob cleanupJob;

    @Autowired
    private RefreshConfig refreshConfig;

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(cleanupJob, getTrigger(cleanupJob));
        taskRegistrar.addTriggerTask(refreshConfig, getTrigger(refreshConfig));
    }

    private Trigger getTrigger(final CronRunnable cronRunnable) {
        return triggerContext -> {
            final String cleanupCron = cronRunnable.getExpression();
            final Trigger trigger = new CronTrigger(cleanupCron);
            return trigger.nextExecutionTime(triggerContext);
        };
    }
}
