package com.thinkbiganalytics.nifi.rest.model.visitor;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/14/16.
 */
public interface NifiFlowVisitor {

    void visitProcessor(NifiVisitableProcessor processor);

    void visitConnection(NifiVisitableConnection connection);

    void visitProcessGroup(NifiVisitableProcessGroup processGroup);

    NifiVisitableProcessor getProcessor(String id);

    NifiVisitableProcessGroup getProcessGroup(String id);

    Map<String, Set<String>> getFailureConnectionIdToSourceProcessorIds();


}
