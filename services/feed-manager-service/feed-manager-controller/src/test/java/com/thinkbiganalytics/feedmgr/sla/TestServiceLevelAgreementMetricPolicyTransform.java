package com.thinkbiganalytics.feedmgr.sla;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.config.TestSpringConfiguration;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetric;
import com.thinkbiganalytics.policy.PolicyTransformException;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 4/5/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSpringConfiguration.class})
public class TestServiceLevelAgreementMetricPolicyTransform {

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementMetrics;

    @Test
    public void testFeedExecutedSinceFeed() throws IOException, ParseException {
        String feedName = "category.feed";
        String cronString = "0 0 12 1/1 * ? *";
        Integer lateTime = 5;
        String lateUnits = "days";
        Integer asOfTime = 3;
        String asOfUnits = "hours";

        FeedOnTimeArrivalMetric metric = new FeedOnTimeArrivalMetric(feedName, cronString, lateTime, lateUnits, asOfTime, asOfUnits);
        ServiceLevelAgreementRule uiModel = ServiceLevelAgreementMetricTransformer.instance().toUIModel(metric);

        FeedOnTimeArrivalMetric convertedPolicy = fromUI(uiModel, FeedOnTimeArrivalMetric.class);
        Assert.assertEquals(cronString, convertedPolicy.getExpectedExpression().getCronExpression());
        Assert.assertEquals(Period.days(5).toString(), convertedPolicy.getLatePeriod().toString());


    }


    @Test
    public void testUiCreation() {
        List<ServiceLevelAgreementRule> rules = serviceLevelAgreementMetrics.discoverSlaMetrics();
        ServiceLevelAgreementRule rule = Iterables.tryFind(rules, new Predicate<ServiceLevelAgreementRule>() {
            @Override
            public boolean apply(ServiceLevelAgreementRule rule) {
                return rule.getName().equalsIgnoreCase("Feed Processed Data by a certain time");
            }
        }).orNull();

        rule.getProperty("FeedName").setValue("currentCategory.currentFeed");
        rule.getProperty("ExpectedDeliveryTime").setValue("0 0 12 1/1 * ? *");
        rule.getProperty("NoLaterThanTime").setValue("5");
        rule.getProperty("NoLaterThanUnits").setValue("days");
        rule.getProperty("AsOfTime").setValue("3");
        rule.getProperty("AsOfUnits").setValue("hours");
        FeedOnTimeArrivalMetric convertedPolicy = fromUI(rule, FeedOnTimeArrivalMetric.class);
        Assert.assertEquals("currentCategory.currentFeed", convertedPolicy.getFeedName());
        Assert.assertEquals("0 0 12 1/1 * ? *", convertedPolicy.getExpectedExpression().getCronExpression());
    }


    private <T extends Metric> T fromUI(ServiceLevelAgreementRule uiModel, Class<T> policyClass) {
        try {
            Metric policy = ServiceLevelAgreementMetricTransformer.instance().fromUiModel(uiModel);
            return (T) policy;
        } catch (PolicyTransformException e) {
            e.printStackTrace();

        }
        return null;
    }


}
