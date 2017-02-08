package com.thinkbiganalytics.nifi.rest.model.visitor;

/*-
 * #%L
 * thinkbig-nifi-flow-visitor-model
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

/**
 * Visitor that will walk a flow starting with a process group walking the connections, input/output ports, funnels etc, returning a {@link NifiVisitableProcessGroup} containing the graph of
 * processors connected together This visitor will eliminate the connections between processors and directly connect processors to other processors. The connection objects are still maintained in a
 * separate map on the returned {@link NifiVisitableProcessGroup}
 */
public interface NifiFlowVisitor {

    /**
     * Visit a processor
     *
     * @param processor the processor to visit
     */
    void visitProcessor(NifiVisitableProcessor processor);

    /**
     * visit a connection
     *
     * @param connection the connection to visit
     */
    void visitConnection(NifiVisitableConnection connection);

    /**
     * visit a process group
     *
     * @param processGroup the process group to visit
     */
    void visitProcessGroup(NifiVisitableProcessGroup processGroup);

    /**
     * Return a visited processor by its processor id
     *
     * @param id the processor id
     * @return the visited processor
     */
    NifiVisitableProcessor getProcessor(String id);

    /**
     * Return the visited process group by its process group id
     *
     * @param id the process group id
     * @return the visited process group
     */
    NifiVisitableProcessGroup getProcessGroup(String id);


}
