package com.thinkbiganalytics.datalake.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization_Implementation.RangerAuthorizationService;
import com.thinkbiganalytics.datalake.authorization_Implementation.SentryAuthorizationService;

public class HadoopAuthorizationFactory {

    private static final Logger log = LoggerFactory.getLogger(HadoopAuthorizationFactory.class); 
    
	static String RANGER  = "ranger";
	static String SENTRY = "sentry";
	
	public HadoopAuthorizationService authorizationType(String autherizationServiceType)
	{

		if(autherizationServiceType == null || autherizationServiceType == ""){
			
			return null;
		}		
		if(autherizationServiceType.equalsIgnoreCase(RANGER)){
			return new RangerAuthorizationService();

		} else if(autherizationServiceType.equalsIgnoreCase(SENTRY)){
			return new SentryAuthorizationService();

		} 

		return null;

	}
}
