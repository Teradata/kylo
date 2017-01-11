package com.thinkbiganalytics.common.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 1/10/17.
 */
public class KyloProcessorFlowTypeRelationship {

    public static String ALL_RELATIONSHIP = "all";

    public static String FAILURE_RELATIONSHIP = "failure";

    public static String SUCCESS_RELATIONSHIP = "success";

    public static KyloProcessorFlowTypeRelationship DEFAULT = new KyloProcessorFlowTypeRelationship(ALL_RELATIONSHIP, KyloProcessorFlowType.NORMAL_FLOW);

    public static KyloProcessorFlowTypeRelationship CRITICAL_FAILURE = new KyloProcessorFlowTypeRelationship(FAILURE_RELATIONSHIP, KyloProcessorFlowType.CRITICAL_FAILURE);

    public static Set<KyloProcessorFlowTypeRelationship> DEFAULT_SET = newHashSet(KyloProcessorFlowTypeRelationship.DEFAULT);

    public static Set<KyloProcessorFlowTypeRelationship> CRITICAL_FAILURE_SET = newHashSet(KyloProcessorFlowTypeRelationship.CRITICAL_FAILURE);

    public KyloProcessorFlowTypeRelationship() {

    }

    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet set = new HashSet();
        Collections.addAll(set, elements);
        return set;
    }

    public KyloProcessorFlowTypeRelationship(String relationship, KyloProcessorFlowType flowType) {
        this.relationship = relationship;
        this.flowType = flowType;
    }

    private String relationship;
    private KyloProcessorFlowType flowType;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public KyloProcessorFlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(KyloProcessorFlowType flowType) {
        this.flowType = flowType;
    }

    public static boolean isNormalFlowSet(Set<KyloProcessorFlowTypeRelationship> relationships) {
        return relationships.stream().allMatch(relationship -> KyloProcessorFlowType.NORMAL_FLOW.equals(relationship.getFlowType()));
    }
}
