package com.thinkbiganalytics.datalake.rangeradmin;


/**
 * HadoopAuthorizationFactory class for returning instance of service class.
 * @author sv186029
 *
 */
public class HadoopAuthorizationFactory {

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
