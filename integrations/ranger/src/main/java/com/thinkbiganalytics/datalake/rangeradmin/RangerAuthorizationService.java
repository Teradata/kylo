package com.thinkbiganalytics.datalake.rangeradmin;

import java.util.ArrayList;

import com.thinkbiganalytics.datalake.rangeradmin.domain.HDFSPolicy;
import com.thinkbiganalytics.datalake.rangeradmin.domain.HivePolicy;
import com.thinkbiganalytics.datalake.rangeradmin.domain.SearchPolicy;

public class RangerAuthorizationService implements HadoopAuthorizationService{


	/**
	 * Implement Ranger Authentication Service. Initiate RangerClient and RangerClientConfig for 
	 * initializing service and invoke different methods of it.
	 */
	@Override

	public void initiateAuthorizationService() {
		// TODO Auto-generated method stub

		System.out.println("This is Ranger Autherization Service. ");

		RangerRestClientConfig  rangerClientConfiguration = new RangerRestClientConfig("127.0.0.1","admin","admin");
		rangerClientConfiguration.setPort(6080);

		RangerRestClient rangerClientObj = new RangerRestClient(rangerClientConfiguration);

		/**
		 * Test getPolicy function pass policyId to retrieve information for that policy
		 */

		//System.out.println("\n Policy Retrived \n");
		//System.out.println(rangerClientObj.getPolicy(66));

		/**
		 * Delete Policy 
		 */

		//System.out.println("Delete Policy");
		//rangerClientObj.deletePolicy((69));

		/**
		 * Test search policy function set required search criteria
		 */

		//System.out.println("\nPolicy searched \n");
		SearchPolicy search = new SearchPolicy();
		search.setPolicyName("hive_test2");
		search.setEnabled(true);
		//System.out.println(rangerClientObj.searchPolicies(search.searchCriteria())+"\n");		

		/**
		 * Test create policy function for HDFS
		 */

		HDFSPolicy policy = new HDFSPolicy();

		ArrayList<String> userList = new ArrayList<String>();
		ArrayList<String> groupList = new ArrayList<String>();
		ArrayList<String> permList = new ArrayList<String>();

		//Add remove users/groups/permissions to test createPolicy function
		//Passing permission list is mandatory if 'usersList' or 'groupList' is passed otherwise createPolicy() will fail

		userList.add("shashi");

		groupList.add("hadoop");
		groupList.add("root");

		permList.add("read");
		permList.add("write");
		//permList.add("execute");

		policy.setPolicyName("shashi_test2");
		policy.setResourceName("/user/shashi");
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
		//rangerClientObj.updatePolicy(policy.policyJson(), 68);

		/**
		 * Get policy count
		 */

		System.out.println(rangerClientObj.countPolicies());

		/**
		 * Test create policy function for Hive
		 */

		HivePolicy hivePolicy = new HivePolicy();

		ArrayList<String> userListHive = new ArrayList<String>();
		ArrayList<String> groupListHive = new ArrayList<String>();
		ArrayList<String> permListHive = new ArrayList<String>();

		//Add remove users/groups/permissions to test createPolicy function
		//Passing permission list is mandatory if 'usersList' or 'groupList' is passed otherwise createPolicy() will fail

		//userListHive.add("shashi");
		userListHive.add("shashi");

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

		/***
		 * Update policy using policy ID - Hive
		 * 
		 */

		serListHive.add("shashi");
		

		groupListHive.add("hadoop");
		//groupListHive.add("root");

		permListHive.add("select");
		//permListHive.add("create");
		permListHive.add("update");

		hivePolicy.setPolicyName("hive_test4");
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
		//rangerClientObj.updatePolicy(hivePolicy.policyJson(), 66);
	}

}
