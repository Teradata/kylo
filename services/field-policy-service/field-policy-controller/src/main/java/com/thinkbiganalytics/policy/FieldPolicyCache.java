package com.thinkbiganalytics.policy;

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
 * Created by sr186054 on 9/21/16.
 */
@Service
public class FieldPolicyCache {

    @Inject
    ServicesApplicationStartup startup;

    enum FieldPolicyType {
        STANDARDIZATION, VALIDATION;
    }

    private static LoadingCache<FieldPolicyType, List<? extends BaseUiPolicyRule>> cache;

    public FieldPolicyCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<FieldPolicyType, List<? extends BaseUiPolicyRule>>() {
            @Override
            public List<? extends BaseUiPolicyRule> load(FieldPolicyType key) throws Exception {
                return availablePolicies(key);
            }
        });
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


    public static List<FieldStandardizationRule> getStandardizationPolicies() {
        return (List<FieldStandardizationRule>) cache.getUnchecked(FieldPolicyType.STANDARDIZATION);
    }

    public static List<FieldValidationRule> getValidationPolicies() {
        return (List<FieldValidationRule>) cache.getUnchecked(FieldPolicyType.VALIDATION);
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
