package com.thinkbiganalytics.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Jeremy Merrifield on 10/28/16.
 */
public class TestKerberosKinit {

    public static void main(String[] args) throws Exception {
        TestKerberosKinit kinit = new TestKerberosKinit();
        Scanner scanner = new Scanner(System.in);

        System.out.println(" ");
        System.out.print("Please enter the list of configuration resources: ");
        String configResources = scanner.next();

        System.out.println(" ");
        System.out.print("Please enter the keytab file location: ");
        String keytab = scanner.next();

        System.out.println(" ");
        System.out.print("Please enter the principal name: ");
        String principal = scanner.next();

        System.out.println(" ");
        System.out.println("Executing Kinit to generate a kerberos ticket");

        UserGroupInformation ugi = kinit.generateKerberosTicket(configResources, keytab, principal);

        System.out.println(" ");
        System.out.println("Sucessfully got a kerberos ticket in the JVM");

    }

    public UserGroupInformation generateKerberosTicket(String configurationResources, String keytabLocation, String principal) throws IOException {
        Configuration config =  new Configuration();

        //System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");

        String[] resources = configurationResources.split(",");
        for(String resource: resources) {
            config.addResource(new Path(resource));
        }

        config.set("hadoop.security.authentication", "Kerberos");

        UserGroupInformation.setConfiguration(config);

        System.out.println("Generating Kerberos ticket for principal: " + principal + " at key tab location: " + keytabLocation);
        UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytabLocation);
        return ugi;
    }

}
