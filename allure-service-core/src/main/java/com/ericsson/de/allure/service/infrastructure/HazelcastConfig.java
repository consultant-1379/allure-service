package com.ericsson.de.allure.service.infrastructure;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.ericsson.de.allure.service.infrastructure.Profiles.PRODUCTION;

@Configuration
@Profile(PRODUCTION)
public class HazelcastConfig {

    @Bean
    public Config config() {
        Config config = new Config();
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).addMember("10.0.0.3-10");
        return config;
    }
}
