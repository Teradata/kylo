package com.thinkbiganalytics.nifi.repository;

import org.apache.nifi.controller.queue.FlowFileQueue;
import org.apache.nifi.controller.repository.*;
import org.apache.nifi.controller.repository.claim.ResourceClaimManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wali.SyncListener;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 3/4/16.
 */
public class ThinkbigWriteAheadFlowFileRepository implements FlowFileRepository, SyncListener {
    private static final Logger logger = LoggerFactory.getLogger(ThinkbigWriteAheadFlowFileRepository.class);

    private WriteAheadFlowFileRepository writeAheadFlowFileRepository;

    public ThinkbigWriteAheadFlowFileRepository(){
        logger.info("NEW THINKBIG FLOW FILE REPO!!!!!");

        writeAheadFlowFileRepository = new WriteAheadFlowFileRepository();
    }

    @Override
    public void initialize(ResourceClaimManager claimManager) throws IOException {
        logger.info("initialize Resource Claim Manager ");
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
        logger.info("UPDATE REPOSITORY !!!!!!!!!!");
        if(records != null){
            logger.info("UPDATE REPOSITORY !!!!!!!!!! Updating   " + records.size() + " records");
        }
        writeAheadFlowFileRepository.updateRepository(records);
    }

    @Override
    public void onSync(int partitionIndex) {
        logger.info("onSync  "+partitionIndex);
        writeAheadFlowFileRepository.onSync(partitionIndex);
    }

    @Override
    public void onGlobalSync() {
        logger.info("onGlobalSync");
        writeAheadFlowFileRepository.onGlobalSync();
    }

    @Override
    public void swapFlowFilesOut(List<FlowFileRecord> swappedOut, FlowFileQueue queue, String swapLocation) throws IOException {
        logger.info("swapFlowFilesOut ");
        writeAheadFlowFileRepository.swapFlowFilesOut(swappedOut, queue, swapLocation);
    }

    @Override
    public void swapFlowFilesIn(String swapLocation, List<FlowFileRecord> swapRecords, FlowFileQueue queue) throws IOException {
        logger.info("swapFlowFilesIn ");
        writeAheadFlowFileRepository.swapFlowFilesIn(swapLocation, swapRecords, queue);
    }

    @Override
    public long loadFlowFiles(QueueProvider queueProvider, long minimumSequenceNumber) throws IOException {
        logger.info("Loading Flow Files " + queueProvider);
        if(queueProvider.getAllQueues() != null){
            logger.info("Loaing Flow Files Queue size of "+queueProvider.getAllQueues());
        }
        return writeAheadFlowFileRepository.loadFlowFiles(queueProvider, minimumSequenceNumber);
    }

    @Override
    public long getNextFlowFileSequence() {

        long nextFlowFileSeq = writeAheadFlowFileRepository.getNextFlowFileSequence();
        logger.info("Get Next Flow File Sequence as: "+nextFlowFileSeq);
        return nextFlowFileSeq;
    }

    @Override
    public long getMaxFlowFileIdentifier() throws IOException {
        return writeAheadFlowFileRepository.getMaxFlowFileIdentifier();
    }

    public int checkpoint() throws IOException {
        return writeAheadFlowFileRepository.checkpoint();
    }
}
