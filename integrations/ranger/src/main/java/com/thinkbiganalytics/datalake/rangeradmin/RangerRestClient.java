
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
 * @author sv186029
 *
 */
public class RangerRestClient extends JerseyRestClient {

	private String apiPath= "/service"; 

	private RangerRestClientConfig clientConfig;


	public RangerRestClient(RangerRestClientConfig config) {
		super(config);
		this.clientConfig = config;
	}

	protected WebTarget getBaseTarget() {
		WebTarget target = super.getBaseTarget();
		return target.path(apiPath);
	}
/***
 * Functions for Managing policies in Ranger
 * @return
 */
	public String getPolicy(int policyId) 
	{
		return get("/public/api/policy/"+policyId, null, String.class);
	}

	public void createPolicy(JSONObject policy) 
	{   
		post("/public/api/policy/",policy,String.class);
	}

	public void updatePolicy(JSONObject obj,int policyId)
	{
		put("/public/api/policy/"+policyId,obj,String.class);
	}

	public String deletePolicy(int policyId) 
	{
		return delete("/public/api/policy/"+policyId,null,String.class);
	}

	public String searchPolicies(Map<String,Object> searchCriteria) 
	{
		return get("/public/api/policy/",searchCriteria,String.class);
	}

	public String countPolicies()
	{
		return get("/public/api/policy/count",null,String.class);
	}

	/***
	 * Functions for getting user/groups information in Ranger
	 */
	
	public String getAllUsers()
	{
		return get("/xusers/users",null,String.class);
	}
	
	public String getUserByName(String userName)
	{
		return get("/xusers/users/userName/"+userName,null,String.class);
		
	}
	
	public String getUserById(int userId)
	{
		return get("/xusers/secure/users/"+userId,null,String.class);
	}
	
	public String getUserCount()
	{
		return get("/xusers/users/count",null,String.class);
	}
	
	public String getAllGroups()
	{
		return get("/xusers/groups",null,String.class);
	}
	
	public String getGroupByName(String groupName)
	{
		return get("/xusers/groups/groupName/"+groupName,null,String.class);
		
	}
	
	public String getGroupById(int groupId)
	{
		return get("/xusers/secure/groups/"+groupId,null,String.class);
	}
	
	public String getGroupCount()
	{
		return get("/xusers/groups/count",null,String.class);
	}
	
	//User-group mapping functions

	public String getUserGroupMappingById(int id)
	{
		return get("/xusers/groupusers/"+id,null,String.class);
	}
	public String getUserGroupMapping()
	{
		return get("/xusers/groupusers",null,String.class);
	}
		
	public String getUserGroupMappingCount()
	{
		return get("/xusers/groupusers/count",null,String.class);
	}
	
	public String getGroupInfoByUserId(int userId)
	{
		return get("/xusers/"+userId+"/groups",null,String.class);
	}
	
	public String getUserInfoByGroupId(int groupId)
	{
		return get("/xusers/"+groupId+"/users",null,String.class);
	}
}
