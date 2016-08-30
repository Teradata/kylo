package com.thinkbiganalytics.datalake.sentry;

import java.sql.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Class for establishing connection Hive2 server
 * @author SV186013
 *
 */

public class HiveServiceConnection extends SentryClientConfig {

    Connection hiveConnectionObj;
    Statement executionStatment;

    private static final Logger log = LoggerFactory.getLogger(HiveServiceConnection.class); 

    public Connection hiveServiceConnection() throws SentryClientException
    {
        try
        {

            Class.forName(getDriverName());
            System.out.println("getting connection");
            hiveConnectionObj = DriverManager.getConnection(getConnectionString());

        }
        catch (Exception e) 
        {
            throw new SentryClientException("Unable to get beeline connection ." ,e); 
        }

        return hiveConnectionObj;
    }

    public boolean executeQuery(String authroizationQuery, Connection hiveConnectionObj) throws SentryClientException
    {
        boolean queryStatus = false;

        try 
        {
            executionStatment = hiveConnectionObj.createStatement();
            queryStatus= executionStatment.execute(authroizationQuery);
        } 
        catch (Exception e)
        {
            throw new SentryClientException("Unable to get beeline connection ." ,e); 
        }

        return queryStatus;
    }
}
