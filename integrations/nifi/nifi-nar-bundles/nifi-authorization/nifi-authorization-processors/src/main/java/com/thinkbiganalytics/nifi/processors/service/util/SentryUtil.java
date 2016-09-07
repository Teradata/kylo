package com.thinkbiganalytics.nifi.processors.service.util;

import java.sql.SQLException;
import java.sql.Statement;

public class SentryUtil {

	private static String NIFI = "nifi_";

	public void createPolicy(Statement stmt, String group_list, String category_name, String feed_name)
	{

		try
		{

			String sentry_policy_role = NIFI+category_name+"_"+feed_name;

			String roleCreation = "CREATE ROLE " + sentry_policy_role;

			String grantPermission = "GRANT ALL ON database "+category_name +"  TO ROLE " + sentry_policy_role;

			//Create Role in Sentry Database
			stmt.execute(roleCreation);

			//Assign Role to All Groups
			String groupAssignmentArray[] = group_list.split(",");
			for(int groupCounter = 0 ; groupCounter < groupAssignmentArray.length ; groupCounter ++)
			{
				stmt.execute("GRANT ROLE" + sentry_policy_role + " TO GROUP " + groupAssignmentArray[groupCounter]);
			}

			//Grant All Permission to Role for Feed
			stmt.execute(grantPermission);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

	}
}
