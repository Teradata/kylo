package com.thinkbiganalytics.spark.shell.cluster;

/*-
 * #%L
 * Spark Shell Core
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

import com.google.common.collect.ImmutableList;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.spark.shell.SparkLauncherSparkShellProcess;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class SparkShellClusterListenerTest {

    private static final String CLIENT_ID = "MY_CLIENT_ID";

    /**
     * Verify message to cluster on connection.
     */
    @Test
    public void onConnected() {
        // Test process connected
        final ClusterService clusterService = Mockito.mock(ClusterService.class);
        final SparkShellClusterListener clusterListener = new SparkShellClusterListener(clusterService, Mockito.mock(SparkShellClusterDelegate.class));
        clusterListener.onConnected(ImmutableList.of("node1", "node2"));

        // Verify message
        final ArgumentCaptor<SparkShellProcessSyncMessage> messageCaptor = ArgumentCaptor.forClass(SparkShellProcessSyncMessage.class);
        Mockito.verify(clusterService).sendMessageToOthers(Mockito.eq(SparkShellProcessSyncMessage.TYPE), messageCaptor.capture());
        Assert.assertNull(messageCaptor.getValue().getProcesses());
    }

    @Test
    public void onMessageReceived() throws Exception {
    }

    /**
     * Verify message to cluster on process ready.
     */
    @Test
    public void processReady() {
        // Mock cluster service
        final ClusterService clusterService = Mockito.mock(ClusterService.class);
        Mockito.when(clusterService.isClustered()).thenReturn(true);

        // Test process ready
        final SparkShellClusterListener clusterListener = new SparkShellClusterListener(clusterService, Mockito.mock(SparkShellClusterDelegate.class));

        final SparkLauncherSparkShellProcess process = Mockito.mock(SparkLauncherSparkShellProcess.class);
        Mockito.when(process.getClientId()).thenReturn(CLIENT_ID);
        clusterListener.processReady(process);

        // Verify message
        final ArgumentCaptor<SparkShellProcessChangedMessage> messageCaptor = ArgumentCaptor.forClass(SparkShellProcessChangedMessage.class);
        Mockito.verify(clusterService).sendMessageToOthers(Mockito.eq(SparkShellProcessChangedMessage.TYPE), messageCaptor.capture());
        Assert.assertEquals(CLIENT_ID, messageCaptor.getValue().getClientId());
        Assert.assertEquals(process, messageCaptor.getValue().getProcess());
    }

    /**
     * Verify message to cluster on process started.
     */
    @Test
    public void processStarted() {
        // Mock cluster service
        final ClusterService clusterService = Mockito.mock(ClusterService.class);
        Mockito.when(clusterService.isClustered()).thenReturn(true);

        // Test process started
        final SparkShellClusterListener clusterListener = new SparkShellClusterListener(clusterService, Mockito.mock(SparkShellClusterDelegate.class));

        final SparkLauncherSparkShellProcess process = Mockito.mock(SparkLauncherSparkShellProcess.class);
        Mockito.when(process.getClientId()).thenReturn(CLIENT_ID);
        clusterListener.processStarted(process);

        // Verify message
        final ArgumentCaptor<SparkShellProcessChangedMessage> messageCaptor = ArgumentCaptor.forClass(SparkShellProcessChangedMessage.class);
        Mockito.verify(clusterService).sendMessageToOthers(Mockito.eq(SparkShellProcessChangedMessage.TYPE), messageCaptor.capture());
        Assert.assertEquals(CLIENT_ID, messageCaptor.getValue().getClientId());
        Assert.assertEquals(process, messageCaptor.getValue().getProcess());
    }

    /**
     * Verify message to cluster on process stopped.
     */
    @Test
    public void processStopped() {
        // Mock cluster service
        final ClusterService clusterService = Mockito.mock(ClusterService.class);
        Mockito.when(clusterService.isClustered()).thenReturn(true);

        // Test process stopped
        final SparkShellClusterListener clusterListener = new SparkShellClusterListener(clusterService, Mockito.mock(SparkShellClusterDelegate.class));

        final SparkLauncherSparkShellProcess process = Mockito.mock(SparkLauncherSparkShellProcess.class);
        Mockito.when(process.getClientId()).thenReturn(CLIENT_ID);
        clusterListener.processStopped(process);

        // Verify message
        final ArgumentCaptor<SparkShellProcessChangedMessage> messageCaptor = ArgumentCaptor.forClass(SparkShellProcessChangedMessage.class);
        Mockito.verify(clusterService).sendMessageToOthers(Mockito.eq(SparkShellProcessChangedMessage.TYPE), messageCaptor.capture());
        Assert.assertEquals(CLIENT_ID, messageCaptor.getValue().getClientId());
        Assert.assertNull(messageCaptor.getValue().getProcess());
    }
}
