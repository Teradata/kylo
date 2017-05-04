package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Map;

import javax.inject.Inject;

/**
 * Quartz Scheduled Job Bean that will run the SLA code
 */
public class SlaQuartzJobBean extends QuartzJobBean{
    public static final String SLA_ID_PARAM = "SLA_ID";
    private static final Logger log = LoggerFactory.getLogger(SlaQuartzJobBean.class);
    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    private ServiceLevelAgreementChecker slaChecker;
    @Inject
    private MetadataAccess metadataAccess;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //query for this SLA
        final Map<String,   Object> jobDataMap = context.getMergedJobDataMap();

        metadataAccess.commit(() -> {
            ServiceLevelAgreement.ID slaId = (ServiceLevelAgreement.ID)jobDataMap.get(SLA_ID_PARAM);
            ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
            if (sla != null) {
                ///Unable to find the SLA... Remove the SLA from teh schedule
                //   unscheduleServiceLevelAgreement(slaId);
                if (sla.isEnabled()) {
                    slaChecker.checkAgreement(sla);
                } else {
                    log.info("SLA {} will not fire since it is disabled ", sla.getName());
                }
            }else {
                log.error("UNABLE TO FIND SLA for {} ",slaId);
            }
        }, MetadataAccess.SERVICE);
    }
}
