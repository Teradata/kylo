package com.thinkbiganalytics.datalake.authorization.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shashi Vishwakarma on 9/9/16.
 */

public class SentryClient {

	private static final Logger log = LoggerFactory.getLogger(SentryClient.class);

	private SentryClientConfig clientConfig;

	public SentryClient()
	{
		/**
		 *  Nothing to do here.
		 */
	}

	public SentryClient(SentryClientConfig config) {
		this.clientConfig = config;
	}

	/**
	 * 
	 * @param stmt : Statement object obtained from connection
	 * @param roleName : role name to be created 
	 * @return
	 * @throws SentryClientException
	 */
	public boolean createRole(Statement stmt ,String roleName) throws SentryClientException
	{
		try {
			stmt.execute("create role " + roleName);
			log.warn("Sentry role " + roleName +" created successfully.");
			return true;

		} catch (SQLException e) {

			if (e.getMessage().contains("SentryAlreadyExistsException"))
			{
				log.warn("Sentry policy already exists. Skipping Policy creation.");
			}

			if (e.getMessage().contains("SentryAccessDeniedException"))
			{
				log.error("User does not has sufficient permission to create policy. Routing to failure.");
			}

			if (e.getMessage().contains("SentryUserException")) 
			{
				log.info("Unkown exception occured.Failed to create Sentry Policy." );
			}

			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 
	 * @param stmt : Statement object obtained from connection
	 * @param roleName : role to be deleted.
	 * @return
	 * @throws SentryClientException
	 */
	public boolean dropRole(Statement stmt , String roleName ) throws SentryClientException
	{
		try {
			stmt.execute("drop role " + roleName);
			log.info("Role  "+roleName+" dropped successfully ");
			return true;
		} catch (SQLException e) {

			if (e.getMessage().contains("SentryAccessDeniedException"))
			{
				log.error("User does not has sufficient permission to perform operation. Routing to failure.");
			}

			if (e.getMessage().contains("SentryUserException")) 
			{
				log.info("Unkown exception occured.Failed to drop role." );
			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param stmt : Statement object obtained from connection
	 * @param roleName : sentry role name  
	 * @param groupName : group name to be granted 
	 * @return
	 */

	public boolean grantRoleToGroup(Statement stmt , String roleName , String groupName) 
	{
		String queryString = "GRANT ROLE " + roleName + " TO GROUP " + groupName + "";
		try {
			stmt.execute(queryString);
			log.info("Role " +roleName+" is assigned to group " + groupName +". ");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (e.getMessage().contains("SentryAccessDeniedException"))
			{
				log.error("Failed in grantRoleToGroup : User does not has sufficient permission to perform operation. Routing to failure.");
			}

			if (e.getMessage().contains("SentryUserException")) 
			{
				log.info("Unkown exception occured.Failed grant role to group." );
			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param stmt : Statement object obtained from connection
	 * @param previledge : ALL/Select
	 * @param objectType : DATABASE/TABLE
	 * @param objectName : Database name or table name 
	 * @param roleName : role name to be granted 
	 * @return 
	 */
	public boolean grantRolePriviledges(Statement stmt , String previledge , String objectType , String objectName , String roleName)
	{
		String dbName[] = objectName.split("\\.");
		String useDB = "use " + dbName[0]+"";
		String queryString = "GRANT " + previledge +  " ON " + objectType + " "+ objectName + "  TO ROLE " + roleName;
		log.info("Sentry Query Formed --" +queryString);
		try {
			stmt.execute(useDB);
			stmt.execute(queryString);
			log.info("Successfully assigned priviledge " + previledge +" to role " + roleName +" on " +objectName+".");
			return true;
		} catch (SQLException e) {

			if (e.getMessage().contains("SentryAccessDeniedException"))
			{
				log.error("Failed in grantRolePriviledges : User does not has sufficient permission to perform operation. Routing to failure.");
			}

			if (e.getMessage().contains("SentryUserException")) 
			{
				log.info("Unkown exception occured.Failed grant role priviledge." );
			}
			e.printStackTrace();
			return false;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			log.error("Failed to obtain database name from " +objectName + ". Routing to failure." );
			return false;
		}
	}

	/**
	 * 
	 * @param stmt : Statement object obtained from connection
	 * @param roleName : Check role if it is already created by Kylo
	 * @return true/false
	 */
	public boolean checkIfRoleExists(Statement stmt , String roleName)
	{
		boolean matchFound = false;
		String queryString = "SHOW ROLES";
		try {

			ResultSet  resultSet = stmt.executeQuery(queryString);
			while(resultSet.next()) 
			{

				String obtainedRole = resultSet.getString(1);
				if (obtainedRole.equalsIgnoreCase(roleName))
				{
					matchFound = true;
				}

			}

			if(matchFound)
			{
				/**
				 * Return true of role is present in result set
				 */
				log.info("Role " + roleName+" found in Sentry database.");
				return true;
			}
			else
			{
				log.info("Role " + roleName+"  not found in Sentry database.");
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (e.getMessage().contains("SentryAccessDeniedException"))
			{
				log.error("Failed in checkIfRoleExists : Unable to search "+roleName+"role. Routing to failure.");
			}

			if (e.getMessage().contains("SentryUserException")) 
			{
				log.info("Unkown exception occured.Failed to search "+roleName+" role. " );
			}
			e.printStackTrace();
			return false;
		}
	}

	public boolean revokeRoleFromGroup() {
		return false;
	}

	public boolean revokeRolePriviledges()
	{
		return false;
	}

}
