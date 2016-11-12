package com.thinkbiganalytics.kerberos;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.shims.Utils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.jdbc.HiveConnection;
import org.apache.hive.service.auth.HiveAuthFactory;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created by Jeremy Merrifield on 10/28/16.
 */
public class TestKerberosKinit {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws Exception {
        final TestKerberosKinit testKerberosKinit = new TestKerberosKinit();
        Scanner scanner = new Scanner(System.in);

        System.out.println(" ");
        System.out.println("Hit enter to default to: /etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml,/usr/hdp/current/hive-client/conf/hive-site.xml");
        System.out.print("Please enter the list of configuration resources: ");
        String configResources = scanner.nextLine();
        if(StringUtils.isEmpty(configResources)) {
            configResources = "/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: /etc/security/keytabs/hive-thinkbig.headless.keytab");
        System.out.print("Please enter the keytab file location: ");
        String keytab = scanner.nextLine();
        if(StringUtils.isEmpty(keytab)) {
            keytab = "/etc/security/keytabs/hive-thinkbig.headless.keytab";
        }

        System.out.println(" ");
        System.out.println("Hit enter to default to: hive/sandbox.hortonworks.com@sandbox.hortonworks.com");
        System.out.print("Please enter the principal name: ");
        String principal = scanner.nextLine();
        if(StringUtils.isEmpty(principal)) {
            principal = "hive/sandbox.hortonworks.com@sandbox.hortonworks.com";
        }

        System.out.println(" ");
        System.out.print("Please enter the proxy user: ");
        String proxyUser = scanner.next();

        System.out.println(" ");
        System.out.println("Executing Kinit to generate a kerberos ticket");

        testKerberosKinit.testHdfsWithUserImpersonation(configResources, keytab, principal, proxyUser);

        testKerberosKinit.testHiveJdbcConnectionWithUserImpersonatoin(configResources, keytab, principal, proxyUser);

    }

    /**
     *
     * @param configResources
     * @param keytab
     * @param principal
     * @param proxyUser
     */
    private void testHdfsWithUserImpersonation(final String configResources, final String keytab, final String principal, String proxyUser) {
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
                        configuration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
                        configuration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
                        FileSystem fs = FileSystem.get(configuration);
                        FileStatus[] status = fs.listStatus(new Path("hdfs://sandbox.hortonworks.com:8020" + path));
                        System.out.println("File Count: " + status.length);

                        // Can run another test like this as different users to make sure permissions are right
                        //fs.create(new Path("/user/dladmin/testjer1.txt"));
                    } catch(Exception e) {
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

    private void testHiveJdbcConnectionWithUserImpersonatoin(final String configResources, final String keytab, final String principal, String proxyUser) throws Exception {
        System.out.println("*******************");
        System.out.println("Testing user impersonation for a hive query for proxy user: " + proxyUser + " and authenticating as: " + principal);
        System.out.println("*******************");

        final Configuration configuration = TestKerberosKinit.createConfigurationFromList(configResources);
        UserGroupInformation realugi = TestKerberosKinit.generateKerberosTicket(configuration, keytab, principal);

        System.out.println(" ");
        System.out.println("Sucessfully got a kerberos ticket in the JVM");

        // Get delegation token for authenticated user
        HiveConnection realUserConnection = (HiveConnection) realugi.doAs(new PrivilegedExceptionAction<Connection>() {
            public Connection run() {
                Connection connection = null;
                try {
                    Class.forName(driverName);
                    connection = DriverManager.getConnection("jdbc:hive2://localhost:10000/default;principal=" + principal);

                } catch (Exception e) {
                    throw new RuntimeException("Error getting delegation token", e);
                }
                return connection;
            }
        });

        String delegationToken = realUserConnection.getDelegationToken(proxyUser, principal);

        System.out.println("Delegation token is: " + delegationToken);

        Utils.setTokenStr(realugi, delegationToken, HiveAuthFactory.HS2_CLIENT_TOKEN);

        Connection con = (Connection) realugi.doAs(new PrivilegedExceptionAction<Object>() {
            public Object run() {
                Connection tcon = null;
                try {
                    Class.forName(driverName);
                    System.out.println("Getting connection");
                    tcon = DriverManager.getConnection("jdbc:hive2://localhost:10000/default;auth=delegationToken");

                    System.out.println("creating statement");
                    Statement stmt = tcon.createStatement();

                    String sql = "select * from jer123.test1 ";
                    ResultSet res = stmt.executeQuery(sql);
                    System.out.println(" \n");
                    System.out.println("Executing the Hive Query:");
                    System.out.println(" ");
                    if (res.next()) {
                        System.out.println("Success.. Here is the first column of the first result: " + res.getString(1));
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

    private static Configuration createConfigurationFromList(String configurationFiles) {
        Configuration config = new Configuration();
        String[] resources = configurationFiles.split(",");
        for (String resource : resources) {
            config.addResource(new Path(resource));
        }
        return config;
    }

    private static UserGroupInformation generateKerberosTicket(Configuration configuration, String keytabLocation, String principal) throws IOException {
        System.setProperty("sun.security.krb5.debug", "false");

        configuration.set("hadoop.security.authentication", "Kerberos");

        UserGroupInformation.setConfiguration(configuration);

        System.out.println("Generating Kerberos ticket for principal: " + principal + " at key tab location: " + keytabLocation);
        UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytabLocation);
        return ugi;
    }
}
