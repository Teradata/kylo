/**
 *
 */
package com.thinkbiganalytics.metadata.core.sla.feed;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.sla.DatasourceUpdatedSinceSchedule;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import java.io.Serializable;

/**
 *
 */
public class DatasourceUpdatedSinceAssessor extends MetadataMetricAssessor<DatasourceUpdatedSinceSchedule> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof DatasourceUpdatedSinceSchedule;
    }

    @Override
    public void assess(DatasourceUpdatedSinceSchedule metric, MetricAssessmentBuilder<Serializable> builder) {
        builder
            .metric(metric)
            .message("This metric is no longer supported")
            .result(AssessmentResult.FAILURE);

//        List<Date> dates = CronExpressionUtil.getPreviousFireTimes(metric.getCronExpression(), 2);
//        DateTime schedTime = new DateTime(dates.get(0));
//        String name = metric.getDatasourceName();
//        DatasourceCriteria crit = getDatasetProvider().datasetCriteria().name(name).limit(1);
//        List<Datasource> list = getDatasetProvider().getDatasources(crit);
//        ArrayList<Dataset<Datasource, ChangeSet>> result = new ArrayList<>();
//        boolean incomplete = false;
//        
//        if (list.size() > 0) {
//            Datasource ds = list.get(0);
//            List<Dataset<Datasource, ChangeSet>> changes = getDataOperationsProvider().getDatasets(ds.getId());
//            
//            for (Dataset<Datasource, ChangeSet> cs : changes) {
//                if (cs.getCreatedTime().isBefore(schedTime)) {
//                    break;
//                }
//                
//                for (ChangeSet content : cs.getChanges()) {
//                    incomplete |= content.getCompletenessFactor() > 0;
//                }
//                
//                result.add(cs);
//            }
//        }
//        
//        builder.metric(metric);
//        
//        if (result.size() > 0) {
//            builder
//                .message(result.size() + " change sets found since the scheduled time of " + schedTime)
//                .data(result)
//                .result(incomplete ? AssessmentResult.WARNING : AssessmentResult.SUCCESS);
//        } else {
//            builder
//                .message("No change sets found since the scheduled time of " + schedTime)
//                .data(result)
//                .result(AssessmentResult.FAILURE);
//        }

    }

}
