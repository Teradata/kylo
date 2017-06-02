package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.serialization.RecordWriter;
import org.apache.nifi.provenance.toc.TocWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class KyloRecordWriterDelegate implements RecordWriter {

    private static final Logger log = LoggerFactory.getLogger(KyloRecordWriterDelegate.class);

    private Long conversionTime = 0L;
    private Long eventCount = 0L;

    private RecordWriter recordWriter;
    KyloProvenanceProcessingQueue processingQueue;

    public KyloRecordWriterDelegate(RecordWriter recordWriter,KyloProvenanceProcessingQueue processingQueue) {
        this.recordWriter = recordWriter;
        this.processingQueue =processingQueue;
    }

    @Override
    public void writeHeader(long l) throws IOException {
        recordWriter.writeHeader(l);
    }


    @Override
    public void flush() throws IOException {
        recordWriter.flush();
    }

    @Override
    public int getRecordsWritten() {
        return recordWriter.getRecordsWritten();
    }

    @Override
    public File getFile() {
        return recordWriter.getFile();
    }

    @Override
    public void lock() {
        recordWriter.lock();
    }

    @Override
    public void unlock() {
        recordWriter.unlock();
    }

    @Override
    public boolean tryLock() {
        return recordWriter.tryLock();
    }

    @Override
    public void markDirty() {
        recordWriter.markDirty();
    }

    @Override
    public void sync() throws IOException {
        recordWriter.sync();
    }

    @Override
    public TocWriter getTocWriter() {
        return recordWriter.getTocWriter();
    }

    @Override
    public boolean isClosed() {
        return recordWriter.isClosed();
    }

    public synchronized void close() throws IOException {
        this.recordWriter.close();
    }

    @Override
    public long writeRecord(ProvenanceEventRecord provenanceEventRecord, long eventId) throws IOException {
        long bytesWritten = recordWriter.writeRecord(provenanceEventRecord, eventId);
         processingQueue.put(eventId,provenanceEventRecord);
         return bytesWritten;
    }

    /**
     * generate timing stats for converting to the DTO
     * @param start start time
     */
    private void generateConversionStats(long start){

        long time = System.currentTimeMillis() - start;
        conversionTime+=time;
        eventCount +=1;
        if(eventCount % 1000 == 0){
            avgConversionTime();
        }
    }


    private Double avgConversionTime() {
      Double time = conversionTime.doubleValue()/eventCount;
      log.info("Avg time to convert {} events is {} ms",eventCount,time);
      return time;
    }


    /***
     * Gets the Spring managed bean to retrieve Kylo feed information
     * @return
     */
    private ProvenanceFeedLookup getProvenanceFeedLookup() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceFeedLookup.class);
    }

    private KyloNiFiFlowCacheUpdater getKyloNiFiFlowCacheUpdater() {
        return SpringApplicationContext.getInstance().getBean(KyloNiFiFlowCacheUpdater.class);
    }


    private ProvenanceEventObjectPool getProvenanceEventObjectPool() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventObjectPool.class);
    }

    /*
NiFi 1.2 method

 public StorageSummary writeRecord(ProvenanceEventRecord provenanceEventRecord) throws IOException {
        StorageSummary storageSummary = recordWriter.writeRecord(provenanceEventRecord);
        //record it to the queue
        processingQueue.put(provenanceEventRecord);
        return storageSummary;

    }

    @Override
    public long getBytesWritten() {
        return 0;
    }

    @Override
    public boolean isDirty() {
        return false;
    }
    */
}
