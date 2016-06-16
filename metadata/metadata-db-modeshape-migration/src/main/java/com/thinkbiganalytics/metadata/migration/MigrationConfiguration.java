package com.thinkbiganalytics.metadata.migration;

import com.thinkbiganalytics.metadata.migration.category.CategoryDatabaseProvider;
import com.thinkbiganalytics.metadata.migration.feed.FeedDatabaseProvider;
import com.thinkbiganalytics.metadata.migration.template.TemplateDatabaseProvider;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by sr186054 on 6/15/16.
 */
@Configuration
@Import(ModeShapeEngineConfig.class)
public class MigrationConfiguration {

    @Bean
    public FeedDatabaseProvider feedDatabaseProvider() {
        return new FeedDatabaseProvider();
    }

    @Bean
    public CategoryDatabaseProvider categoryDatabaseProvider() {
        return new CategoryDatabaseProvider();
    }

    @Bean
    public TemplateDatabaseProvider templateDatabaseProvider() {
        return new TemplateDatabaseProvider();
    }

    @Bean
    public FeedMigrationService feedMigrationService() {
        return new FeedMigrationService();
    }
}
