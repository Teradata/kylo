/**
 * @author sv186029
 */
package com.thinkbiganalytics.nifi.authorization.ranger.service;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClient;

@Tags({"ranger,athorization,thinkbig"})
@CapabilityDescription("Ranger authorization service for PCNG.")
public interface RangerService extends ControllerService {
	
    RangerRestClient getConnection() throws Exception; 
}
