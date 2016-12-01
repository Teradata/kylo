/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkbiganalytics.testing.jpa.TestJpaConfiguration;

/**
 *
 * @author Sean Felten
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-jpa-application.properties")
@SpringApplicationConfiguration(classes = { TestJpaConfiguration.class, JpaAuditLogProviderTestConfig.class })
public class JpaAuditLogProviderTest {

    @Inject
    private JpaAuditLogProvider provider;
    
    @Test
    public void test() {
        
        return;
    }
}
