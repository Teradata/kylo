package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 9/21/16.
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
