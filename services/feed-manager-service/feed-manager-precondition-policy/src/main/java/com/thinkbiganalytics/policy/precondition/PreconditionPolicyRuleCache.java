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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * A cache of all the preconditions found on the classpath, transformed to user interface objects
 */
@Service
public class PreconditionPolicyRuleCache implements ServicesApplicationStartupListener {

    @Inject
    ServicesApplicationStartup startup;

    List<PreconditionRule> cache = null;
    boolean discovered = false;

    public PreconditionPolicyRuleCache() {

    }

    public void onStartup(DateTime startTime) {
        discoverPreconditionRules();
    }

    private void discoverPreconditionRules() {
        cache = AvailablePolicies.discoverPreconditions();
        discovered = true;
    }


    @PostConstruct
    private void init() {
        startup.subscribe(this);
    }

    public List<PreconditionRule> getPreconditionRules() {
        if (discovered) {
            return cache;
        } else {
            discoverPreconditionRules();
            return cache;
        }
    }

}
