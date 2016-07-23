package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;

import org.apache.commons.lang3.StringUtils;
import org.modeshape.jcr.ModeShapeEngine;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 7/22/16.
 */
public class JcrServiceLevelAgreementScheduler implements ServiceLevelAgreementScheduler {

    private String DEFAULT_CRON = "0 0/5 * 1/1 * ? *";// every 5 min
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


    @PostConstruct
    public void scheduleServiceLevelAgreements() {

        modeshapeAvailableTimer = new Timer();
        modeshapeAvailableTimer.schedule(new QuerySla(), 0, 10 * 1000);


    }

    class QuerySla extends TimerTask {

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


    public void scheduleServiceLevelAgreement(ServiceLevelAgreement sla) {

        //TODO Remove any existing schedules for this sla

        String slaId = sla.getId().toString();
        for (ServiceLevelAgreementCheck check : sla.getSlaChecks()) {
            String cron = check.getCronSchedule();
            if (StringUtils.isBlank(cron)) {
                cron = DEFAULT_CRON;
            }

            JobIdentifier jobIdentifier = new DefaultJobIdentifier(sla.getName(), "SLA");
            try {
                jobScheduler.scheduleWithCronExpression(jobIdentifier, new Runnable() {
                    @Override
                    public void run() {

                        //query for this SLA
                        metadataAccess.read(new Command<Object>() {
                            @Override
                            public Object execute() {
                                ServiceLevelAgreement sla = slaProvider.getAgreement(slaProvider.resolve(slaId));
                                slaChecker.checkAgreement(sla);
                                return null;
                            }
                        });


                    }
                }, cron);
            } catch (JobSchedulerException e) {
                e.printStackTrace();
            }
        }

    }


}
