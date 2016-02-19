/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

import com.thinkbiganalytics.controller.precond.FeedPrecondition.ID;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
@Tags({"thinkbig", "feed", "precondition", "trigger"})
@CapabilityDescription("")
public interface FeedPreconditionService extends ControllerService {
    
    ID resolve(Serializable ser);
    
    FeedPrecondition createPrecondition(String name, Metric first, Metric... rest);
    
    FeedPrecondition createPrecondition(String name, Collection<Metric> metrics);
    
    public FeedPrecondition getPrecondition(ID id);

    public FeedPrecondition getPrecondition(String name);
    
    Set<ChangeSet<? extends Dataset, ? extends ChangedContent>> checkPreconditions(ID id);
    
    void addListener(ID id, PreconditionListener listener);
    
    void addListener(String name, PreconditionListener listener);
    
}
