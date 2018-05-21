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
import org.apache.nifi.processor.FlowFileFilter;

import java.util.List;
import java.util.Set;


/**
 * FlowFile Filter that will populate the list of Accepted or Rejected flow files based upon some condition
 */
public abstract interface SavepointFlowFileFilter extends FlowFileFilter {


    public boolean isAccepted(FlowFile flowFile);

    public boolean isRejected(FlowFile flowFile);

    public Integer getAcceptedCount();

    public Integer getRejectedCount();

    public Set<FlowFile> getRejectedFlowFiles();

    public Set<FlowFile> getAcceptedFlowFiles();

    /**
     * List of ids which the filter should match
     *
     * @return the list of passed in ids to match
     */
    public List<String> getFlowFileIdsToMatch();

}
