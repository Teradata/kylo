/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.util.ArrayList;

import org.joda.time.DateTime;

import com.thinkbiganalytics.controller.MetadataProviderService;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
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
public class WithinScheduleAssessor extends MetadataMetricAssessor<WithinSchedule> {

    public WithinScheduleAssessor(MetadataProviderService service) {
        super(service);
    }

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof WithinSchedule;
    }

    @Override
    public void assess(WithinSchedule metric, 
                       MetricAssessmentBuilder<ArrayList<ChangeSet<Dataset, ChangedContent>>> builder) {
        DateTime start = new DateTime(CronExpressionUtil.getPreviousFireTime(metric.getCronExpression()));
        DateTime end = start.withPeriodAdded(metric.getPeriod(), 1);
        
        builder.metric(metric);
        
        if (start.isBeforeNow() && end.isAfterNow()) {
            builder
                .result(AssessmentResult.SUCCESS)
                .message("Current time falls between the schedule " + start + " - " + end)
                .data(new ArrayList<ChangeSet<Dataset, ChangedContent>>());
        } else {
            builder
                .result(AssessmentResult.FAILURE)
                .message("Current time does not falls between the schedule " + start + " - " + end);
        }
    }
}
