package com.thinkbiganalytics.metadata.modeshape.sla;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;

import org.apache.commons.lang.StringUtils;
import org.modeshape.jcr.ModeShapeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 7/22/16.
 */
public class JcrServiceLevelAgreementScheduler implements ServiceLevelAgreementScheduler {

    private static final Logger log = LoggerFactory.getLogger(JcrServiceLevelAgreementScheduler.class);

    private String DEFAULT_CRON = "0 0/1 * 1/1 * ? *";// every 5 min
    @Inject
    private JobScheduler jobScheduler;

    @Inject
    private ServiceLevelAgreementChecker slaChecker;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    private ModeShapeEngine modeShapeEngine;

    Timer modeshapeAvailableTimer;

    private Map<ServiceLevelAgreement.ID, String> scheduledJobNames = new ConcurrentHashMap<>();


    @PostConstruct
    public void scheduleServiceLevelAgreements() {
        modeshapeAvailableTimer = new Timer();
        modeshapeAvailableTimer.schedule(new QueryAndScheduleServiceLevelAgreementsTask(), 0, 10 * 1000);
    }

    class QueryAndScheduleServiceLevelAgreementsTask extends TimerTask {

        public void run() {
            if (ModeShapeEngine.State.RUNNING.equals(modeShapeEngine.getState())) {
                modeshapeAvailableTimer.cancel();
                metadataAccess.read(new Command<Object>() {
                    @Override
                    public Object execute() {
                        List<ServiceLevelAgreement> agreements = slaProvider.getAgreements();
                        if (agreements != null) {
                            for (ServiceLevelAgreement agreement : agreements) {
                                scheduleServiceLevelAgreement(agreement);
                            }
                        }
                        return null;
                    }
                });
            }

        }

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

    public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement sla){
        boolean unscheduled = false;
        JobIdentifier scheduledJobId = null;
        try {
            if (scheduledJobNames.containsKey(sla.getId())) {

            scheduledJobId = slaJobName(sla);
            log.info("Unscheduling sla job " + scheduledJobId.getName());
            jobScheduler.deleteJob(scheduledJobId);
            scheduledJobNames.remove(sla.getId());
            unscheduled = true;
        }
        } catch (JobSchedulerException e) {
            log.error("Unable to delete the SLA Job "+scheduledJobId);
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
        JobIdentifier jobIdentifier = new DefaultJobIdentifier(name, "SLA");
        return jobIdentifier;
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
                        metadataAccess.commit(new Command<Object>() {
                            @Override
                            public Object execute() {
                                ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
                                slaChecker.checkAgreement(sla);
                                return null;
                            }
                        });


                    }
                }, DEFAULT_CRON);
            scheduledJobNames.put(sla.getId(), jobIdentifier.getName());
            } catch (JobSchedulerException e) {
                e.printStackTrace();
            }


    }


}
