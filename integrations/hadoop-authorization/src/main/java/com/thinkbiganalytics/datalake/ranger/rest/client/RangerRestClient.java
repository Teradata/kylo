package com.thinkbiganalytics.datalake.ranger.rest.client;

import java.util.Map;
import javax.ws.rs.client.WebTarget;
import org.json.simple.JSONObject;
import com.thinkbiganalytics.rest.JerseyRestClient;

/**
 * This class has all the functions for implementing REST calls like createPolicy, getPolicy, updatePolicy etc.
 * @author sv186029
 *
 */

public class RangerRestClient extends JerseyRestClient{

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
	public String getPolicy(int policyId) throws RangerRestClientException
	{
		try {
			return get("/public/api/policy/"+policyId, null, String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get policy ." ,e);
		}
	}

	public void createPolicy(JSONObject policy) throws RangerRestClientException
	{   
		try {
			post("/public/api/policy/",policy,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to create policy." ,e);
		}
	}

	public void updatePolicy(JSONObject obj,int policyId) throws RangerRestClientException
	{
		try {
			put("/public/api/policy/"+policyId,obj,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to update policy." ,e);
		} 
	}

	public String deletePolicy(int policyId)  throws RangerRestClientException
	{
		try {
			return delete("/public/api/policy/"+policyId,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to delete policy ." ,e);
		}
	}

	public String searchPolicies(Map<String,Object> searchCriteria)  throws RangerRestClientException
	{
		try {
			return get("/public/api/policy/",searchCriteria,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to search policy." ,e);
		}
	}

	public String countPolicies() throws RangerRestClientException
	{
		try {
			return get("/public/api/policy/count",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to count policy." ,e);
		}
	}

	/***
	 * Functions for getting user/groups information in Ranger
	 */

	public String getAllUsers() throws RangerRestClientException
	{
		try {
			return get("/xusers/users",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get all user." ,e);
		}
	}

	public String getUserByName(String userName) throws RangerRestClientException
	{
		try {
			return get("/xusers/users/userName/"+userName,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get user by name .." ,e);
		}

	}

	public String getUserById(int userId) throws RangerRestClientException
	{
		try {
			return get("/xusers/secure/users/"+userId,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get user by ID." ,e);
		}
	}

	public String getUserCount() throws RangerRestClientException
	{
		try {
			return get("/xusers/users/count",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get user count." ,e);
		}
	}

	public String getAllGroups() throws RangerRestClientException
	{
		try {
			return get("/xusers/groups",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to all user.." ,e);
		}
	}

	public String getGroupByName(String groupName) throws RangerRestClientException
	{
		try {
			return get("/xusers/groups/groupName/"+groupName,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get group by name." ,e);
		}

	}

	public String getGroupById(int groupId) throws RangerRestClientException
	{
		try {
			return get("/xusers/secure/groups/"+groupId,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to  group by ID." ,e);
		}
	}

	public String getGroupCount() throws RangerRestClientException
	{
		try {
			return get("/xusers/groups/count",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get group count." ,e);
		}
	}

	//User-group mapping functions

	public String getUserGroupMappingById(int id) throws RangerRestClientException
	{
		try {
			return get("/xusers/groupusers/"+id,null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to group mapping by ID." ,e);
		}
	}
	public String getUserGroupMapping() throws RangerRestClientException
	{
		try {
			return get("/xusers/groupusers",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to user group mapping." ,e);
		}
	}

	public String getUserGroupMappingCount() throws RangerRestClientException
	{
		try {
			return get("/xusers/groupusers/count",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to user group mapping count." ,e);
		}
	}

	public String getGroupInfoByUserId(int userId) throws RangerRestClientException
	{
		try {
			return get("/xusers/"+userId+"/groups",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get group information by user." ,e);
		}
	}

	public String getUserInfoByGroupId(int groupId) throws RangerRestClientException
	{
		try {
			return get("/xusers/"+groupId+"/users",null,String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RangerRestClientException("Unable to get user information by group ID." ,e);
		}
	}
}
