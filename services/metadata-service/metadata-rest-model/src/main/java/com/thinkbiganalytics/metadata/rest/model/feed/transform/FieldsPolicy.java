/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed.transform;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
public class FieldsPolicy {
    
    List<FieldPolicy> fieldPolicies = new ArrayList<>();

    public List<FieldPolicy> getFieldPolicies() {
        return fieldPolicies;
    }

    public void setFieldPolicies(List<FieldPolicy> fieldPolicies) {
        this.fieldPolicies = fieldPolicies;
    }
    
    public void addPolicy(FieldPolicy policy) {
        this.fieldPolicies.add(policy);
    }
}
