/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

import com.thinkbiganalytics.spark.SparkContextService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class MergeTableConfig {

    @Bean
    public TableMerger defaultTableMerger(SparkContextService scs) {
        return new DefaultTableMerger(scs);
    }
}
