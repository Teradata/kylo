package com.thinkbiganalytics.datalake.rangeradmin;

import java.io.IOException;

/***
 * 
 * @author sv186029
 *
 */


public class HadoopAuthorizationDemo {

	public  static void  main(String args []) throws IOException
	{


		HadoopAuthorizationFactory hadoopAuthorizationFactory = new HadoopAuthorizationFactory();
		HadoopAuthorizationService  hadoopAuthorizationServiceObj =  hadoopAuthorizationFactory.authorizationType("ranger");

		if ( hadoopAuthorizationServiceObj == null )
		{
			System.out.println("No proper authorization service provided");
		}

		else
			hadoopAuthorizationServiceObj.initiateAuthorizationService();

	}
}


