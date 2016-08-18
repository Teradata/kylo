package com.thinkbiganalytics.nifi.rest.model.visitor;

/**
 * Created by sr186054 on 2/14/16.
 */

public interface NifiVisitable {
    void accept(NifiFlowVisitor nifiVisitor);
}