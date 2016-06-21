package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.ApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.JobRepoApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * On Startup this class will check to see if there are any Jobs that were in a STARTED/STARTING state prior to the restart. It will then attach Nifi Provenance Events and associate those events with
 * the correct JOB EXECUTION and STEP EXECUTIONS and merge them in with the JMS queue that is listening for other provenance events.
 *
 * Once it completes the startup it will then notify all listeners that it is complete
 */
public class ProvenanceEventApplicationStartupListener implements ApplicationStartupListener {


    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventApplicationStartupListener.class);

    @Inject
    JobRepoApplicationStartupListener jobRepoApplicationStartupListener;


    @Inject
    private JobRepository jobRepository;

    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    NifiRestClient nifiRestClient;

    @Autowired
    ProvenanceEventExecutedJob provenanceEventExecutedJob;

    private Boolean isProcessingStartupJobs = null;

    private DateTime startTime;

    private List<ProvenanceEventJobExecutionStartupListener> jobExecutionStartupListeners = new ArrayList<>();

    @PostConstruct
    private void init() {
        jobRepoApplicationStartupListener.subscribe(this);
    }

    public void subscribe(ProvenanceEventJobExecutionStartupListener listener) {
        this.jobExecutionStartupListeners.add(listener);
    }


    public boolean isFinishedProcessingStartupJobs() {
        return isProcessingStartupJobs != null && isProcessingStartupJobs == false;
    }


    @Override
    public void onStartup(DateTime startTime) {
        this.startTime = startTime;
        log.info("*******APPLICATION STARTUP @ {} ****** ", startTime);
        log.info("*******Looking for previously running Jobs prior to startTime {} in a new Thread", startTime);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                processEventsFromStartTime();
            }
        });
        thread.start();
    }

    public void processEventsFromStartTime() {
        if (startTime != null) {
            isProcessingStartupJobs = true;
            log.info("Looking for Jobs that were running prior to start Time of {} ", startTime);

            List<ExecutedJob> jobs = jobRepository.findJobsCurrentStartedBeforeSpecifiedTime(startTime);
            if (jobs != null) {
                log.info("Found {} Jobs that were running prior to start Time of {} ", jobs.size(), startTime);
                try {
                    processStartupJobExecutions(jobs);
                } catch (NifiConnectionException e) {
                    log.info("Nifi Connection Error... abort matching existing running jobs with Nifi Events ");
                }
                //if we cant get any FlowFileEvents then mark the jobs as abandoned?
                //jobService.abandonJobExecution();
            } else {
                log.info("Found 0 Jobs that were running prior to start Time of {} ", startTime);
            }
            for (ProvenanceEventJobExecutionStartupListener listener : jobExecutionStartupListeners) {
                listener.onEventsInitialized();
            }
            isProcessingStartupJobs = false;
        }
    }

    private void processStartupJobExecutions(List<ExecutedJob> jobs) {
        for (ExecutedJob job : jobs) {
            try {
                provenanceEventExecutedJob.processExecutedJob(job);
            } catch (NifiConnectionException e) {
                for (ProvenanceEventJobExecutionStartupListener listener : jobExecutionStartupListeners) {
                    listener.onStartupConnectionError();
                }
                break;
            }
        }
    }


    public void setJobRepoApplicationStartupListener(JobRepoApplicationStartupListener jobRepoApplicationStartupListener) {
        this.jobRepoApplicationStartupListener = jobRepoApplicationStartupListener;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
}
