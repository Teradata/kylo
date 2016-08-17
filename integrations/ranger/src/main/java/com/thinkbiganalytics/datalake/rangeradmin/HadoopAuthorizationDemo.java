package com.thinkbiganalytics.datalake.rangeradmin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.thinkbiganalytics.datalake.rangeradmin.domain.HDFSPolicy;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.filter.LoggingFilter;
import org.joda.time.chrono.AssembledChronology.Fields;
import org.json.simple.JSONObject;

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


