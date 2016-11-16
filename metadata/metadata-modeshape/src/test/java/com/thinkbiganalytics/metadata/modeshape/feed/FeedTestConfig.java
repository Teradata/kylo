package com.thinkbiganalytics.metadata.modeshape.feed;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 11/10/16.
 */
@Configuration
public class FeedTestConfig {


    @Bean
    public FeedTestUtil feedTestUtil() {
        return new FeedTestUtil();
    }


}
