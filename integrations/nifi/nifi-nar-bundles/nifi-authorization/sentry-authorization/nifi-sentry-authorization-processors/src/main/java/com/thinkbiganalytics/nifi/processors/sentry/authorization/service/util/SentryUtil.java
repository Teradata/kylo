package com.thinkbiganalytics.nifi.processors.sentry.authorization.service.util;
/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.datalake.authorization.client.SentryClient;

public class SentryUtil {

	private static final Logger log = LoggerFactory.getLogger(SentryUtil.class);

	private static final String NIFI = "nifi_";
	private static final String CATEGORY= "category";
	private static final String ALL= "ALL";
	private static final String DATABASE= "DATABASE";
	private static final String TABLE = "TABLE";

	SentryClient sentryClientObject ;

	/**
	 * 
	 * @param stmt : Statement object obtained from Thrift Connection
	 * @param group_list : Group List for which permisison needs to be granted
	 * @param category_name : Category Name
	 * @param feed_name : Feed Name 
	 * @param permission_level : Level at which permission needs to be granted
	 * @return : Return true/false based on policy creation status
	 */
	public boolean createPolicy(Statement stmt, String group_list, String category_name, String feed_name ,String permission_level) 
	{

		try
		{
			sentryClientObject = new SentryClient();

			log.info("Starting Sentry Policy Creation.");
			String sentry_policy_role = NIFI+category_name+"_"+feed_name+"_"+permission_level;

			log.info("Check if role already exists.");
			boolean ifRoleExists = sentryClientObject.checkIfRoleExists(stmt, sentry_policy_role);

			if (ifRoleExists)
			{
				return true;
			}

			log.info("Creating Sentry Role " + sentry_policy_role);
			//Create Role in Sentry Database
			boolean  roleCreation = sentryClientObject.createRole(stmt, sentry_policy_role);

			if (!roleCreation)
			{
				return false;
			}

			log.info("Assiging Role to Group");

			//Assign Role to All Groups
			String groupAssignmentArray[] = group_list.split(",");
			for(int groupCounter = 0 ; groupCounter < groupAssignmentArray.length ; groupCounter ++)
			{
				boolean rolToGroupAssgnt = sentryClientObject.grantRoleToGroup(stmt, sentry_policy_role, groupAssignmentArray[groupCounter]);
				if (!rolToGroupAssgnt)
				{
					return false;
				}
			}

			log.info("Granting Permission to Role");
			//Grant All Permission to Role for Feed
			if(permission_level.equalsIgnoreCase("category"))
			{
				boolean grantPriviledgeToRole = sentryClientObject.grantRolePriviledges(stmt, ALL, DATABASE, category_name, sentry_policy_role);
				
				if (!grantPriviledgeToRole)
				{
					return false;
				}
			}
			else
			{
				String tableList = constructResourceforPermissionHIVE(category_name, feed_name, permission_level);
				String tableAssignmentArray[] = tableList.split(",");
				for(int tableCounter = 0 ; tableCounter < tableAssignmentArray.length ; tableCounter++)
				{
					boolean grantPriviledgeToRole = sentryClientObject.grantRolePriviledges(stmt, ALL, TABLE, category_name+"."+tableAssignmentArray[tableCounter], sentry_policy_role);
					if (!grantPriviledgeToRole)
					{
						return false;
					}
				}

			}
			return true;
		}
		catch(Exception e)
		{
			log.info("Unkown exception occured.Failed to create Sentry Policy." );
			e.printStackTrace();
			return false;
		}
		
	}

	private String constructResourceforPermissionHIVE(String category_name, String feed_name, String permission_level) {

		String final_table_PermissionList ="";

		if (permission_level.equalsIgnoreCase(CATEGORY))
		{
			/**
			 * Give all permission at database level
			 */
			final_table_PermissionList = DATABASE;
		}
		else
		{
			/**
			 * Give Permissionto Feed Level
			 */
			final_table_PermissionList = feed_name + "," + feed_name + "_feed" + "," +
					feed_name + "_invalid" + "," +
					feed_name + "_profile"	+ "," +
					feed_name + "_valid" ;
		}

		return final_table_PermissionList;
	}
}
