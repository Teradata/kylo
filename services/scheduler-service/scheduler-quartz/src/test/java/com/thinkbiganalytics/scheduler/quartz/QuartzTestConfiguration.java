package com.thinkbiganalytics.scheduler.quartz;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.QuartzScheduler;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class QuartzTestConfiguration {

    @Bean
    JobScheduler jobScheduler(){
        return new QuartzScheduler();
    }

    @Bean
    ClusterService clusterService(){
        return Mockito.mock(ClusterService.class);
    }

    @Bean(name="dataSource")
    DataSource dataSource(){
        return Mockito.mock(DataSource.class);
    }

}
