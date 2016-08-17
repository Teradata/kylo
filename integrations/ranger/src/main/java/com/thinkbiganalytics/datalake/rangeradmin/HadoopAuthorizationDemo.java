package com.thinkbiganalytics.datalake.rangeradmin;

import java.io.IOException;
/**
 * Hadoop Demo class for initializing Ranger Service.
 * @author sv186029
 *
 */
		
public class HadoopAuthorizationDemo {

	public  static void  main(String args []) throws IOException
	{

		//Initiate Hadoop Factory Object
		HadoopAuthorizationFactory hadoopAuthorizationFactory = new HadoopAuthorizationFactory();
		
		//Set Authorization type
		HadoopAuthorizationService  hadoopAuthorizationServiceObj =  hadoopAuthorizationFactory.authorizationType("ranger");

		if ( hadoopAuthorizationServiceObj == null )
		{
			System.out.println("No proper authorization service provided");
		}
		else
		{
			hadoopAuthorizationServiceObj.initiateAuthorizationService();
		}

	}
}


