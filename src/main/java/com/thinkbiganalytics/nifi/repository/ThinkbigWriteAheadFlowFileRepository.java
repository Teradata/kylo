package com.thinkbiganalytics.nifi.repository;

import org.apache.nifi.controller.queue.FlowFileQueue;
import org.apache.nifi.controller.repository.*;
import org.apache.nifi.controller.repository.claim.ResourceClaimManager;
import org.wali.SyncListener;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 3/4/16.
 */
public class ThinkbigWriteAheadFlowFileRepository implements FlowFileRepository, SyncListener {

    private WriteAheadFlowFileRepository writeAheadFlowFileRepository;

    public ThinkbigWriteAheadFlowFileRepository(){
        writeAheadFlowFileRepository = new WriteAheadFlowFileRepository();
    }

    @Override
    public void initialize(ResourceClaimManager claimManager) throws IOException {
        writeAheadFlowFileRepository.initialize(claimManager);
    }

    @Override
    public void close() throws IOException {
        writeAheadFlowFileRepository.close();
    }

    @Override
    public boolean isVolatile() {
        return writeAheadFlowFileRepository.isVolatile();
    }

    @Override
    public long getStorageCapacity() throws IOException {
        return writeAheadFlowFileRepository.getStorageCapacity();
    }

    @Override
    public long getUsableStorageSpace() throws IOException {
        return writeAheadFlowFileRepository.getUsableStorageSpace();
    }

    @Override
    public void updateRepository(Collection<RepositoryRecord> records) throws IOException {
        writeAheadFlowFileRepository.updateRepository(records);
    }

    @Override
    public void onSync(int partitionIndex) {
        writeAheadFlowFileRepository.onSync(partitionIndex);
    }

    @Override
    public void onGlobalSync() {
        writeAheadFlowFileRepository.onGlobalSync();
    }

    @Override
    public void swapFlowFilesOut(List<FlowFileRecord> swappedOut, FlowFileQueue queue, String swapLocation) throws IOException {
        writeAheadFlowFileRepository.swapFlowFilesOut(swappedOut, queue, swapLocation);
    }

    @Override
    public void swapFlowFilesIn(String swapLocation, List<FlowFileRecord> swapRecords, FlowFileQueue queue) throws IOException {
        writeAheadFlowFileRepository.swapFlowFilesIn(swapLocation, swapRecords, queue);
    }

    @Override
    public long loadFlowFiles(QueueProvider queueProvider, long minimumSequenceNumber) throws IOException {
        return writeAheadFlowFileRepository.loadFlowFiles(queueProvider, minimumSequenceNumber);
    }

    @Override
    public long getNextFlowFileSequence() {
        return writeAheadFlowFileRepository.getNextFlowFileSequence();
    }

    @Override
    public long getMaxFlowFileIdentifier() throws IOException {
        return writeAheadFlowFileRepository.getMaxFlowFileIdentifier();
    }

    public int checkpoint() throws IOException {
        return writeAheadFlowFileRepository.checkpoint();
    }
}
