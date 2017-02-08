package com.thinkbiganalytics.servicemonitor.db;

/*-
 * #%L
 * thinkbig-service-monitor-pipeline-db
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.servicemonitor.check.PipelineDatabaseServiceStatusCheck;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 */
@Configuration
public class PipelineDbSpringConfiguration {

    @Bean(name = "pipelineDatabaseServiceStatus")
    public PipelineDatabaseServiceStatusCheck pipelineDatabaseServiceHealth() {
        return new PipelineDatabaseServiceStatusCheck();
    }
}
