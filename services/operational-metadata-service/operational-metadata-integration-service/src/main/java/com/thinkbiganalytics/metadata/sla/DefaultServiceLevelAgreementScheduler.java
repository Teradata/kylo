package com.thinkbiganalytics.metadata.sla;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 7/22/16.
 */
public class DefaultServiceLevelAgreementScheduler implements ServiceLevelAgreementScheduler, ModeShapeAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultServiceLevelAgreementScheduler.class);

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
    private OperationalMetadataAccess operationalMetadataAccess;

    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    private ModeShapeAvailability modeShapeAvailability;


    private Map<ServiceLevelAgreement.ID, String> scheduledJobNames = new ConcurrentHashMap<>();


    @PostConstruct
    public void scheduleServiceLevelAgreements() {
        modeShapeAvailability.subscribe(this);
    }

    @Override
    public void modeShapeAvailable() {
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

    public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement sla) {
        boolean unscheduled = false;
        if (sla != null) {
            unscheduled = unscheduleServiceLevelAgreement(sla.getId());
        }
        return unscheduled;
    }


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

    public void disableServiceLevelAgreement(ServiceLevelAgreement sla){

    ServiceLevelAgreement.ID slaId = sla.getId();
        if (scheduledJobNames.containsKey(slaId)) {
            JobIdentifier  scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
            try {
                jobScheduler.pauseTriggersOnJob(scheduledJobId);
            }catch (JobSchedulerException e){
                log.error("Unable to pause the schedule for the disabled SLA {} ",sla.getName());
            }
        }
    }

    public void enableServiceLevelAgreement(ServiceLevelAgreement sla){

        ServiceLevelAgreement.ID slaId = sla.getId();
        if (scheduledJobNames.containsKey(slaId)) {
            JobIdentifier  scheduledJobId = jobIdentifierForName(scheduledJobNames.get(slaId));
            try {
                jobScheduler.resumeTriggersOnJob(scheduledJobId);
            }catch (JobSchedulerException e){
                log.error("Unable to resume the schedule for the enabled SLA {} ",sla.getName());
            }
        }
    }


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
                            operationalMetadataAccess.commit(() -> {
                                slaChecker.checkAgreement(sla);
                                return null;
                            });
                        }
                        else {
                            log.info("SLA {} will not fire since it is disabled ",sla.getName());
                        }
                }

                ,MetadataAccess.SERVICE);


            }
        }, (StringUtils.isBlank(defaultCron) ? DEFAULT_CRON : defaultCron));
            log.debug("Schedule sla job " + jobIdentifier.getName());
            scheduledJobNames.put(sla.getId(), jobIdentifier.getName());
        } catch (JobSchedulerException e) {
            throw new RuntimeException(e);
        }

        if(!sla.isEnabled()){
            disableServiceLevelAgreement(sla);
        }


    }


}
