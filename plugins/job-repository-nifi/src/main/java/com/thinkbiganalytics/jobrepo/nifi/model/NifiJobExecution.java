package com.thinkbiganalytics.jobrepo.nifi.model;


import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 2/26/16.
 */
public class NifiJobExecution extends RunStatusContext implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(NifiJobExecution.class);

    private String feedName;
    private FlowFileEvents flowFile;

    private Long jobInstanceId;
    private Long jobExecutionId;
    private Date createTime;
    private Date lastUpdated;
    private Integer version;
    private ExitStatus exitStatus;
    private BatchStatus status;
    private Map<String, String> jobParameters;

    private String jobType = FeedConstants.PARAM_VALUE__JOB_TYPE_FEED;

    private Map<Long, BulletinDTO> processedBulletinErrors = new HashMap<>();

    private Set<String> endingProcessorComponentIds = new HashSet<>();

    private AtomicInteger endingProcessorCount = new AtomicInteger(0);

    private Set<FlowFileComponent> failedComponents;

    private List<FlowFileComponent> componentOrder = new ArrayList<>();

    private boolean jobExecutionContextSet;

    private Map<String, Object> jobExecutionContextMap = new HashMap<>();


    public NifiJobExecution(String feedName, ProvenanceEventRecordDTO event) {
        this.feedName = feedName;
        this.flowFile = event.getFlowFile();
        this.flowFile.setNifiJobExecution(this);
        this.createTime = DateTimeUtil.getUTCTime();
        this.lastUpdated = DateTimeUtil.getUTCTime();
        if (event.getAttributeMap() != null) {
            jobParameters = new HashMap<>(event.getAttributeMap());
        } else {
            jobParameters = new HashMap<>();
        }
        //bootstrap the feed parameters
        jobParameters.put(FeedConstants.PARAM__FEED_NAME, feedName);
        jobParameters.put(FeedConstants.PARAM__JOB_TYPE, FeedConstants.PARAM_VALUE__JOB_TYPE_FEED);
        jobParameters.put(FeedConstants.PARAM__FEED_IS_PARENT, "true");
        this.jobType = FeedConstants.PARAM_VALUE__JOB_TYPE_FEED;

    }

    public NifiJobExecution( ExecutedJob job) {

        this.createTime = job.getCreateTime().toDate();
        this.lastUpdated = job.getLastUpdated().toDate();
        jobParameters = new HashMap<>();
        if (job.getJobParameters() != null) {
            for(Map.Entry<String,Object> jobParamsEntry: job.getJobParameters().entrySet()){
                jobParameters.put(jobParamsEntry.getKey(),jobParamsEntry.getValue() != null ? jobParamsEntry.getValue().toString() : null);
            }
        } else {
            jobParameters = new HashMap<>();
        }
        this.jobType = jobParameters.containsKey(FeedConstants.PARAM__JOB_TYPE) ? jobParameters.get(FeedConstants.PARAM__JOB_TYPE).toString() : FeedConstants.PARAM_VALUE__JOB_TYPE_FEED;
        this.feedName = jobParameters.containsKey(FeedConstants.PARAM__FEED_NAME) ? jobParameters.get(FeedConstants.PARAM__FEED_NAME).toString() : null;
        job.setExecutionId(job.getExecutionId());
        if(job.getExecutionContext() != null){
            jobExecutionContextMap.putAll(job.getExecutionContext());
        }
        this.setJobExecutionId(job.getExecutionId());
        this.setJobInstanceId(job.getJobId());

    }

    public void componentComplete(String componentId) {
        if (endingProcessorComponentIds.contains(componentId)) {
            endingProcessorComponentIds.remove(componentId);
            endingProcessorCount.decrementAndGet();
            LOG.info("Completed Ending Processor " + componentId + " " + endingProcessorCount.get() + " ending processors remain. ");
        }
    }

    public void setFlowFile(FlowFileEvents flowFile) {
        this.flowFile = flowFile;
    }

    public Set<String> getEndingProcessorComponentIds() {
        return endingProcessorComponentIds;
    }

    public void setEndingProcessorComponentIds(Set<String> endingProcessorComponentIds) {
        this.endingProcessorComponentIds = endingProcessorComponentIds;
    }

    public Integer getEndingProcessorCount() {
        return endingProcessorCount.get();
    }

    public void setEndingProcessorCount(Integer count) {
        this.endingProcessorCount.set(count);
    }

    public Integer decrementEndingProcessorCount() {
        return this.endingProcessorCount.decrementAndGet();
    }

    public String getFeedName() {
        return feedName;
    }

    public FlowFileEvents getFlowFile() {
        return flowFile;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getVersion() {
        return version;
    }

    public ExitStatus getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = exitStatus;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public void markStarted() {
        this.setStatus(BatchStatus.STARTED);
        this.exitStatus = ExitStatus.EXECUTING;
        this.markRunning();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getJobParameters() {
        return jobParameters;
    }

    public Set<FlowFileComponent> getFailedComponents() {
        if (failedComponents == null) {
            failedComponents = new HashSet<>();
        }
        return failedComponents;
    }

    public boolean containsComponent(String componentId) {
        return flowFile.containsComponent(componentId);
    }

    public FlowFileComponent getComponent(String componentId) {
        return flowFile.getComponent(componentId);
    }

    public Set<FlowFileComponent> getComponents() {
        return flowFile.getAllComponents();
    }

    public boolean hasFailedComponents() {
        return !getFailedComponents().isEmpty();
    }

    public void addFailedComponent(FlowFileComponent flowFileComponent) {
        getFailedComponents().add(flowFileComponent);
    }

    /**
     * Increment the version number
     */
    public void incrementVersion() {
        if (version == null) {
            version = 0;
        } else {
            version = version + 1;
        }
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void addBulletinErrors(Collection<BulletinDTO> dtos) {
        for (BulletinDTO dto : dtos) {
            LOG.info("Adding Processed Bulletin level: {} - category: {}, sourceId: {}, message: {} ", dto.getLevel(), dto.getCategory(), dto.getSourceId(), dto.getMessage());
            processedBulletinErrors.put(dto.getId(), dto);
        }
    }

    public void addBulletinError(BulletinDTO dto) {
        processedBulletinErrors.put(dto.getId(), dto);
    }

    public boolean isBulletinProcessed(BulletinDTO dto) {
        return processedBulletinErrors.containsKey(dto.getId());
    }

    public void addComponentToOrder(FlowFileComponent component) {
        componentOrder.add(component);
    }

    public List<FlowFileComponent> getComponentOrder() {
        return componentOrder;
    }

    public boolean isJobExecutionContextSet() {
        return jobExecutionContextSet;
    }

    public void setJobExecutionContextSet(boolean jobExecutionContextSet) {
        this.jobExecutionContextSet = jobExecutionContextSet;
    }

    public Map<String, Object> getJobExecutionContextMap() {
        return jobExecutionContextMap;
    }

    public void setJobExecutionContextMap(Map<String, Object> jobExecutionContextMap) {
        this.jobExecutionContextMap = jobExecutionContextMap;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public boolean isCheckDataJob() {
        return FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType);
    }
}
