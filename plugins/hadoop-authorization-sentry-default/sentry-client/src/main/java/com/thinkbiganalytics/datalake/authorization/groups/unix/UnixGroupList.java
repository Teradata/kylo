package com.thinkbiganalytics.datalake.authorization.groups.unix;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixGroupList {

    private static final Logger log = LoggerFactory.getLogger(UnixGroupList.class);

    private String OWNER = "kylo";
    private String DESCRIPTION = "Kylo Authorization Group";

    private HashMap<String , String> groupInfo ;
    SentryGroup sentryGroup ;
    public void  generateGroupList(String groupFilePath) 
    {
        String[] splitedUnixGroup ;
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        List<String> groupList = new ArrayList<>();
        groupInfo = new HashMap<>();

        try 
        {
            fileReader = new FileReader(groupFilePath);
            bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            if (line == null) {
                throw new RuntimeException("Unable to generate groups - Linux group file is empty." );
            }

            while ((line = bufferedReader.readLine()) != null) {

                stringBuilder.append(line);

                if(line.startsWith("#") || line.isEmpty())
                {
                    continue;
                }

                splitedUnixGroup = line.split(":");

                if(splitedUnixGroup.length < 3)
                {
                    throw new RuntimeException("Invalid Unix Group Format. Unable to parse " +groupFilePath);
                }

                groupList.add(splitedUnixGroup[0]);
                groupInfo.put(splitedUnixGroup[0], splitedUnixGroup[2]);
            }

        } catch (Exception e) {
            log.error("Error parsing unix group {}", e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            try {
                if(bufferedReader==null ){}
                else
                    bufferedReader.close();

                if(fileReader==null ){}
                else
                    fileReader.close();

            } catch (IOException ioe) {
                log.warn("I/O error closing buffered reader for stream" + ioe.getMessage() +ioe);
            }
        }
    }

    public List<HadoopAuthorizationGroup>  getHadoopAuthorizationList(SentryClientConfig clientConfig)
    {
        List<HadoopAuthorizationGroup> sentryHadoopAuthorizationGroups = new ArrayList<>();
        SentryGroup hadoopAuthorizationGroup = new SentryGroup();
        generateGroupList(clientConfig.getLinuxGroupFilePath());

        for(Map.Entry<String, String> group:groupInfo.entrySet()){    
            hadoopAuthorizationGroup.setId(group.getValue());
            hadoopAuthorizationGroup.setDescription(DESCRIPTION);
            hadoopAuthorizationGroup.setName(group.getKey());
            hadoopAuthorizationGroup.setOwner(OWNER);
            sentryHadoopAuthorizationGroups.add(hadoopAuthorizationGroup);
            hadoopAuthorizationGroup = new SentryGroup();
        }
        return sentryHadoopAuthorizationGroups;
    }
}
