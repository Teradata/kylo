package com.thinkbiganalytics.kerberos;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.jdbc.HiveConnection;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created by Jeremy Merrifield on 10/28/16.
 */
public class TestKerberosKinit {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static final String ENVIRONMENT_HDP = "HDP";
    private static final String ENVIRONMENT_CLOUDERA = "CLOUDERA";

    public static void main(String[] args) throws Exception {
        final TestKerberosKinit testKerberosKinit = new TestKerberosKinit();
        Scanner scanner = new Scanner(System.in);

        System.out.println(" ");
        System.out.print("Which environment are you in? Enter 1 for HDP or 2 for Cloudera: ");
        String environmentCode = scanner.nextLine();
        if (StringUtils.isEmpty(environmentCode)) {
            environmentCode = "1";
        }

        String environment = null;
        if (environmentCode.equals("1")) {
            environment = ENVIRONMENT_HDP;
        } else if (environmentCode.equals("2")) {
            environment = ENVIRONMENT_CLOUDERA;
        } else {
            throw new Exception("Invalid environment code");
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: /etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml,/usr/hdp/current/hive-client/conf/hive-site.xml");
        System.out.print("Please enter the list of configuration resources: ");
        String configResources = scanner.nextLine();
        if (StringUtils.isEmpty(configResources)) {
            configResources = "/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: /etc/security/keytabs/hive-thinkbig.headless.keytab");
        System.out.print("Please enter the keytab file location: ");
        String keytab = scanner.nextLine();
        if (StringUtils.isEmpty(keytab)) {
            keytab = "/etc/security/keytabs/hive-thinkbig.headless.keytab";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: thinkbig");
        System.out.print("Please enter the real user principal name: ");
        String realUserPrincipal = scanner.nextLine();
        if (StringUtils.isEmpty(realUserPrincipal)) {
            realUserPrincipal = "thinkbig";
        }

        System.out.println(" ");
        System.out.println("Please enter Y/N (default is N)");
        System.out.print("Do you want to test with a proxy user: ");
        String proxyUser = scanner.nextLine();
        if (StringUtils.isEmpty(realUserPrincipal)) {
            proxyUser = "N";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: hdfs://sandbox.hortonworks.com:8020");
        System.out.print("Please enter the HDFS URL: ");
        String hdfsUrl = scanner.nextLine();
        if (StringUtils.isEmpty(hdfsUrl)) {
            hdfsUrl = "hdfs://sandbox.hortonworks.com:8020";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: localhost");
        System.out.print("Please enter the Hive host name: ");
        String hiveHost = scanner.nextLine();
        if (StringUtils.isEmpty(hiveHost)) {
            hiveHost = "localhost";
        }

        String proxyUserName = null;
        if ("Y".equalsIgnoreCase(proxyUser)) {
            System.out.println(" ");
            System.out.print("Please enter the proxy user: ");
            proxyUserName = scanner.next();
        }

        System.out.println(" ");
        System.out.println("Executing Kinit to generate a kerberos ticket");

        if ("Y".equalsIgnoreCase(proxyUser)) {
            System.out.println("Testing with the proxy user: " + proxyUserName);
            testKerberosKinit.testHdfsWithUserImpersonation(configResources, keytab, realUserPrincipal, proxyUserName, environment, hdfsUrl);

            //testKerberosKinit.testHiveJdbcConnectionWithUserImpersonation(configResources, keytab, realUserPrincipal, proxyUserName);
            testKerberosKinit.testHiveJdbcConnection(configResources, keytab, realUserPrincipal, proxyUserName, hiveHost);
        } else {
            System.out.println("No Proxy User");
            testKerberosKinit.testHdfsAsKerberosUser(configResources, keytab, realUserPrincipal, environment, hdfsUrl);
            testKerberosKinit.testHiveJdbcConnection(configResources, keytab, realUserPrincipal, null,  hiveHost);
        }
    }

    private void testHdfsWithUserImpersonation(final String configResources, final String keytab, final String principal, String proxyUser, final String environment, final String hdfsUrl) {
        final String path = "/user";
        try {
            final Configuration configuration = TestKerberosKinit.createConfigurationFromList(configResources);
            UserGroupInformation realugi = TestKerberosKinit.generateKerberosTicket(configuration, keytab, principal);
            System.out.println(" ");
            System.out.println("Sucessfully got a kerberos ticket in the JVM");
            System.out.println("current user is: " + realugi.getUserName());

            UserGroupInformation ugiProxy = UserGroupInformation.createProxyUser(proxyUser, realugi);
            System.out.println("proxy user is: " + ugiProxy.getUserName());
            ugiProxy.doAs(new PrivilegedExceptionAction<Object>() {
                public Object run() {
                    try {
                        searchHDFS(configuration, environment, path, hdfsUrl);
                    } catch (Exception e) {
                        throw new RuntimeException("Error testing HDFS with Kerberos Hive Impersonation", e);
                    }
                    return null;
                }
            });


        } catch (Exception e) {
            System.out.println("Error testing HDFS\n\n");
            e.printStackTrace();
        }
    }

    private void testHdfsAsKerberosUser(final String configResources, final String keytab, final String principal, final String environment, final String hdfsUrl) {
        final String path = "/user";
        try {
            final Configuration configuration = TestKerberosKinit.createConfigurationFromList(configResources);
            UserGroupInformation realugi = TestKerberosKinit.generateKerberosTicket(configuration, keytab, principal);
            System.out.println(" ");
            System.out.println("Sucessfully got a kerberos ticket in the JVM");
            System.out.println("current user is: " + realugi.getUserName());

            realugi.doAs(new PrivilegedExceptionAction<Object>() {
                public Object run() {
                    try {
                        searchHDFS(configuration, environment, path, hdfsUrl);
                    } catch (Exception e) {
                        throw new RuntimeException("Error testing HDFS with Kerberos", e);
                    }
                    return null;
                }
            });


        } catch (Exception e) {
            System.out.println("Error testing HDFS\n\n");
            e.printStackTrace();
        }
    }

    private void searchHDFS(Configuration configuration, final String environment, String hdfsPath, String hdfsUrl) throws Exception {
        configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        FileSystem fs = FileSystem.get(configuration);

        if (environment.equalsIgnoreCase(ENVIRONMENT_CLOUDERA)) {
            FileStatus[] status = fs.listStatus(new Path(hdfsUrl + hdfsPath));
            System.out.println("File Count: " + status.length);
        } else {
            if (environment.equalsIgnoreCase(ENVIRONMENT_HDP)) {
                FileStatus[] status = fs.listStatus(new Path(hdfsUrl + hdfsPath));
                System.out.println("File Count: " + status.length);
            }
        }
    }

    /*
    private void testHiveJdbcConnectionWithUserImpersonation(final String configResources, final String keytab,final String realUserPrincipal ,  String proxyUser) throws Exception {
        System.out.println("*******************");
        System.out.println("Testing user impersonation for a hive query for proxy user: " + proxyUser + " and authenticating as: " + realUserPrincipal);
        System.out.println("*******************");

        final Configuration configuration = TestKerberosKinit.createConfigurationFromList(configResources);
        UserGroupInformation realugi = TestKerberosKinit.generateKerberosTicket(configuration, keytab, realUserPrincipal);

        System.out.println(" ");
        System.out.println("Sucessfully got a kerberos ticket in the JVM");

        // Get delegation token for authenticated user
        HiveConnection realUserConnection = (HiveConnection) realugi.doAs(new PrivilegedExceptionAction<Connection>() {
            public Connection run() {
                Connection connection = null;
                try {
                    Class.forName(driverName);
                    connection = DriverManager.getConnection("jdbc:hive2://localhost:10000/default;principal=" + realUserPrincipal);

                } catch (Exception e) {
                    throw new RuntimeException("Error getting delegation token", e);
                }
                return connection;
            }
        });


        String delegationToken = realUserConnection.getDelegationToken(proxyUser, realUserPrincipal);

        System.out.println("Delegation token is: " + delegationToken);


        setDelegationToken(realugi, delegationToken, HiveAuthFactory.HS2_CLIENT_TOKEN);

        Connection con = (Connection) realugi.doAs(new PrivilegedExceptionAction<Object>() {
            public Object run() {
                Connection tcon = null;
                try {
                    Class.forName(driverName);
                    System.out.println("Getting connection");
                    tcon = DriverManager.getConnection("jdbc:hive2://localhost:10000/default;auth=delegationToken");

                    System.out.println("creating statement");
                    Statement stmt = tcon.createStatement();

                    String sql = "show databases";
                    ResultSet res = stmt.executeQuery(sql);
                    System.out.println(" \n");
                    System.out.println("Executing the Hive Query:");
                    System.out.println(" ");

                    int itr =1;

                    System.out.println("List of Databases");
                    while (res.next()) {
                        System.out.println(res.getString(itr));
                    }
                } catch (Exception e) {
                    System.out.println("Error testing showing hive databases");
                    e.printStackTrace();
                }
                return tcon;

            }
        });
        con.close();

        realUserConnection.cancelDelegationToken(delegationToken);
        realUserConnection.close();
        System.out.println(" ");
        System.out.println("Delegation token " + delegationToken + " has been removed");
    }


    public static void setDelegationToken(UserGroupInformation ugi, String tokenStr, String tokenService)
        throws IOException {
        Token<DelegationTokenIdentifier> delegationToken = new Token<DelegationTokenIdentifier>();
        delegationToken.decodeFromUrlString(tokenStr);
        delegationToken.setService(new Text(tokenService));
        ugi.addToken(delegationToken);
    }
    */

    /**
     *
     * @param configResources
     * @param keytab
     * @param realUserPrincipal
     * @param proxyUser
     * @throws Exception
     */
    private void testHiveJdbcConnection(final String configResources, final String keytab, final String realUserPrincipal, final String proxyUser, final String hiveHostName) throws Exception {

        final Configuration configuration = TestKerberosKinit.createConfigurationFromList(configResources);
        UserGroupInformation realugi = TestKerberosKinit.generateKerberosTicket(configuration, keytab, realUserPrincipal);

        System.out.println(" ");
        System.out.println("Sucessfully got a kerberos ticket in the JVM");

        HiveConnection realUserConnection = (HiveConnection) realugi.doAs(new PrivilegedExceptionAction<Connection>() {
            public Connection run() {
                Connection connection = null;
                try {
                    Class.forName(driverName);
                    String url = "jdbc:hive2://" + hiveHostName + ":10000/default;principal=" + realUserPrincipal;
                    if(proxyUser != null) {
                        url = url + ";hive.server2.proxy.user=" + proxyUser;
                    }

                    connection = DriverManager.getConnection(url);

                    Class.forName(driverName);

                    System.out.println("creating statement");
                    Statement stmt = connection.createStatement();

                    String sql = "show databases";
                    ResultSet res = stmt.executeQuery(sql);
                    System.out.println(" \n");
                    System.out.println("Executing the Hive Query:");
                    System.out.println(" ");

                    System.out.println("List of Databases");
                    while (res.next()) {
                        System.out.println(res.getString(1));
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Error creating connection with proxy user", e);
                }
                return connection;
            }
        });


    }

    /**
     *
     * @param configurationFiles
     * @return
     */
    private static Configuration createConfigurationFromList(String configurationFiles) {
        Configuration config = new Configuration();
        String[] resources = configurationFiles.split(",");
        for (String resource : resources) {
            config.addResource(new Path(resource));
        }
        return config;
    }

    /**
     *
     * @param configuration
     * @param keytabLocation
     * @param principal
     * @return
     * @throws IOException
     */
    private static UserGroupInformation generateKerberosTicket(Configuration configuration, String keytabLocation, String principal) throws IOException {
        System.setProperty("sun.security.krb5.debug", "false");
        configuration.set("hadoop.security.authentication", "Kerberos");
        UserGroupInformation.setConfiguration(configuration);

        System.out.println("Generating Kerberos ticket for principal: " + principal + " at key tab location: " + keytabLocation);
        UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytabLocation);
        return ugi;
    }
}
