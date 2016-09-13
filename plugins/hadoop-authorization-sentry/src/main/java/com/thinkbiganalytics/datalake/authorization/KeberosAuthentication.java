package com.thinkbiganalytics.datalake.authorization;

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is temporary kerberos authentication code, will be removed in actual release.
 *
 * @author sv186029
 */
public class KeberosAuthentication {

    private static final Logger log = LoggerFactory.getLogger(KeberosAuthentication.class);
    private final String AUTHENTICATION_MECHANISM = "hadoop.security.authentication";

    public boolean validateUserWithKerberos(String principal, String keytab) throws SentryClientException {

        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set(AUTHENTICATION_MECHANISM, "Kerberos");
        UserGroupInformation.setConfiguration(conf);

        try {
            UserGroupInformation.loginUserFromKeytab(principal, keytab);

            //Return true once authentication is successful.
            return true;

        } catch (Exception e) {

            throw new SentryClientException("Unable to authenticate user ", e);
        }

    }

}
