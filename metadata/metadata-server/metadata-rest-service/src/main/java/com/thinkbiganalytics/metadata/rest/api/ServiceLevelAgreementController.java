/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

import org.springframework.http.MediaType;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAgreementController {
    
    @Inject
    private ServiceLevelAgreementProvider provider;
    
    @Inject
    private MetadataAccess metadata;

    @POST
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    public ServiceLevelAgreement createAgreement(ServiceLevelAgreement agreement) {
        
        return this.metadata.commit(new Command<ServiceLevelAgreement>() {
            @Override
            public ServiceLevelAgreement execute() {
                
                
                return null;
            }
        });
    }

}
