package com.thinkbiganalytics.datalake.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a temporary class for kerberos authentication , it will be removed after integrated with PCNG.
 *
 * @author sv186029
 */
public class SentryClient {

    private static final Logger log = LoggerFactory.getLogger(SentryClient.class);
    HiveServiceConnection hiveConnection;
    private SentryClientConfig clientConfig;
    private KeberosAuthentication kerberosObj;

    public SentryClient(SentryClientConfig config) {
        this.clientConfig = config;
    }

    public boolean executeAuthorization(String authroizationQuery) throws SentryClientException {

        //Kerberos authentication is just used for testing.It will be removed after testing.
        //Check if Kerberos enabled

        boolean validateStatus = kerberosObj.validateUserWithKerberos(clientConfig.getPrincipal(), clientConfig.getKeytab());

        if (validateStatus) {
            System.out.println("User successfully authenticated");
        } else {
            System.out.println("User failed authentication");
        }

        //call execute statement
        hiveConnection.executeQuery(authroizationQuery, hiveConnection.hiveServiceConnection());

        //Return Authorization Status
        return true;

    }

}
