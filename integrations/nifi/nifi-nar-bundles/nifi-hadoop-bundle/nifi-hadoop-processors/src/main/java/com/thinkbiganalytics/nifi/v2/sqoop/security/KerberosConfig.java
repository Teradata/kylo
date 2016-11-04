package com.thinkbiganalytics.nifi.v2.sqoop.security;

import org.apache.nifi.logging.ComponentLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jagrut sharma
 */
/*
Class to store Kerberos Configuration
Used for doing a kinit in Kerberized environment
 */
public class KerberosConfig {

    private String kerberosPrincipal =null;
    private String kerberosKeytab=null;
    private String kerberosKinitLocation = "/usr/bin/kinit";    //default
    private ComponentLog logger = null;

    public KerberosConfig setLogger (ComponentLog logger) {
        this.logger = logger;
        return this;
    }

    public KerberosConfig setKerberosPrincipal (String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
        if (logger != null) {
            logger.info("Kerberos Principal set to {}", new Object[]{ this.kerberosPrincipal });
        }
        return this;
    }

    public KerberosConfig setKerberosKeytab (String kerberosKeytab) {
        this.kerberosKeytab = kerberosKeytab;
        if (logger != null) {
            logger.info("Kerberos Keytab set to {}", new Object[] { this.kerberosKeytab });
        }
        return this;
    }

    public KerberosConfig setKerberosKinitLocation (String kerberosKinitLocation) {
        if (kerberosKinitLocation == null) {
            logger.info("Kerberos Kinit location is provided as null."
                        + " Skipping setting it. Will use default value of " + this.kerberosKinitLocation);
            return this;
        }
        else if (!kerberosKinitLocation.contains("kinit")) {
            logger.info("Kerberos Kinit location is provided as " + kerberosKinitLocation
                        + ". Appears invalid. Skipping setting it. Will use default value of " + this.kerberosKinitLocation);
            return this;
        }
        this.kerberosKinitLocation = kerberosKinitLocation;
        logger.info("Kerberos Kinit Location set to {}", new Object[] { this.kerberosKinitLocation });
        return this;
    }

    public String getKinitCommandAsString() {
        //kinit nifi -kt /etc/security/keytabs/nifi.headless.keytab
        StringBuffer kInitCommand = new StringBuffer();
        return kInitCommand.append(kerberosKinitLocation)
            .append(" ")
            .append(kerberosPrincipal)
            .append(" ")
            .append("-kt")
            .append(" ")
            .append(kerberosKeytab)
            .toString();
    }

    public List<String> getKinitCommandAsList() {
        List<String> kinitCommandAsList = new ArrayList<>();
        kinitCommandAsList.add(kerberosKinitLocation);
        kinitCommandAsList.add(kerberosPrincipal);
        kinitCommandAsList.add("-kt");
        kinitCommandAsList.add(kerberosKeytab);
        return kinitCommandAsList;
    }

    public boolean isKerberosConfigured() {
        return ((kerberosPrincipal != null) && (kerberosKeytab != null));
    }
}
