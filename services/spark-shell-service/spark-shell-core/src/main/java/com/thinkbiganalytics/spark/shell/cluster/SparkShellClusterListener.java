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

import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceListener;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Coordinates Spark Shell processes between Kylo nodes in a cluster.
 */
public class SparkShellClusterListener implements ClusterServiceListener, ClusterServiceMessageReceiver, SparkShellProcessListener {

    private static final Logger log = LoggerFactory.getLogger(SparkShellClusterListener.class);

    /**
     * Kylo cluster service
     */
    @Nonnull
    private final ClusterService clusterService;

    /**
     * Helper for managing processes
     */
    @Nonnull
    private final SparkShellClusterDelegate delegate;

    /**
     * Constructs a {@code SparkShellClusterListener}.
     *
     * @param clusterService the Kylo cluster service
     * @param delegate       the process manager
     */
    public SparkShellClusterListener(@Nonnull final ClusterService clusterService, @Nonnull final SparkShellClusterDelegate delegate) {
        this.clusterService = clusterService;
        this.delegate = delegate;

        // Subscribe to cluster events
        clusterService.subscribe((ClusterServiceListener) this);

        clusterService.subscribe( this,new String[] {SparkShellProcessChangedMessage.TYPE,SparkShellProcessSyncMessage.TYPE});
    }

    @Override
    public void onClosed(final List<String> currentMembers) {
        // ignored
    }

    @Override
    public void onClusterMembershipChanged(final List<String> previousMembers, final List<String> currentMembers) {
        // ignored
    }

    /**
     * Request Spark Shell processes from other nodes when connected to a cluster.
     */
    @Override
    public void onConnected(@Nonnull final List<String> currentMembers) {
        if (currentMembers.size() > 1) {
            clusterService.sendMessageToOthers(SparkShellProcessSyncMessage.TYPE, new SparkShellProcessSyncMessage());
        }
    }

    @Override
    public void onDisconnected(final List<String> currentMembers) {
        // ignored
    }

    /**
     * Handle messages received from the cluster.
     */
    @Override
    public void onMessageReceived(@Nonnull final String from, @Nonnull final ClusterMessage message) {
        log.trace("entry params({}, {})", from, message);

        if (SparkShellProcessChangedMessage.TYPE.equals(message.getType())) {
            final SparkShellProcessChangedMessage changedMessage = (SparkShellProcessChangedMessage) message.getMessage();
            if (changedMessage.getProcess() != null) {
                delegate.updateProcess(changedMessage.getProcess());
            } else {
                delegate.removeProcess(changedMessage.getClientId());
            }
        } else if (SparkShellProcessSyncMessage.TYPE.equals(message.getType())) {
            final SparkShellProcessSyncMessage syncMessage = (SparkShellProcessSyncMessage) message.getMessage();
            if (syncMessage.getProcesses() != null) {
                syncMessage.getProcesses().forEach(delegate::updateProcess);
            } else if (clusterService.isClustered()) {
                final List<SparkShellProcess> processes = delegate.getProcesses().stream().filter(SparkShellProcess::isLocal).collect(Collectors.toList());
                syncMessage.setProcesses(processes);
                clusterService.sendMessageToOther(from, SparkShellProcessSyncMessage.TYPE, syncMessage);
            }
        }

        log.trace("exit");
    }

    @Override
    public void processReady(@Nonnull final SparkShellProcess process) {
        fireProcessChanged(process.getClientId(), process);
    }

    @Override
    public void processStarted(@Nonnull final SparkShellProcess process) {
        fireProcessChanged(process.getClientId(), process);
    }

    @Override
    public void processStopped(@Nonnull final SparkShellProcess process) {
        fireProcessChanged(process.getClientId(), null);
    }

    /**
     * Notify the cluster of changes to local processes.
     *
     * @param clientId the client identifier
     * @param process  the Spark Shell process
     */
    private void fireProcessChanged(@Nonnull final String clientId, @Nullable final SparkShellProcess process) {
        if (clusterService.isClustered()) {
            final SparkShellProcessChangedMessage message = new SparkShellProcessChangedMessage();
            message.setClientId(clientId);
            message.setProcess(process);
            clusterService.sendMessageToOthers(SparkShellProcessChangedMessage.TYPE, message);
        }
    }
}
