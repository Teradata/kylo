package com.thinkbiganalytics.datalake.rangeradmin;

import java.util.ArrayList;

import com.thinkbiganalytics.datalake.rangeradmin.domain.HDFSPolicy;
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

		RangerRestClientConfig  rangerClientConfiguration = new RangerRestClientConfig("127.0.0.1","admin","adminshashi1");
		rangerClientConfiguration.setPort(6080);

		RangerRestClient rangerClientObj = new RangerRestClient(rangerClientConfiguration);

		//Below code is just for demonstration of different features.

		
		/**
		 * Test getPolicy function pass policyId to retrieve information for that policy
		 */

		System.out.println("\n Policy Retrived \n");
		System.out.println(rangerClientObj.getPolicy(24));

		/**
		 * Test search policy function set required search criteria
		 */

		System.out.println("\nPolicy searched \n");
		SearchPolicy search = new SearchPolicy();
		search.setPolicyName("pcng_security");
		search.setEnabled(true);
		System.out.println(rangerClientObj.searchPolicies(search.searchCriteria())+"\n");		

		/**
		 * Test create policy function
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
		permList.add("execute");

		policy.setPolicyName("shashi_test3");
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

		rangerClientObj.createPolicy(policy.policyJson());
		System.out.println("Policy created");
	}

}
