package com.thinkbiganalytics.nifi.provenance;
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

import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatistics;
import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatisticsData;
import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatisticsDataV2;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

public class TestLoadBackup {


    /**
     * Test the Provenance ability to load the backup of feed event metadata.
     * This is used when NiFi shuts down and restarts.
     * The Provenance Repo needs to load the snapshot of running events as it was when NiFi was shut down to continue flow operations in Kylo ops manager.
     * This will test loading the backup as well as backuping and loading previous kylo versions to the latest version
     */
    @Test
    public void testBackupProvenance() throws Exception {
        StandardProvenanceEventRecord.Builder builder = new StandardProvenanceEventRecord.Builder();
        String componentId = UUID.randomUUID().toString();
        String flowfileId = UUID.randomUUID().toString();
        ProvenanceEventRecord eventRecord = builder.setEventTime(System.currentTimeMillis())
            .setFlowFileEntryDate(DateTime.now().getMillis())
            .setComponentId(componentId)
            .setComponentType("PROCESSOR")
            .setCurrentContentClaim("container", "section", "identifier", 0L, 0L)
            .setFlowFileUUID(flowfileId)
            .setEventType(ProvenanceEventType.CREATE)
            .build();
        FeedEventStatistics.getInstance().setDeleteBackupAfterLoad(false);

        FeedEventStatistics.getInstance().checkAndAssignStartingFlowFile(eventRecord, eventRecord.getEventId());
        URL r = getClass().getClassLoader().getResource("feed-event-statistics.gz");
        String path = r.getPath();

        //Test backup and load of the V3 version of the metadata events
        String v3BackupLocation = StringUtils.substringBeforeLast(path, "/") + "/test-backup.v3.gz";
        FeedEventStatistics.getInstance().backup(v3BackupLocation);
        FeedEventStatistics.getInstance().clear();
        boolean success = FeedEventStatistics.getInstance().loadBackup(v3BackupLocation);
        Assert.assertTrue(success);
        FeedEventStatistics eventStatistics = FeedEventStatistics.getInstance();
        Assert.assertNotNull(eventStatistics.getFeedFlowFileId(flowfileId));

        //Test backup of the V2 version of the metadata events, loading into the V3  (simulate a user upgrading from 0.8.3 or 0.8.4 to 0.9.0)
        String v2BackupLocation = StringUtils.substringBeforeLast(path, "/") + "/test-backup.v2.gz";
        backupV2(v2BackupLocation);
        FeedEventStatistics.getInstance().clear();
        success = FeedEventStatistics.getInstance().loadBackup(v2BackupLocation);
        eventStatistics = FeedEventStatistics.getInstance();
        Assert.assertTrue(success);
        Assert.assertNotNull(eventStatistics.getFeedFlowFileId(flowfileId));

        //Test backup of the v1 version of the metadata events, loading into the V3  (simulate a user upgrading from 0.8.2 to 0.9.0)
        String v1BackupLocation = StringUtils.substringBeforeLast(path, "/") + "/test-backup.v1.gz";
        backupV1(v1BackupLocation);
        FeedEventStatistics.getInstance().clear();
        success = FeedEventStatistics.getInstance().loadBackup(v1BackupLocation);
        eventStatistics = FeedEventStatistics.getInstance();
        Assert.assertTrue(success);
        Assert.assertNotNull(eventStatistics.getFeedFlowFileId(flowfileId));

    }


    private void backupV2(String location) throws Exception {

        try (FileOutputStream fos = new FileOutputStream(location);
             GZIPOutputStream gz = new GZIPOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(gz)) {
            oos.writeObject(new FeedEventStatisticsDataV2(FeedEventStatistics.getInstance()));
            oos.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void backupV1(String location) throws Exception {

        try (FileOutputStream fos = new FileOutputStream(location);
             GZIPOutputStream gz = new GZIPOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(gz)) {
            oos.writeObject(new FeedEventStatisticsData(FeedEventStatistics.getInstance()));
            oos.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

}
