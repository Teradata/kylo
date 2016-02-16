/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface FeedSource extends FeedData {

    interface ID extends Serializable { }

    ID getId();
    
    ServiceLevelAgreement.ID getAgreementId();
}
