package com.thinkbiganalytics.datalake.authorization.groups.ldap;

/*-
 * #%L
 * thinkbig-sentry-client
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

import com.thinkbiganalytics.datalake.authorization.client.SentryClientConfig;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.model.SentryGroup;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class LdapGroupList {

    private String OWNER = "kylo";
    private String DESCRIPTION = "Kylo Authorization Group";
    private String DEFAULT_ID="1";
    List<String> groupInfo;

    public void  getAllGroups(LdapTemplate ldapTemplate , String groupBaseDnPattern) {

        try
        {
            groupInfo =new  ArrayList<>();
            LdapQuery query = query().base(groupBaseDnPattern);
            groupInfo = ldapTemplate.list(query.base());
        }
        catch(NamingException e)
        {
            throw new RuntimeException("Unable to Groups from LDAP " + e.getMessage());
        }

    }

    public List<HadoopAuthorizationGroup>  getHadoopAuthorizationList(SentryClientConfig clientConfig , LdapTemplate ldapTemplate)
    {
        List<HadoopAuthorizationGroup> sentryHadoopAuthorizationGroups = new ArrayList<>();
        SentryGroup hadoopAuthorizationGroup = new SentryGroup();
        getAllGroups(ldapTemplate, clientConfig.getLdapGroupDnPattern() );

        for(String group:groupInfo){   

            if(group.contains("cn"))
            {
                /**
                 * Skip Processing - Do not include CN in group list
                 */
            }
            else
            {
                if(group.contains("ou"))
                {
                    group = group.split("=")[1];
                    hadoopAuthorizationGroup.setId(DEFAULT_ID);
                    hadoopAuthorizationGroup.setDescription(DESCRIPTION);
                    hadoopAuthorizationGroup.setName(group);
                    hadoopAuthorizationGroup.setOwner(OWNER);
                    sentryHadoopAuthorizationGroups.add(hadoopAuthorizationGroup);
                    hadoopAuthorizationGroup = new SentryGroup();
                }
            }
        }

        return sentryHadoopAuthorizationGroups;
    }
}
