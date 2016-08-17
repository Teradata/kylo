
package com.thinkbiganalytics.datalake.rangeradmin;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

/** 
 * 
 *
 * @category ranger security
 * @version 1.0
 *
 */

/**
 * This class has all the functions for implementing REST calls like createPolicy, getPolicy, updatePolicy etc.
 * @author cv186013
 *
 */
public class RangerRestClient extends JerseyRestClient {

	private String apiPath= "/service/public/api/policy"; 

	private RangerRestClientConfig clientConfig;


	public RangerRestClient(RangerRestClientConfig config) {
		super(config);
		this.clientConfig = config;
	}

	protected WebTarget getBaseTarget() {
		WebTarget target = super.getBaseTarget();
		return target.path(apiPath);
	}

	public String getPolicy(int policyId) 
	{
		return get("/"+policyId, null, String.class);
	}

	public void createPolicy(JSONObject policy) 
	{   
		post("/",policy,String.class);
	}

	public void updatePolicy(JSONObject obj,int policyId)
	{
		put("/"+policyId,obj,String.class);
	}

	public String deletePolicy(int policyId) 
	{
		return delete("/"+policyId,null,String.class);
	}

	public String searchPolicies(Map<String,Object> searchCriteria) 
	{
		return get("/",searchCriteria,String.class);
	}

	public String countPolicies()
	{
		return get("/count",null,String.class);
	}

}
