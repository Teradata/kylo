package com.thinkbiganalytics.feedmgr.service.feed;

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

import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.precondition.PreconditionPolicyRuleCache;
import com.thinkbiganalytics.policy.precondition.transform.PreconditionAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */
public class FeedManagerPreconditionService {

    @Inject
    FeedManagerFeedService feedManagerFeedService;

    @Inject
    PreconditionPolicyRuleCache preconditionPolicyRuleCache;

    public List<PreconditionRule> getPossiblePreconditions() {
        List<PreconditionRule> rules = preconditionPolicyRuleCache.getPreconditionRules();
        //find and attach Feed Lookup list to those that are of that type

        feedManagerFeedService
            .applyFeedSelectOptions(
                PreconditionAnnotationTransformer.instance()
                    .findPropertiesForRulesetMatchingRenderTypes(rules, new String[]{PolicyPropertyTypes.PROPERTY_TYPE.feedChips.name(),
                                                                                     PolicyPropertyTypes.PROPERTY_TYPE.feedSelect.name(),
                                                                                     PolicyPropertyTypes.PROPERTY_TYPE.currentFeed.name()}));
        return rules;
    }

}
