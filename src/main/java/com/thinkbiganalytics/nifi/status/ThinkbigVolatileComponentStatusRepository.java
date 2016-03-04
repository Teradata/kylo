package com.thinkbiganalytics.nifi.status;

import org.apache.nifi.controller.status.ConnectionStatus;
import org.apache.nifi.controller.status.ProcessGroupStatus;
import org.apache.nifi.controller.status.ProcessorStatus;
import org.apache.nifi.controller.status.RemoteProcessGroupStatus;
import org.apache.nifi.controller.status.history.ComponentStatusRepository;
import org.apache.nifi.controller.status.history.MetricDescriptor;
import org.apache.nifi.controller.status.history.StatusHistory;
import org.apache.nifi.controller.status.history.VolatileComponentStatusRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 3/4/16.
 */
public class ThinkbigVolatileComponentStatusRepository implements ComponentStatusRepository {

    private VolatileComponentStatusRepository repository;

    public ThinkbigVolatileComponentStatusRepository(){
        System.out.println("NEW ThinkbigVolatileComponentStatusRepository ");
        repository = new VolatileComponentStatusRepository();
    }

    @Override
    public void capture(ProcessGroupStatus rootGroupStatus) {
        System.out.println("CATURE for "+rootGroupStatus.getId()+", "+rootGroupStatus.getName());
        repository.capture(rootGroupStatus);
    }

    @Override
    public void capture(ProcessGroupStatus rootGroupStatus, Date timestamp) {
        System.out.println("CATURE with timestamp "+rootGroupStatus.getId()+", "+rootGroupStatus.getName()+" at "+timestamp);
        repository.capture(rootGroupStatus, timestamp);
    }

    @Override
    public Date getLastCaptureDate() {
        System.out.println("getLastCaptureDate");
        return repository.getLastCaptureDate();
    }

    @Override
    public StatusHistory getProcessorStatusHistory(String processorId, Date start, Date end, int preferredDataPoints) {
        System.out.println("getProcessorStatusHistory "+processorId);
        return repository.getProcessorStatusHistory(processorId, start, end, preferredDataPoints);
    }

    @Override
    public StatusHistory getConnectionStatusHistory(String connectionId, Date start, Date end, int preferredDataPoints) {
        return repository.getConnectionStatusHistory(connectionId, start, end, preferredDataPoints);
    }

    @Override
    public StatusHistory getProcessGroupStatusHistory(String processGroupId, Date start, Date end, int preferredDataPoints) {
        return repository.getProcessGroupStatusHistory(processGroupId, start, end, preferredDataPoints);
    }

    @Override
    public StatusHistory getRemoteProcessGroupStatusHistory(String remoteGroupId, Date start, Date end, int preferredDataPoints) {
        return repository.getRemoteProcessGroupStatusHistory(remoteGroupId, start, end, preferredDataPoints);
    }

    @Override
    public List<MetricDescriptor<ConnectionStatus>> getConnectionMetricDescriptors() {
        System.out.println("getConnectionMetricDescriptors");
        return repository.getConnectionMetricDescriptors();
    }

    @Override
    public List<MetricDescriptor<ProcessGroupStatus>> getProcessGroupMetricDescriptors() {
        System.out.println("getProcessGroupMetricDescriptors");
        return repository.getProcessGroupMetricDescriptors();
    }

    @Override
    public List<MetricDescriptor<RemoteProcessGroupStatus>> getRemoteProcessGroupMetricDescriptors() {
        System.out.println("getRemoteProcessGroupMetricDescriptors");
        return repository.getRemoteProcessGroupMetricDescriptors();
    }

    @Override
    public List<MetricDescriptor<ProcessorStatus>> getProcessorMetricDescriptors() {
        System.out.println("getProcessorMetricDescriptors");
        return repository.getProcessorMetricDescriptors();
    }
}
