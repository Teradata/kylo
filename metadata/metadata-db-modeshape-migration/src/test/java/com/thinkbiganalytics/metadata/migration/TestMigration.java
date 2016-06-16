package com.thinkbiganalytics.metadata.migration;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Created by sr186054 on 6/15/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
@SpringApplicationConfiguration(classes = TestConfig.class)
public class TestMigration {

    @Autowired
    public Environment env;

    @Inject
    FeedMigrationService feedMigrationService;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Test
    public void testMigration() {

        feedMigrationService.migrate();
    }


}
