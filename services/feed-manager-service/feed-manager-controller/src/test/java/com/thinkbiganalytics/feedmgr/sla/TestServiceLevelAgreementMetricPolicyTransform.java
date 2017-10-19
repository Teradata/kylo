package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.common.velocity.config.VelocitySpringConfiguration;
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
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSpringConfiguration.class,VelocitySpringConfiguration.class})
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

        FeedOnTimeArrivalMetric metric = new FeedOnTimeArrivalMetric(feedName, cronString, lateTime, lateUnits);
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
                return rule.getName().equalsIgnoreCase("Feed Processing deadline");
            }
        }).orNull();

        rule.getProperty("FeedName").setValue("currentCategory.currentFeed");
        rule.getProperty("ExpectedDeliveryTime").setValue("0 0 12 1/1 * ? *");
        rule.getProperty("NoLaterThanTime").setValue("5");
        rule.getProperty("NoLaterThanUnits").setValue("days");
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
