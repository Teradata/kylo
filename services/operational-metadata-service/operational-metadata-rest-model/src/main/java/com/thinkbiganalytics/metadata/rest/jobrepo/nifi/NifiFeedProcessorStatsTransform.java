package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-rest-model
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
