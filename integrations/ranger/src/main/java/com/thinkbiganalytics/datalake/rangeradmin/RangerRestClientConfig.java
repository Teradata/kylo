
package com.thinkbiganalytics.datalake.rangeradmin;


import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

/** 
 * 
 * @category ranger security
 * @version 1.0
 *
 */

public class RangerRestClientConfig extends  JerseyClientConfig{

// apiPath not require here value is set in RangerRestClient 
	
	private String apiPath;
	//="/service/public/api" ;


	public RangerRestClientConfig(String apiPath) {
		this.apiPath = apiPath;
	}
	public RangerRestClientConfig() {

	}

	public RangerRestClientConfig(String host, String username, String password) {
		super(host, username, password);
		//this.apiPath = apiPath;

	}

	public RangerRestClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, String apiPath) {
		super(host, username, password, https, keystoreOnClasspath, keystorePath, keystorePassword);
		this.apiPath = apiPath;
	}

	public String getApiPath() {
		return apiPath;
	}

	public void setApiPath(String apiPath) {
		this.apiPath = apiPath;
	}

}
