/**
 * @author sv186029
 */
package com.thinkbiganalytics.nifi.authorization.ranger.service;

import com.thinkbiganalytics.datalake.authorization.rest.client.RangerRestClient;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;


@Tags({"ranger,athorization,thinkbig"})
@CapabilityDescription("Ranger authorization service for PCNG.")
public interface RangerService extends ControllerService {
	
    RangerRestClient getConnection() throws Exception; 
}
