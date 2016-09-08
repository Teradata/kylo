package com.thinkbiganalytics.datalake.common;

import com.thinkbiganalytics.datalake.helper.RangerConnectionHelper;
import com.thinkbiganalytics.datalake.helper.SentryConnectionHelper;

/***
 * 
 * @author sv186029
 *
 */

public interface HadoopAuthorizationService
{
	void initiateAuthorizationService() throws Exception;
	
	void initiateAuthorizationService(RangerConnectionHelper ranerConnHelper) throws Exception;
	
	void initiateAuthorizationService(SentryConnectionHelper sentryConnHelper) throws Exception;
}
