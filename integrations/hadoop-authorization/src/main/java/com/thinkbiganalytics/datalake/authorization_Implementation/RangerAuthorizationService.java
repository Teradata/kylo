package com.thinkbiganalytics.datalake.authorization_Implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.common.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.helper.RangerConnectionHelper;
import com.thinkbiganalytics.datalake.helper.SentryConnectionHelper;
import com.thinkbiganalytics.datalake.ranger.domain.HDFSPolicy;
import com.thinkbiganalytics.datalake.ranger.domain.SearchPolicy;
import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClient;
import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClientConfig;
import com.thinkbiganalytics.datalake.ranger.rest.client.RangerRestClientException;

public class RangerAuthorizationService implements HadoopAuthorizationService{

	private static final Logger log = LoggerFactory.getLogger(RangerAuthorizationService.class); 
	/**
	 * Implement Ranger Authentication Service. Initiate RangerClient and RangerClientConfig for 
	 * initializing service and invoke different methods of it.
	 * @throws Exception 
	 */
	
	
	@Override
	public void initiateAuthorizationService(RangerConnectionHelper ranerConnHelper) throws RangerRestClientException {
		// TODO Auto-generated method stub

		//RangerConnectionHelper ranerConnHelper ;
		
		System.out.println("This is Ranger Autherization Service. ");
		
		System.out.println("-----------------------------------------------");
		System.out.println("got hostname --->"+  ranerConnHelper.getHostname());
		System.out.println("-----------------------------------------------");
	
		
		RangerRestClientConfig  rangerClientConfiguration = new RangerRestClientConfig(ranerConnHelper.getHostname(),ranerConnHelper.getUsername(),ranerConnHelper.getPassword());
		rangerClientConfiguration.setPort(ranerConnHelper.getPort());

		RangerRestClient rangerClientObj = new RangerRestClient(rangerClientConfiguration);

		/**
		 * Test getPolicy function pass policyId to retrieve information for that policy
		 */

		
		//System.out.println("\n Policy Retrived \n");
		//System.out.println(rangerClientObj.getPolicy(5));

		/**
		 * Delete Policy 
		 */

		//System.out.println("Delete Policy");
		//rangerClientObj.deletePolicy((69));

		/**
		 * Test search policy function set required search criteria
		 */

		System.out.println("\nPolicy searched \n");
		SearchPolicy search = new SearchPolicy();
		search.setPolicyName("Sandbox_hive-2-20160229183752");
		search.setEnabled(true);
		System.out.println(rangerClientObj.searchPolicies(search.searchCriteria())+"\n");		

		/**
		 * Test create policy function for HDFS
		 */

/*		HDFSPolicy policy = new HDFSPolicy();

		ArrayList<String> userList = new ArrayList<String>();
		//ArrayList<String> groupList = new ArrayList<String>();
		ArrayList<String> permList = new ArrayList<String>();

		//Add remove users/groups/permissions to test createPolicy function
		//Passing permission list is mandatory if 'usersList' or 'groupList' is passed otherwise createPolicy() will fail

		//userList.add("user2");
		//userList.add("user1");

		String groupListInformation = ranerConnHelper.getGroupList();
		//ArrayList<String> groupList = (ArrayList<String>) Arrays.asList(groupListInformation.split(","));
		//List<String>  groupList = new ArrayList<String>(Arrays.asList(groupListInformation.split(" , ")));
		
		
		String [] items = groupListInformation.split(",");
		
		List<String> groupList = Arrays.asList(groupListInformation.split("\\s*,\\s*"));
		//List<String> groupList = (ArrayList<String>) Arrays.asList(items);
		System.out.println("---------------- group information ---- >" + groupList.toString());
		//groupList.to
		//groupList.add("hadoop");
		//groupList.add("root");

		ArrayList arraylist = new ArrayList(groupList);
		
		permList.add("read");
		permList.add("write");
		//permList.add("execute");

		policy.setPolicyName("user1_test3");
		policy.setResourceName("/user/newuser");
		policy.setDescription("This is test of Ranger API");
		policy.setRepositoryName("Sandbox_hadoop");
		policy.setRepositorytype("hdfs");
		policy.setIsEnabled("true");
		policy.setIsRecursive("true");
		policy.setIsAuditEnabled("true");
		policy.setUsers(userList);
		policy.setGroups(arraylist);
		HDFSPolicy.setPermissions(permList);
		rangerClientObj.createPolicy(policy.policyJson());
		System.out.println("Policy created");
*/		
	/*	HDFSPolicy policy = new HDFSPolicy();

		ArrayList<String> userList = new ArrayList<String>();
		ArrayList<String> groupList = new ArrayList<String>();
		ArrayList<String> permList = new ArrayList<String>();

		//Add remove users/groups/permissions to test createPolicy function
		//Passing permission list is mandatory if 'usersList' or 'groupList' is passed otherwise createPolicy() will fail

		userList.add("user2");
		userList.add("user1");

		groupList.add("hadoop");
		groupList.add("root");

		permList.add("read");
		permList.add("write");
		//permList.add("execute");

		policy.setPolicyName("user1_test3");
		policy.setResourceName("/user/newuser");
		policy.setDescription("This is test of Ranger API");
		policy.setRepositoryName("Sandbox_hadoop");
		policy.setRepositorytype("hdfs");
		policy.setIsEnabled("true");
		policy.setIsRecursive("true");
		policy.setIsAuditEnabled("true");
		policy.setUsers(userList);
		policy.setGroups(groupList);
		HDFSPolicy.setPermissions(permList);

		//System.out.println("Policy created");
		//rangerClientObj.createPolicy(policy.policyJson());
		//rangerClientObj.updatePolicy(policy.policyJson(), 70);

		*//**
		 * Get policy count
		 *//*

		//System.out.println(rangerClientObj.countPolicies());

		*//**
		 * Test create policy function for Hive
		 *//*

		HivePolicy hivePolicy = new HivePolicy();

		ArrayList<String> userListHive = new ArrayList<String>();
		ArrayList<String> groupListHive = new ArrayList<String>();
		ArrayList<String> permListHive = new ArrayList<String>();

		//Add remove users/groups/permissions to test createPolicy function
		//Passing permission list is mandatory if 'usersList' or 'groupList' is passed otherwise createPolicy() will fail

		//userListHive.add("shashi");
		userListHive.add("user");

		groupListHive.add("hadoop");
		//groupListHive.add("root");

		permListHive.add("select");
		//permListHive.add("create");
		permListHive.add("update");

		hivePolicy.setPolicyName("hive_test2");
		hivePolicy.setDatabases("default,test");
		hivePolicy.setTables("sample_07,testing");
		hivePolicy.setColumns("*");
		hivePolicy.setUdfs("");
		hivePolicy.setDescription("This is test of Ranger API for Hive");
		hivePolicy.setRepositoryName("Sandbox_hive");
		hivePolicy.setRepositoryType("hive");
		hivePolicy.setIsEnabled("true");
		hivePolicy.setIsAuditEnabled("true");
		hivePolicy.setUserList(userListHive);
		hivePolicy.setGroupList(groupListHive);
		HivePolicy.setPermList(permListHive);

		//System.out.println("Policy created");
		//rangerClientObj.createPolicy(hivePolicy.policyJson());

		*//***
		 * Update policy using policy ID - Hive
		 * 
		 *//*

		//userListHive.add("shashi");
		userListHive.add("user");

		groupListHive.add("hadoop");
		//groupListHive.add("root");

		permListHive.add("select");
		//permListHive.add("create");
		permListHive.add("update");

		hivePolicy.setPolicyName("hive_test");
		hivePolicy.setDatabases("default,test");
		hivePolicy.setTables("sample_07,testing");
		hivePolicy.setColumns("*");
		hivePolicy.setUdfs("");
		hivePolicy.setDescription("This is test of Ranger API for Hive");
		hivePolicy.setRepositoryName("Sandbox_hive");
		hivePolicy.setRepositoryType("hive");
		hivePolicy.setIsEnabled("true");
		hivePolicy.setIsAuditEnabled("true");
		hivePolicy.setUserList(userListHive);
		hivePolicy.setGroupList(groupListHive);
		HivePolicy.setPermList(permListHive);

		//System.out.println("Policy update");
		//rangerClientObj.updatePolicy(hivePolicy.policyJson(), 71);

		*//***
		 * Get user/group information
		 *//*

		//System.out.println(rangerClientObj.getAllUsers());
		//System.out.println(rangerClientObj.getUserByName("hive"));
		//System.out.println(rangerClientObj.getUserById(53));
		//System.out.println("Number of users: "+rangerClientObj.getUserCount());

		//Groups information

		//System.out.println(rangerClientObj.getAllGroups());
		//System.out.println(rangerClientObj.getGroupById(12));
		//System.out.println(rangerClientObj.getGroupByName("hadoop"));
		//System.out.println("Number of groups: "+rangerClientObj.getGroupCount());

		*//***
		 * User-Group mapping information
		 *//*

		System.out.println(rangerClientObj.getUserGroupMapping());
		//System.out.println(rangerClientObj.getUserGroupMappingCount());
		//System.out.println(rangerClientObj.getUserGroupMappingById(12));
		//System.out.println(rangerClientObj.getGroupInfoByUserId(53));
		//System.out.println(rangerClientObj.getUserInfoByGroupId(3));
*/	}
	
	@Override
	public void initiateAuthorizationService() throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initiateAuthorizationService(SentryConnectionHelper sentryConnHelper) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
