package com.thinkbiganalytics.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Jeremy Merrifield on 8/24/16.
 */
@Service
public class KerberosTicketGenerator {
    private static final Logger log = LoggerFactory.getLogger(KerberosTicketGenerator.class);

    public UserGroupInformation generateKerberosTicket(KerberosTicketConfiguration kerberosTicketConfiguration) throws IOException {
        Configuration config =  new Configuration();

        String[] resources = kerberosTicketConfiguration.getHadoopConfigurationResources().split(",");
        for(String resource: resources) {
            config.addResource(new Path(resource));
        }

        config.set("hadoop.security.authentication", "Kerberos");

        UserGroupInformation.setConfiguration(config);

        log.debug("Generating Kerberos ticket for principal: " + kerberosTicketConfiguration.getKerberosPrincipal() + " at key tab location: " + kerberosTicketConfiguration.getKeytabLocation());
        UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(kerberosTicketConfiguration.getKerberosPrincipal(), kerberosTicketConfiguration.getKeytabLocation());
        return ugi;
    }

}
