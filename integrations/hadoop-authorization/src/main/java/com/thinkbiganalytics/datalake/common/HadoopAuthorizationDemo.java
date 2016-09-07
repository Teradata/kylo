package com.thinkbiganalytics.datalake.common;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.helper.RangerConnectionHelper;

/**
 * HadoopAuthorizationDemo class used to invoke Ranger/Sentry authorization Service. 
 * @author sv186013
 *
 */

public class HadoopAuthorizationDemo 
{
	private static final Logger log = LoggerFactory.getLogger(HadoopAuthorizationDemo.class); 

	static HadoopAuthorizationFactory hadoopAuthorizationFactory;
	static HadoopAuthorizationService  hadoopAuthorizationServiceObj;


	public  static void  main(String args []) throws IOException, Exception
	{

		RangerConnectionHelper rn = new RangerConnectionHelper();
		rn.setHostname("localhost");
		rn.setPort(6080);
		rn.setUsername("admin");
		rn.setPassword("admin");

		// Initialize Ranger Authorization Service
		hadoopAuthorizationFactory = new HadoopAuthorizationFactory();
		hadoopAuthorizationServiceObj =  hadoopAuthorizationFactory.authorizationType("ranger");

		if ( hadoopAuthorizationServiceObj == null )
		{
			System.out.println("No proper authorization service provided");
		}
		else
		{
			hadoopAuthorizationServiceObj.initiateAuthorizationService(rn);
		}

/*		// Initialize Sentry Authorization Service
		hadoopAuthorizationFactory = new HadoopAuthorizationFactory();
		hadoopAuthorizationServiceObj =  hadoopAuthorizationFactory.authorizationType("sentry");

		if ( hadoopAuthorizationServiceObj == null )
		{
			System.out.println("No proper authorization service provided");
		}
		else
		{
			hadoopAuthorizationServiceObj.initiateAuthorizationService();
		}
*/
	}
}


