package com.thinkbiganalytics.nifi.provenance.v2;


import com.thinkbiganalytics.nifi.provenance.v2.writer.ThinkbigProvenanceEventWriter;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.serialization.RecordWriter;
import org.apache.nifi.provenance.toc.TocWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Used in conjunction with the ThinkbigProvenanceEventRepository to assign the correct Ids to the incoming ProvenanceEventRecord objects
 *
 * Created by sr186054 on 8/26/16.
 */
public class ThinkbigRecordWriterDelegate implements RecordWriter {

    private static final Logger log = LoggerFactory.getLogger(ThinkbigRecordWriterDelegate.class);
    private RecordWriter recordWriter;

    public ThinkbigRecordWriterDelegate(RecordWriter recordWriter) {
        this.recordWriter = recordWriter;
    }

    @Override
    public void writeHeader(long l) throws IOException {
        recordWriter.writeHeader(l);
    }

    @Override
    public long writeRecord(ProvenanceEventRecord provenanceEventRecord, long l) throws IOException {
        ThinkbigProvenanceEventWriter thinkbigProvenanceEventWriter = (ThinkbigProvenanceEventWriter) SpringApplicationContext.getInstance().getBean("thinkbigProvenanceEventWriter");
        if (thinkbigProvenanceEventWriter != null) {
            thinkbigProvenanceEventWriter.writeEvent(provenanceEventRecord, l);
        }
        return recordWriter.writeRecord(provenanceEventRecord, l);

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
}
