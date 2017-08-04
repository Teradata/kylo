package com.thinkbiganalytics.alerts.spi.defaults;
/*-
 * #%L
 * thinkbig-alerts-default
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
import com.thinkbiganalytics.alerts.spi.AlertSourceAggregator;
import com.thinkbiganalytics.metadata.alerts.KyloEntityAwareAlertManager;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 8/3/17.
 */
@Configuration
public class KyloAlertManagerConfig {

    @Bean(name = "kyloAlertManager")
    public KyloEntityAwareAlertManager kyloAlertManager(JpaAlertRepository repo, AlertSourceAggregator aggregator) {
        KyloEntityAwareAlertManager mgr = new KyloEntityAwareAlertManager(repo);
        aggregator.addAlertManager(mgr);
        return mgr;
    }
}
