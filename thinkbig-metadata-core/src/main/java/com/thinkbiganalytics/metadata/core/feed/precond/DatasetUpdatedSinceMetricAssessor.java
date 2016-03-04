/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.feed.precond.DatasetUpdatedSinceMetric;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

/**
 *
 * @author Sean Felten
 */
public class DatasetUpdatedSinceMetricAssessor extends MetadataMetricAssessor<DatasetUpdatedSinceMetric> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof DatasetUpdatedSinceMetric;
    }

    @Override
    public void assess(DatasetUpdatedSinceMetric metric, MetricAssessmentBuilder<ArrayList<ChangeSet<Dataset, ChangedContent>>> builder) {
        List<Date> dates = CronExpressionUtil.getPreviousFireTimes(metric.getCronExpression(), 2);
        DateTime schedTime = new DateTime(dates.get(0));
//        DateTime prevSchedTime = new DateTime(dates.get(1));
        String name = metric.getDatasetName();
        DatasetCriteria crit = getDatasetProvider().datasetCriteria().name(name);
        Set<Dataset> set = getDatasetProvider().getDatasets(crit);
        ArrayList<ChangeSet<Dataset, ChangedContent>> result = new ArrayList<>();
        boolean incomplete = false;
        
        if (set.size() > 0) {
            Dataset ds = set.iterator().next();
            Collection<ChangeSet<Dataset, ChangedContent>> changes = getDataOperationsProvider().getChangeSets(ds.getId());
            
            for (ChangeSet<Dataset, ChangedContent> cs : changes) {
                if (cs.getTime().isBefore(schedTime)) {
                    break;
                }
                
                for (ChangedContent content : cs.getChanges()) {
                    incomplete |= content.getCompletenessFactor() > 0;
                }
                
                result.add(cs);
            }
        }
        
        builder.metric(metric);

        if (result.size() > 0) {
            builder
                .message(result.size() + " change sets found since the scheduled time of " + schedTime)
                .data(result)
                .result(incomplete ? AssessmentResult.WARNING : AssessmentResult.SUCCESS);
        } else {
            builder
                .message("No change sets found since the scheduled time of " + schedTime)
                .data(result)
                .result(AssessmentResult.FAILURE);
        }
        
    }

}
