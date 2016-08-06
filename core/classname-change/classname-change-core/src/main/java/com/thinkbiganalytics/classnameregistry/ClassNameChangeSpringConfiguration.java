package com.thinkbiganalytics.classnameregistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 7/22/16.
 */
@Configuration
public class ClassNameChangeSpringConfiguration {

    @Bean
    public ClassNameChangeRegistry classNameChangeRegistry() {
        return new ClassNameChangeRegistry();
    }

}
