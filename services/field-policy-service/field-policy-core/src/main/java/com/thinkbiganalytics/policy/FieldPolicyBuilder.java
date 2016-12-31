package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Vector;


public class FieldPolicyBuilder {

    public static final FieldPolicy SKIP_VALIDATION = FieldPolicyBuilder.newBuilder().skipSchemaValidation().build();

    private FieldPolicy policy = new FieldPolicy();
    private List<ValidationPolicy> validators = new Vector<>();
    private List<StandardizationPolicy> standardizationPolicies = new Vector<>();

    private FieldPolicyBuilder() {
        super();
    }

    public static FieldPolicyBuilder newBuilder() {
        return new FieldPolicyBuilder();
    }

    public FieldPolicyBuilder addValidator(ValidationPolicy validator) {
        validators.add(validator);
        return this;
    }

    public FieldPolicyBuilder addValidators(List<ValidationPolicy> validator) {
        validators.addAll(validator);
        return this;
    }

    public FieldPolicyBuilder addStandardizers(List<StandardizationPolicy> policyList) {
        standardizationPolicies.addAll(policyList);
        return this;
    }

    public FieldPolicyBuilder addStandardizer(StandardizationPolicy policy) {
        standardizationPolicies.add(policy);
        return this;
    }

    public FieldPolicyBuilder fieldName(String fieldName) {
        policy.setField(fieldName);
        return this;
    }

    public FieldPolicyBuilder disallowNullOrEmpty() {
        policy.setNullable(false);
        return this;
    }

    public FieldPolicyBuilder feedFieldName(String feedFieldName) {
        this.policy.setFeedField(feedFieldName);
        return this;
    }


    /**
     * Constrains value to one of the values provided in the list
     *
     * public FieldPolicyBuilder constrainValues(String... values) { validators.add(new LookupValidator(values)); return this; }
     *
     * public FieldPolicyBuilder constrainLength(int minlen, int maxlen) { validators.add(new LengthValidator(minlen, maxlen));
     * return this; }
     */

    public FieldPolicyBuilder skipSchemaValidation() {
        policy.setSkipSchemaValidation(true);
        return this;
    }

    public FieldPolicyBuilder enableDiscoverTypes() {
        policy.setTypeDiscovery(true);
        return this;
    }

    public FieldPolicy build() {
        FieldPolicy newPolicy = this.policy;
        if (StringUtils.isEmpty(newPolicy.getFeedField())) {
            newPolicy.setFeedField(policy.getFeedField());
        }
        newPolicy.setValidators(validators);
        newPolicy.setStandardizationPolicies(standardizationPolicies);
        return newPolicy;
    }

    /*
    FieldPolicyBuilder constrainRange(Number min, Number max) {
        policy.addValidator(new RangeValidator(min, max));
        return this;
    }

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
    }   */
}