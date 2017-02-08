package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-controller
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Hold a cache of the Standardization and Validation rules
 * This cache is built on startup on the application via a startup listener
 * {@link ServicesApplicationStartup}
 */
@Service
public class FieldPolicyCache {

    private static LoadingCache<FieldPolicyType, List<? extends BaseUiPolicyRule>> cache;
    @Inject
    ServicesApplicationStartup startup;

    public FieldPolicyCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<FieldPolicyType, List<? extends BaseUiPolicyRule>>() {
            @Override
            public List<? extends BaseUiPolicyRule> load(FieldPolicyType key) throws Exception {
                return availablePolicies(key);
            }
        });
    }

    public static List<FieldStandardizationRule> getStandardizationPolicies() {
        return (List<FieldStandardizationRule>) cache.getUnchecked(FieldPolicyType.STANDARDIZATION);
    }

    public static List<FieldValidationRule> getValidationPolicies() {
        return (List<FieldValidationRule>) cache.getUnchecked(FieldPolicyType.VALIDATION);
    }

    private List<? extends BaseUiPolicyRule> availablePolicies(FieldPolicyType key) {
        if (FieldPolicyType.STANDARDIZATION.equals(key)) {
            return AvailablePolicies.discoverStandardizationRules();
        } else if (FieldPolicyType.VALIDATION.equals(key)) {
            return AvailablePolicies.discoverValidationRules();
        } else {
            return null;
        }
    }

    @PostConstruct
    private void init() {
        startup.subscribe(new ValidationStartupListener());
        startup.subscribe(new StandardizationStartupListener());
    }

    enum FieldPolicyType {
        STANDARDIZATION, VALIDATION;
    }

    public class ValidationStartupListener implements ServicesApplicationStartupListener {

        @Override
        public void onStartup(DateTime startTime) {
            getValidationPolicies();
        }
    }

    public class StandardizationStartupListener implements ServicesApplicationStartupListener {

        @Override
        public void onStartup(DateTime startTime) {
            getStandardizationPolicies();
        }
    }

}
