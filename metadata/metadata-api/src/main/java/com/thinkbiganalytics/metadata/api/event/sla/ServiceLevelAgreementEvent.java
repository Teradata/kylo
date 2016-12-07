/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.sla;

import java.security.Principal;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAgreementEvent extends AbstractMetadataEvent<ServiceLevelAgreementChange> {

    private static final long serialVersionUID = 1L;

    public ServiceLevelAgreementEvent(ServiceLevelAgreementChange data) {
        super(data);
    }

    public ServiceLevelAgreementEvent(ServiceLevelAgreementChange data, Principal user) {
        super(data, user);
    }

    public ServiceLevelAgreementEvent(ServiceLevelAgreementChange data, DateTime time, Principal user) {
        super(data, time, user);
    }

}
