package com.thinkbiganalytics.datalake.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sentry Authorization Service
 *
 * @author sv186029
 */
public class SentryAuthorizationService implements HadoopAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(SentryAuthorizationService.class);

    static String HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    static String BEELINE_URL = "jdbc:hive2://dn1.cdhtdaws.com:10000/test;principal=hive/dn1.cdhtdaws.com@CDHTDAWS.COM";
    static String KERBEROS_PRINCIPLE = "user@CDHTDAWS.COM";
    static String KERBEROS_KEYTAB = "/etc/security/keytabs/user.headless.keytab";

    @Override
    public void initiateAuthorizationService(AuthorizationConfiguration config) throws SentryClientException {

        System.out.println("This is Sentry Authorization Service.");

        SentryClientConfig sentryClientConfig = new SentryClientConfig();
        SentryClient sentryClient = new SentryClient(sentryClientConfig);

        //set required parameters
        sentryClientConfig.setDriverName(HIVE_DRIVER);
        sentryClientConfig.setConnectionString(BEELINE_URL);
        sentryClientConfig.setPrincipal(KERBEROS_PRINCIPLE);
        sentryClientConfig.setPrincipal(KERBEROS_KEYTAB);

        //Execute authorization granting statements
        sentryClient.executeAuthorization("CREATE ROLE developer");
        sentryClient.executeAuthorization("GRANT ROLE developer TO GROUP testsentry");
        sentryClient.executeAuthorization("GRANT ALL ON database test TO ROLE developer");
    }

}
