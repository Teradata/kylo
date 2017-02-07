package com.thinkbiganalytics.kerberos;

/*-
 * #%L
 * thinkbig-kerberos-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 */
@Service
public class KerberosTicketGenerator {

    private static final Logger log = LoggerFactory.getLogger(KerberosTicketGenerator.class);

    public UserGroupInformation generateKerberosTicket(KerberosTicketConfiguration kerberosTicketConfiguration) throws IOException {
        Configuration config = new Configuration();

        String[] resources = kerberosTicketConfiguration.getHadoopConfigurationResources().split(",");
        for (String resource : resources) {
            config.addResource(new Path(resource));
        }

        config.set("hadoop.security.authentication", "Kerberos");

        UserGroupInformation.setConfiguration(config);

        log.debug("Generating Kerberos ticket for principal: " + kerberosTicketConfiguration.getKerberosPrincipal() + " at key tab location: " + kerberosTicketConfiguration.getKeytabLocation());
        return UserGroupInformation.loginUserFromKeytabAndReturnUGI(kerberosTicketConfiguration.getKerberosPrincipal(), kerberosTicketConfiguration.getKeytabLocation());
    }

}
