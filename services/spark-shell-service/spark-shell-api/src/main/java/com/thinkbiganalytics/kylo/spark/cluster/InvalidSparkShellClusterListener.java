package com.thinkbiganalytics.kylo.spark.cluster;

/*-
 * #%L
 * Spark Shell Service API
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import java.util.List;

import javax.annotation.Nonnull;

public class InvalidSparkShellClusterListener implements SparkShellClusterListener {

    private static final String NOT_IMPLEMENTED = "method called on invalid SparkShellClusterListener";

    @Override
    public void onClusterMembershipChanged(List<String> previousMembers, List<String> currentMembers) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void onConnected(List<String> currentMembers) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void onDisconnected(List<String> currentMembers) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void onClosed(List<String> currentMembers) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void onMessageReceived(String from, ClusterMessage message) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void processReady(@Nonnull SparkShellProcess process) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void processStarted(@Nonnull SparkShellProcess process) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void processStopped(@Nonnull SparkShellProcess process) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
