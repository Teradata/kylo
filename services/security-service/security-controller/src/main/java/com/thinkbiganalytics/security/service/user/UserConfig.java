package com.thinkbiganalytics.security.service.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the users and groups controllers.
 */
@Configuration
public class UserConfig {

    /**
     * Gets the user service for accessing Kylo users.
     *
     * @return the user service
     */
    @Bean
    public UserService userService() {
        return new UserMetadataService();
    }
}
