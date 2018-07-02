package com.thinkbiganalytics;
/*-
 * #%L
 * kylo-commons-util
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
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

public class UsernameCaseStrategyUtil {
    private static String propertySuffix = "username.case";
    private static String hiveServer2Property = "hive.server2.proxy.user.case";
    public static String hiveDatasourcePrefix = "hive.datasource";

    @Inject
    private Environment environment;


    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UsernameCaseStrategyUtil.class);

    public static enum UsernameCaseStrategy {
        AS_SPECIFIED,LOWER_CASE,UPPER_CASE;
    }



    public UsernameCaseStrategy getHiveUsernameCaseStrategy(){
        return getUsernameCaseStrategy(hiveDatasourcePrefix);
    }
    /**
     * Gets the environment setting for how the username case sensitivity should be handled
     * By default it uses the exact case as specified
     * @param prefix
     * @return
     */
    public UsernameCaseStrategy getUsernameCaseStrategy(String prefix){
        UsernameCaseStrategy usernameCase = UsernameCaseStrategy.AS_SPECIFIED;
        try {
            if(!prefix.endsWith(".")){
                prefix +=".";
            }
            String strategy = environment.getProperty(prefix+propertySuffix);
            if(strategy == null && prefix.equalsIgnoreCase(hiveDatasourcePrefix+".")){
                strategy = environment.getProperty(hiveServer2Property,UsernameCaseStrategy.AS_SPECIFIED.name());
            }
            usernameCase = UsernameCaseStrategy.valueOf(strategy);
        }catch (Exception e){
            usernameCase = UsernameCaseStrategy.AS_SPECIFIED;
        }
        return usernameCase;
    }

    public static String convertUsernameCase(String username, String usernameCase){
        UsernameCaseStrategy strategy = UsernameCaseStrategy.AS_SPECIFIED;
        try {
            strategy = UsernameCaseStrategy.valueOf(usernameCase);
        }catch(Exception e){

        }
        return convertUsernameCase(username,strategy);
    }

    public static String  convertUsernameCase(String username, UsernameCaseStrategy usernameCase){
        if(usernameCase == UsernameCaseStrategy.LOWER_CASE){
            return username.toLowerCase();
        }
        else if(usernameCase == UsernameCaseStrategy.UPPER_CASE){
            return username.toUpperCase();
        }
        return username;

    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
