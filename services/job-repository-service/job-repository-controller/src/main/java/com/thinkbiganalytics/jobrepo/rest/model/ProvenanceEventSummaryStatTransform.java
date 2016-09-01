package com.thinkbiganalytics.jobrepo.rest.model;

import com.thinkbiganalytics.jobrepo.repository.rest.model.ProvenanceEventSummaryStats;

import org.apache.commons.beanutils.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/24/16.
 */
public class ProvenanceEventSummaryStatTransform {

    public ProvenanceEventSummaryStatTransform() {

    }


    public static List<ProvenanceEventSummaryStats> toModel(List<? extends com.thinkbiganalytics.jobrepo.model.ProvenanceEventSummaryStats> domains) {

        if (domains != null && !domains.isEmpty()) {
            return domains.stream().map(domain -> toModel(domain)).collect(Collectors.toList());
        }
        return null;
    }

    public static ProvenanceEventSummaryStats toModel(com.thinkbiganalytics.jobrepo.model.ProvenanceEventSummaryStats domain) {
        ProvenanceEventSummaryStats stats = new ProvenanceEventSummaryStats();
        try {
            BeanUtils.copyProperties(stats, domain);
        } catch (Exception e) {
            //TODO LOG IT
        }
        return stats;
    }

}
