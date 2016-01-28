/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface DataSource extends FeedData {

    ServiceLevelAgreement.ID getAgreementId();
}
