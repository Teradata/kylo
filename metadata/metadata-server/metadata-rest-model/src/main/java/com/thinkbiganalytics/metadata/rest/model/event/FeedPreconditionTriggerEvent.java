/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPreconditionTriggerEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String feedId;
    
    public FeedPreconditionTriggerEvent() {
    }
    
    public FeedPreconditionTriggerEvent(String id) {
        this.feedId = id;
    }

}
