package com.thinkbiganalytics.nifi.rest.model.visitor;

/**
 * Created by sr186054 on 2/14/16.
 */
public interface NifiFlowVisitor {
    void visitProcessor(NifiVisitableProcessor processor);
    void visitConnection(NifiVisitableConnection connection);
    void visitProcessGroup(NifiVisitableProcessGroup processGroup);


}
