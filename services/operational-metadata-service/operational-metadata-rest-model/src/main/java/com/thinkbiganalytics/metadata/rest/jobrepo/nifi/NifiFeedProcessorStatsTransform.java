package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;


import org.apache.commons.beanutils.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/24/16.
 */
public class NifiFeedProcessorStatsTransform {

    public NifiFeedProcessorStatsTransform() {

    }


    public static List<NifiFeedProcessorStats> toModel(List<? extends com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats> domains) {

        if (domains != null && !domains.isEmpty()) {
            return domains.stream().map(domain -> toModel(domain)).collect(Collectors.toList());
        }
        return null;
    }

    public static NifiFeedProcessorStats toModel(com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats domain) {
        NifiFeedProcessorStats stats = new NifiFeedProcessorStats();
        try {
            BeanUtils.copyProperties(stats, domain);
        } catch (Exception e) {
            //TODO LOG IT
        }
        return stats;
    }

}
