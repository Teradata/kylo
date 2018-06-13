package com.thinkbiganalytics.nifi.v2.savepoint;
/*-
 * #%L
 * kylo-nifi-core-processors
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

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.FlowFileFilter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultSavepointFlowFileFilter implements FlowFileFilter, SavepointFlowFileFilter {

    private List<String> flowFileIdsToMatch;

    private SavepointContextData contextData;

    private Set<FlowFile> rejectedFlowFiles = new HashSet<>();

    private Set<FlowFile> acceptedFlowFiles = new HashSet<>();

    int foundCount = 0;


    public DefaultSavepointFlowFileFilter(List<String> flowfileIdsToMatch, SavepointContextData contextData) {
        this.contextData = contextData;
        this.flowFileIdsToMatch = flowfileIdsToMatch == null ? Collections.emptyList() : flowfileIdsToMatch;
    }

    @Override
    public FlowFileFilterResult filter(FlowFile flowFile) {
        if (flowFileIdsToMatch.isEmpty()) {
            //check to see if its new or expired
            if (SavepointFlowFileFilterUtil.isExpired(flowFile, contextData.getExpirationDuration()) || SavepointFlowFileFilterUtil.isNew(flowFile, contextData.getProcessorId())) {
                acceptedFlowFiles.add(flowFile);
                return FlowFileFilterResult.ACCEPT_AND_CONTINUE;
            }
            rejectedFlowFiles.add(flowFile);
            return FlowFileFilterResult.REJECT_AND_CONTINUE;
        } else if (flowFileIdsToMatch.contains(flowFile.getAttribute(CoreAttributes.UUID.key()))) {
            acceptedFlowFiles.add(flowFile);
            foundCount++;
            return foundCount == flowFileIdsToMatch.size() ? FlowFileFilterResult.ACCEPT_AND_TERMINATE : FlowFileFilterResult.ACCEPT_AND_CONTINUE;
        } else {
            rejectedFlowFiles.add(flowFile);
            return FlowFileFilterResult.REJECT_AND_CONTINUE;
        }
    }

    public Set<FlowFile> getRejectedFlowFiles() {
        return rejectedFlowFiles;
    }

    public Set<FlowFile> getAcceptedFlowFiles() {
        return acceptedFlowFiles;
    }

    @Override
    public boolean isAccepted(FlowFile flowFile) {
        return acceptedFlowFiles.contains(flowFile);
    }

    @Override
    public boolean isRejected(FlowFile flowFile) {
        return rejectedFlowFiles.contains(flowFile);
    }

    @Override
    public Integer getAcceptedCount() {
        return acceptedFlowFiles.size();
    }

    @Override
    public Integer getRejectedCount() {
        return rejectedFlowFiles.size();
    }

    public List<String> getFlowFileIdsToMatch() {
        return flowFileIdsToMatch != null ? flowFileIdsToMatch : Collections.emptyList();
    }
}
