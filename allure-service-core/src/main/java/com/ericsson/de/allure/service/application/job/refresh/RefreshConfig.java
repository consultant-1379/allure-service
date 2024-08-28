package com.ericsson.de.allure.service.application.job.refresh;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ericsson.de.allure.service.application.job.CronRunnable;

@Component
@RefreshScope
public class RefreshConfig implements CronRunnable {

    private static final Logger LOGGER = getLogger(RefreshConfig.class);

    @Value("${refresh.cron}")
    private String cron;

    @Autowired
    private RefreshEndpoint refreshEndpoint;

    @Autowired
    private Environment env;

    @Override
    public void run() {
        LOGGER.info("Refreshing the configuration.");
        final Collection<String> changes = refreshEndpoint.invoke();
        if (changes.isEmpty()) {
            LOGGER.info("No configuration changes detected.");
        } else {
            changes.forEach(s -> LOGGER.info("{} has been changed to {}", s, env.getProperty(s)));
        }
    }

    @Override
    public String getExpression() {
        return cron;
    }
}