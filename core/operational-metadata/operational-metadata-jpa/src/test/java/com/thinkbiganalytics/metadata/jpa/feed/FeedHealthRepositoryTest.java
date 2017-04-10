package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
@Transactional
public class FeedHealthRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(FeedHealthRepositoryTest.class);

    @Autowired
    FeedHealthRepository repo;

    @Test
    public void testFindAll() {
        List<JpaOpsManagerFeedHealth> all = repo.findAll();
        LOG.debug("all = " + all);
    }
}