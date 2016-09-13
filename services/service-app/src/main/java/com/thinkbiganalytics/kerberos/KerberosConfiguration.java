package com.thinkbiganalytics.kerberos;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Created by Jeremy Merrifield on 8/30/16.
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages={"com.thinkbiganalytics"})
public class KerberosConfiguration {
    @Autowired
    private Environment env;

    @Bean(name="kerberosHiveConfiguration")
    public KerberosTicketConfiguration kerberosTicketHiveConfiguration(){
        String kerberosEnabled = env.getProperty("kerberos.hive.kerberosEnabled");
        String hadoopConfigurationResources = env.getProperty("kerberos.hive.hadoopConfigurationResources");
        String kerberosPrincipal = env.getProperty("kerberos.hive.kerberosPrincipal");
        String keytabLocation = env.getProperty("kerberos.hive.keytabLocation");

        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equals(kerberosEnabled) ? true: false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);

        if(config.isKerberosEnabled() && (StringUtils.isBlank(config.getHadoopConfigurationResources())
                                          || StringUtils.isBlank(config.getKeytabLocation()) || StringUtils.isBlank(config.getKerberosPrincipal()))) {
            throw new RuntimeException("Kerberos is enabled. hadoopConfigurationResources, kerberosPrincipal, and keytabLocation are required");
        }
        return config;
    }

}
