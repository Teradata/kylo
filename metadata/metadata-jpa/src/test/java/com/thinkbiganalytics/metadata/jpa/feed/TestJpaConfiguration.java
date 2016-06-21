/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.jpa.JpaConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author Sean Felten
 */
@Import(JpaConfiguration.class)
@PropertySource("classpath:TestJpaConfiguration.properties")
public class TestJpaConfiguration {

    @Bean
    public ServiceLevelAgreementProvider slaProvider() {
        return new InMemorySLAProvider();
    }


}
