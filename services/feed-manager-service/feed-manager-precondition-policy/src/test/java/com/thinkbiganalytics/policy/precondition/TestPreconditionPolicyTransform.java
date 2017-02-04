package com.thinkbiganalytics.policy.precondition;

/*-
 * #%L
 * thinkbig-feed-manager-precondition-policy
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
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;
import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.LabelValue;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Test the Precondition policy transformation to/from user interface objects
 */
public class TestPreconditionPolicyTransform {


    @Test
    public void testFeedExecutedSinceFeed() throws IOException {
        String dependentUponFeed = "category.feed";
        String currentFeed = "currentCategory.currentFeeda";

        FeedExecutedSinceFeeds feedExecutedSinceFeed = new FeedExecutedSinceFeeds(currentFeed, dependentUponFeed);
        PreconditionRule uiModel = PreconditionAnnotationTransformer.instance().toUIModel(feedExecutedSinceFeed);
        List<LabelValue> values = new ArrayList<>();
        values.add(new LabelValue("Label1", "Value1"));
        values.add(new LabelValue("Label2", "Value2"));
        uiModel.getProperty("Dependent Feeds").setValues(values);
        FeedExecutedSinceFeeds convertedPolicy = fromUI(uiModel, FeedExecutedSinceFeeds.class);
        Assert.assertEquals(currentFeed, convertedPolicy.getSinceCategoryAndFeedName());
        Assert.assertEquals(dependentUponFeed, convertedPolicy.getCategoryAndFeeds());

        Set<ObligationGroup> preconditionGroups = convertedPolicy.buildPreconditionObligations();


    }


    @Test
    public void testUiCreation() {
        List<PreconditionRule> rules = AvailablePolicies.discoverPreconditions();
        PreconditionRule rule = Iterables.tryFind(rules, new Predicate<PreconditionRule>() {
            @Override
            public boolean apply(PreconditionRule rule) {
                return rule.getName().equalsIgnoreCase(PreconditionPolicyConstants.FEED_EXECUTED_SINCE_FEEDS_NAME);
            }
        }).orNull();

        rule.getProperty("Since Feed").setValue("currentCategory.currentFeed");
        rule.getProperty("Dependent Feeds").setValue("category.feed");
        FeedExecutedSinceFeeds convertedPolicy = fromUI(rule, FeedExecutedSinceFeeds.class);
        Assert.assertEquals("currentCategory.currentFeed", convertedPolicy.getSinceCategoryAndFeedName());
        Assert.assertEquals("category.feed", convertedPolicy.getCategoryAndFeeds());
    }

    @Test
    public void testPeriod() {
        Period p = new Period(0, 0, 1, 0);
        String withinPeriod = PeriodFormat.getDefault().print(p);

    }


    private <T extends Precondition> T fromUI(PreconditionRule uiModel, Class<T> policyClass) {
        try {
            Precondition policy = PreconditionAnnotationTransformer.instance().fromUiModel(uiModel);
            return (T) policy;
        } catch (PolicyTransformException e) {
            e.printStackTrace();

        }
        return null;
    }


}
