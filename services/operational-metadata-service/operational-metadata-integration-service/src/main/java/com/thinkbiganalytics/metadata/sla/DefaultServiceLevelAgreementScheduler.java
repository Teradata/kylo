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
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
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
import com.thinkbiganalytics.scheduler.QuartzScheduler;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerIdentifier;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Provides the default implementation for service level agreement scheduling.
 */
public class DefaultServiceLevelAgreementScheduler implements ServiceLevelAgreementScheduler, PostMetadataConfigAction, ClusterServiceMessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(DefaultServiceLevelAgreementScheduler.class);

    public static final String QTZ_JOB_SCHEDULED_MESSAGE_TYPE = "QTZ_JOB_SCHEDULED";

    public static final String QTZ_JOB_UNSCHEDULED_MESSAGE_TYPE = "QTZ_JOB_UNSCHEDULED";

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

    @Inject
    private ClusterService clusterService;

    private Map<ServiceLevelAgreement.ID, String> scheduledJobNames = new ConcurrentHashMap<>();


    @PostConstruct
    private void init() {
        clusterService.subscribe(this, QTZ_JOB_SCHEDULED_MESSAGE_TYPE, QTZ_JOB_SCHEDULED_MESSAGE_TYPE);
    }

    /**
     * Called on startup as part of the PostMetadataConfigAction.
     */
    @Override
    public void run() {
        log.info("PostMetadataConfigAction called for DefaultServiceLevelAgreementScheduler.  About to schedule the SLA's ");
        metadataAccess.read(() -> {
            List<? extends ServiceLevelAgreement> agreements = slaProvider.getAgreements();

            if (agreements != null) {
                log.info("About to schedule {} SLA's", agreements.size());
                for (ServiceLevelAgreement agreement : agreements) {
                    JobIdentifier jobIdentifier = slaJobName(agreement);
                    QuartzScheduler scheduler = (QuartzScheduler) jobScheduler;
                    if (!scheduler.jobExists(jobIdentifier)) {
                        scheduleServiceLevelAgreement(agreement);
                    } else {
                        scheduledJobNames.put(agreement.getId(), jobIdentifier.getName());
                    }
                }
            } else {
                log.info("No SLA's found to schedule.");
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
            }
            QuartzScheduler scheduler = (QuartzScheduler) jobScheduler;
            if (scheduledJobId != null && scheduler.jobExists(scheduledJobId)) {

                log.info("Unscheduling sla job " + scheduledJobId.getName());
                jobScheduler.deleteJob(scheduledJobId);
                scheduledJobNames.remove(slaId);
                unscheduled = true;
                if (clusterService.isClustered()) {
                    clusterService.sendMessageToOthers(QTZ_JOB_UNSCHEDULED_MESSAGE_TYPE, new ScheduledServiceLevelAgreementClusterMessage(slaId, scheduledJobId));
                }
            } else {
                log.info("Unable to unschedule/delete the SLA job.  Referencing SLA Id: {}.  It doesn't exist. Scheduled Jobs are: {}  ", slaId, scheduledJobNames);
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

    private TriggerIdentifier triggerIdentifier(JobIdentifier jobIdentifier) {
        TriggerIdentifier triggerIdentifier = new DefaultTriggerIdentifier(jobIdentifier.getName(), jobIdentifier.getGroup());
        return triggerIdentifier;
    }

    private TriggerIdentifier triggerIdentifier(String name) {
        TriggerIdentifier triggerIdentifier = new DefaultTriggerIdentifier(name, "SLA");
        return triggerIdentifier;
    }

    private ServiceLevelAgreement.ID slaIdForJobIdentifier(JobIdentifier jobIdentifier) {
        String jobIdentifierName = jobIdentifier.getName();
        return scheduledJobNames.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(jobIdentifierName)).map(entry -> entry.getKey()).findFirst().orElse(null);

    }

    /**
     * Used to disable the schedule of the SLA, so that it no longer executes until subsequently re-enabled
     *
     * @param sla The SLA to disable
     */
    public void disableServiceLevelAgreement(ServiceLevelAgreement sla) {
        ServiceLevelAgreement.ID slaId = sla.getId();
        JobIdentifier scheduledJobId = null;
        if (scheduledJobNames.containsKey(slaId)) {
            scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
        } else {
            scheduledJobId = jobIdentifierForName(sla.getName());
        }
        QuartzScheduler scheduler = (QuartzScheduler) jobScheduler;
        if (scheduledJobId != null && scheduler.jobExists(scheduledJobId)) {
            try {
                jobScheduler.pauseTriggersOnJob(scheduledJobId);
            } catch (JobSchedulerException e) {
                log.error("Unable to pause the schedule for the disabled SLA {} ", sla.getName());
            }
        } else {
            log.info("Unable to pause the SLA job {} .  The Job does not exist", sla.getName());
        }

    }

    /**
     * Used to enable the schedule of the SLA, so that once again executes after a being disabled
     *
     * @param sla The SLA to enable
     */
    public void enableServiceLevelAgreement(ServiceLevelAgreement sla) {

        ServiceLevelAgreement.ID slaId = sla.getId();
        JobIdentifier scheduledJobId = null;
        if (scheduledJobNames.containsKey(slaId)) {
            scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
        } else {
            scheduledJobId = jobIdentifierForName(sla.getName());
        }
        QuartzScheduler scheduler = (QuartzScheduler) jobScheduler;
        if (scheduledJobId != null && scheduler.jobExists(scheduledJobId)) {
            try {
                jobScheduler.resumeTriggersOnJob(scheduledJobId);
            } catch (JobSchedulerException e) {
                log.error("Unable to resume the schedule for the SLA {} ", sla.getName());
            }
        } else {
            log.info("Unable to resume the SLA job {} .  The Job does not exist", sla.getName());
        }
    }

    /**
     * Schedule the SlaQuartzJobBean for the given sla
     *
     * @param jobIdentifier the job identifier for this schedule
     * @param slaId         the SLA id
     */
    private void scheduleSlaJob(JobIdentifier jobIdentifier, ServiceLevelAgreement.ID slaId) {

        QuartzScheduler scheduler = (QuartzScheduler) jobScheduler;
        TriggerIdentifier triggerIdentifier = triggerIdentifier(jobIdentifier);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SlaQuartzJobBean.SLA_ID_PARAM, slaId);
        try {
            scheduler.scheduleJob(jobIdentifier, triggerIdentifier, SlaQuartzJobBean.class, (StringUtils.isBlank(defaultCron) ? DEFAULT_CRON : defaultCron), map);
        } catch (SchedulerException e) {
            throw new RuntimeException("Error scheduling job", e);
        }
    }


    /**
     * Schedules an SLA to be run
     *
     * @param sla The SLA to schedule
     */
    public void scheduleServiceLevelAgreement(ServiceLevelAgreement sla) {
        if (scheduledJobNames.containsKey(sla.getId())) {
            unscheduleServiceLevelAgreement(sla);
        }
        JobIdentifier jobIdentifier = slaJobName(sla);
        ServiceLevelAgreement.ID slaId = sla.getId();
        //schedule the job
        scheduleSlaJob(jobIdentifier, slaId);
        log.info("Schedule sla job " + jobIdentifier.getName());
        scheduledJobNames.put(sla.getId(), jobIdentifier.getName());
        //notify the other schedulers in the cluster of the scheduled job name
        if (clusterService.isClustered()) {
            clusterService.sendMessageToOthers(QTZ_JOB_SCHEDULED_MESSAGE_TYPE, new ScheduledServiceLevelAgreementClusterMessage(slaId, jobIdentifier));
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


    /**
     * Keep the job name cache in sync across clusters
     *
     * @param from    cluser address sending the message
     * @param message the message
     */
    @Override
    public void onMessageReceived(String from, ClusterMessage message) {

        if (QTZ_JOB_SCHEDULED_MESSAGE_TYPE.equalsIgnoreCase(message.getType())) {
            ScheduledServiceLevelAgreementClusterMessage msg = (ScheduledServiceLevelAgreementClusterMessage) message.getMessage();
            log.info("Received message {}, slaId: {}, jobId:{} ", message.getType(), msg.getSlaId(), msg.getJobIdentifier().getName());
            scheduledJobNames.put(msg.getSlaId(), msg.getJobIdentifier().getName());
        } else if (QTZ_JOB_UNSCHEDULED_MESSAGE_TYPE.equalsIgnoreCase(message.getType())) {
            ScheduledServiceLevelAgreementClusterMessage msg = (ScheduledServiceLevelAgreementClusterMessage) message.getMessage();
            log.info("Received message {}, slaId: {}, jobId:{} ", message.getType(), msg.getSlaId(), msg.getJobIdentifier().getName());
            scheduledJobNames.remove(msg.getSlaId());
        }
    }


}
