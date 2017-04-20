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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerEvent;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

/**
 * Provides the default implementation for service level agreement scheduling.
 */
public class DefaultServiceLevelAgreementScheduler implements ServiceLevelAgreementScheduler, PostMetadataConfigAction {

    private static final Logger log = LoggerFactory.getLogger(DefaultServiceLevelAgreementScheduler.class);
    @Inject
    ServiceLevelAgreementProvider slaProvider;
    private String DEFAULT_CRON = "0 0/5 * 1/1 * ? *";// every 5 min
    @Value("${sla.cron.default:0 0/5 * 1/1 * ? *}")
    private String defaultCron;
    @Inject
    private JobScheduler jobScheduler;
    @Inject
    private ServiceLevelAgreementChecker slaChecker;
    @Inject
    private MetadataAccess metadataAccess;


    private Map<ServiceLevelAgreement.ID, String> scheduledJobNames = new ConcurrentHashMap<>();


    @Override
    public void run() {
        metadataAccess.read(() -> {
            List<? extends ServiceLevelAgreement> agreements = slaProvider.getAgreements();

            if (agreements != null) {
                for (ServiceLevelAgreement agreement : agreements) {
                    scheduleServiceLevelAgreement(agreement);
                }
            }

            return null;
        }, MetadataAccess.SERVICE);
    }

    private String getUniqueName(String name) {
        String uniqueName = name;
        final String checkName = name;
        String matchingName = Iterables.tryFind(scheduledJobNames.values(), new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.equalsIgnoreCase(checkName);
            }
        }).orNull();
        if (matchingName != null) {
            //get numeric string after '-';
            if (StringUtils.contains(matchingName, "-")) {
                String number = StringUtils.substringAfterLast(matchingName, "-");
                if (StringUtils.isNotBlank(number)) {
                    number = StringUtils.trim(number);
                    if (StringUtils.isNumeric(number)) {
                        Integer num = Integer.parseInt(number);
                        num++;
                        uniqueName += "-" + num;
                    } else {
                        uniqueName += "-1";
                    }
                }
            } else {
                uniqueName += "-1";
            }
        }
        return uniqueName;
    }

    /**
     * removes a SLA from the scheduler, so that it is no longer executed.
     *
     * @param sla The Service Level Agreement object
     * @return true if we were able to remove the SLA from the scheduler
     */
    public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement sla) {
        boolean unscheduled = false;
        if (sla != null) {
            unscheduled = unscheduleServiceLevelAgreement(sla.getId());
        }
        return unscheduled;
    }

    /**
     * removes the SLA, identified by slaId, from the scheduler, so that it is no longer executed.
     *
     * @param slaId The ServiceLevelAgreement id
     * @return true if we were able to remove the SLA from the scheduler
     */
    public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement.ID slaId) {
        boolean unscheduled = false;
        JobIdentifier scheduledJobId = null;
        try {
            if (scheduledJobNames.containsKey(slaId)) {
                scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
                log.debug("Unscheduling sla job " + scheduledJobId.getName());
                jobScheduler.deleteJob(scheduledJobId);
                scheduledJobNames.remove(slaId);
                unscheduled = true;
            }
        } catch (JobSchedulerException e) {
            log.error("Unable to delete the SLA Job " + scheduledJobId);
        }
        return unscheduled;
    }


    private JobIdentifier slaJobName(ServiceLevelAgreement sla) {
        String name = sla.getName();
        if (scheduledJobNames.containsKey(sla.getId())) {
            name = scheduledJobNames.get(sla.getId());
        } else {
            //ensure the name is unique in the saved ist
            name = getUniqueName(name);
        }
        return jobIdentifierForName(name);
    }

    private JobIdentifier jobIdentifierForName(String name) {
        JobIdentifier jobIdentifier = new DefaultJobIdentifier(name, "SLA");
        return jobIdentifier;
    }

    private ServiceLevelAgreement.ID slaIdForJobIdentifier(JobIdentifier jobIdentifier) {
        String jobIdentifierName = jobIdentifier.getName();
        return scheduledJobNames.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(jobIdentifierName)).map(entry -> entry.getKey()).findFirst().orElse(null);

    }

    /**
     * Used to disable the schedule of the SLA, so that it no longer executes until subsequently re-enabled via {@link enableServiceLevelAgreement}
     *
     * @param sla The SLA to disable
     */
    public void disableServiceLevelAgreement(ServiceLevelAgreement sla) {
        ServiceLevelAgreement.ID slaId = sla.getId();
        if (scheduledJobNames.containsKey(slaId)) {
            JobIdentifier scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
            try {
                jobScheduler.pauseTriggersOnJob(scheduledJobId);
            } catch (JobSchedulerException e) {
                log.error("Unable to pause the schedule for the disabled SLA {} ", sla.getName());
            }
        }
    }

    /**
     * Used to enable the schedule of the SLA, so that once again executes after a being disabled via {@link disableServiceLevelAgreement}
     *
     * @param sla The SLA to enable
     */
    public void enableServiceLevelAgreement(ServiceLevelAgreement sla) {

        ServiceLevelAgreement.ID slaId = sla.getId();
        if (scheduledJobNames.containsKey(slaId)) {
            JobIdentifier scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
            try {
                jobScheduler.resumeTriggersOnJob(scheduledJobId);
            } catch (JobSchedulerException e) {
                log.error("Unable to resume the schedule for the enabled SLA {} ", sla.getName());
            }
        }
    }

    /**
     * Schedules an SLA to be run
     *
     * @param sla The SLA to schedule
     */
    public void scheduleServiceLevelAgreement(ServiceLevelAgreement sla) {
        try {
            //Delete any jobs with this SLA if they already exist
            if (scheduledJobNames.containsKey(sla.getId())) {
                unscheduleServiceLevelAgreement(sla);
            }
            JobIdentifier jobIdentifier = slaJobName(sla);
            ServiceLevelAgreement.ID slaId = sla.getId();

            jobScheduler.scheduleWithCronExpression(jobIdentifier, new Runnable() {
                @Override
                public void run() {
                    //query for this SLA
                    metadataAccess.commit(() -> {
                        ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
                        if (sla == null) {
                            ///Unable to find the SLA... Remove the SLA from teh schedule
                            unscheduleServiceLevelAgreement(slaId);

                        }
                        if (sla.isEnabled()) {
                            slaChecker.checkAgreement(sla);
                        } else {
                            log.info("SLA {} will not fire since it is disabled ", sla.getName());
                        }
                    }, MetadataAccess.SERVICE);
                }
            }, (StringUtils.isBlank(defaultCron) ? DEFAULT_CRON : defaultCron));

            log.debug("Schedule sla job " + jobIdentifier.getName());
            scheduledJobNames.put(sla.getId(), jobIdentifier.getName());
        } catch (JobSchedulerException e) {
            throw new RuntimeException(e);
        }

        if (!sla.isEnabled()) {
            disableServiceLevelAgreement(sla);
        }
    }

    /**
     * Called be the framework when the job is scheduled this is where we manage the life cycle of the SLAs
     *
     * @param event the job event.
     */
    public void onJobSchedulerEvent(JobSchedulerEvent event) {
        try {
            switch (event.getEvent()) {
                case PAUSE_JOB:
                    pauseServiceLevelAgreement(event);
                    break;
                case RESUME_JOB:
                    resumeServiceLevelAgreement(event);
                    break;
                case PAUSE_ALL_JOBS:
                    pauseAllServiceLevelAgreements();
                    break;
                case RESUME_ALL_JOBS:
                    resumeAllServiceLevelAgreements();
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            log.error("Error processing JobScheduler Event {}", event, e);
        }
    }

    private void resumeServiceLevelAgreement(JobSchedulerEvent event) {
        ServiceLevelAgreement.ID slaId = slaIdForJobIdentifier(event.getJobIdentifier());
        if (slaId != null) {
            metadataAccess.commit(() -> enable(slaId), MetadataAccess.SERVICE);
        }
    }

    private void pauseServiceLevelAgreement(JobSchedulerEvent event) {
        ServiceLevelAgreement.ID slaId = slaIdForJobIdentifier(event.getJobIdentifier());
        if (slaId != null) {
            metadataAccess.commit(() -> disable(slaId), MetadataAccess.SERVICE);
        }
    }

    private void pauseAllServiceLevelAgreements() {
        metadataAccess.commit(() -> {
            scheduledJobNames.keySet().stream().forEach(slaId -> disable(slaId));
        }, MetadataAccess.SERVICE);
    }

    private void resumeAllServiceLevelAgreements() {
        metadataAccess.commit(() -> {
            scheduledJobNames.keySet().stream().forEach(slaId -> enable(slaId));
        }, MetadataAccess.SERVICE);
    }

    private void enable(ServiceLevelAgreement.ID slaId) {
        findAgreement(slaId).ifPresent(sla -> ((JcrServiceLevelAgreement) sla).setEnabled(true));
    }

    private void disable(ServiceLevelAgreement.ID slaId) {
        findAgreement(slaId).ifPresent(sla -> ((JcrServiceLevelAgreement) sla).setEnabled(false));
    }

    /**
     * Must be called inside a metadatAccess wrapper
     */
    private Optional<ServiceLevelAgreement> findAgreement(ServiceLevelAgreement.ID slaId) {
        ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
        return Optional.ofNullable(sla);
    }
}
