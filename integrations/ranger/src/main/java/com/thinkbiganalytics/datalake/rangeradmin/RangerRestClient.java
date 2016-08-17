
package com.thinkbiganalytics.datalake.rangeradmin;



import org.json.simple.JSONObject;
import com.thinkbiganalytics.rest.JerseyRestClient;
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

	private String apiPath= "/service/public/api"; 

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
		return get("/policy/"+policyId, null, String.class);
	}

	public void createPolicy(JSONObject policy) 
	{   
		post("/policy",policy,String.class);
	}

	public void updatePolicy(JSONObject obj,int policyId)
	{
		put("/policy/"+policyId,obj,String.class);
	}

	public String deletePolicy(int policyId) 
	{
		return delete("/policy/"+policyId,null,String.class);
	}

	public String searchPolicies(Map<String,Object> searchCriteria) 
	{
		return get("/policy",searchCriteria,String.class);
	}

	public Long countPolicies(String repo)
	{
		return null;
	}

}
