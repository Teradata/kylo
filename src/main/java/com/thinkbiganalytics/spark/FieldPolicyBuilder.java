package com.thinkbiganalytics.spark;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;
import com.thinkbiganalytics.spark.standardization.impl.DateTimeStandardizer;
import com.thinkbiganalytics.spark.standardization.impl.RemoveControlCharsStandardizer;
import com.thinkbiganalytics.spark.validation.Validator;
import com.thinkbiganalytics.spark.validation.impl.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class FieldPolicyBuilder {

    public static final FieldPolicy SKIP_VALIDATION = FieldPolicyBuilder.newBuilder().skipSchemaValidation().build();

    private FieldPolicy policy = new FieldPolicy();
    private List<Validator> validators = new Vector<>();
    private List<StandardizationPolicy> standardizationPolicies = new Vector<>();

    private FieldPolicyBuilder() {
        super();
    }

    public static FieldPolicyBuilder newBuilder() {
        return new FieldPolicyBuilder();
    }

    FieldPolicyBuilder addValidator(Validator validator) {
        validators.add(validator);
        return this;
    }

    FieldPolicyBuilder addStandardizer(StandardizationPolicy policy) {
        standardizationPolicies.add(policy);
        return this;
    }

    FieldPolicyBuilder disallowNullOrEmpty() {
        policy.setNullable(false);
        return this;
    }

    /**
     * Constrains value to one of the values provided in the list
     */
    FieldPolicyBuilder constrainValues(String... values) {
        validators.add(new LookupValidator(values));
        return this;
    }

    FieldPolicyBuilder constrainLength(int minlen, int maxlen) {
        validators.add(new LengthValidator(minlen, maxlen));
        return this;
    }

    FieldPolicyBuilder skipSchemaValidation() {
        policy.setSkipSchemaValidation(true);
        return this;
    }

    FieldPolicyBuilder enableDiscoverTypes() {
        policy.setTypeDiscovery(true);
        return this;
    }

    FieldPolicy build() {
        FieldPolicy newPolicy = this.policy;
        newPolicy.setValidators(validators);
        newPolicy.setStandardizationPolicies(standardizationPolicies);
        return newPolicy;
    }

    /*
    FieldPolicyBuilder constrainRange(Number min, Number max) {
        policy.addValidator(new RangeValidator(min, max));
        return this;
    }
    */
    public static void main(String[] args) {
        Map<String, FieldPolicy> policyMap = new HashMap<String, FieldPolicy>();

        policyMap.put("registration_dttm", FieldPolicyBuilder.newBuilder().addValidator(TimestampValidator.instance()).build());
        policyMap.put("id", FieldPolicyBuilder.newBuilder().disallowNullOrEmpty().build());
        //policyMap.put("first_name", FieldPolicyBuilder.SKIP_VALIDATION);
        //policyMap.put("last_name",  FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("email", FieldPolicyBuilder.newBuilder().addValidator(EmailValidator.instance()).build());
        policyMap.put("gender", FieldPolicyBuilder.newBuilder().constrainValues("Male", "Female").build());
        policyMap.put("ip_address", FieldPolicyBuilder.newBuilder().addValidator(IPAddressValidator.instance()).build());
        policyMap.put("cc", FieldPolicyBuilder.newBuilder().addValidator(CreditCardValidator.instance()).build());
        //policyMap.put("country", FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("birthdate", FieldPolicyBuilder.newBuilder().addStandardizer(new DateTimeStandardizer("MM/dd/YYYY", DateTimeStandardizer.OutputFormats.DATE_ONLY)).build());
        //policyMap.put("salary", FieldPolicyBuilder.SKIP_VALIDATION);
        //policyMap.put("title", FieldPolicyBuilder.SKIP_VALIDATION);
        policyMap.put("comments", FieldPolicyBuilder.newBuilder().addStandardizer(RemoveControlCharsStandardizer.instance()).constrainLength(0, 255).build());

        policyMap.get("birthdate").getValidators();
        policyMap.get("birthdate").getStandardizationPolicies();
    }
}