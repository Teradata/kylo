package com.thinkbiganalytics.metadata.api.alerts;
/*-
 * #%L
 * thinkbig-operational-metadata-api
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
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sr186054 on 7/19/17.
 */
public class OperationalAlerts {

    public static final URI FEED_ALERT_TYPE = URI.create("http://kylo.io/alert/feed");

    public static URI feedAlertType(String feedName){
        return URI.create(FEED_ALERT_TYPE.toString()+"/"+feedName);
    }

    public static URI feedJobFailureAlertType(String feedName, Long jobExecutionId){
        return URI.create(FEED_ALERT_TYPE.toString()+"/"+feedName+"/job/"+jobExecutionId);
    }
    public static URI feedJobFailureAlertType(String feedName, String jobExecutionId){
        return URI.create(FEED_ALERT_TYPE.toString()+"/"+feedName+"/job/"+jobExecutionId);
    }


    public static boolean matches(URI type, URI regexType){
        String regex = regexType.toString();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(type.toString());
        return m.matches();
    }

    public static boolean isFeedJobFailureType(URI type){
        return matches(type, feedJobFailureAlertType("*.*","*.*"));
    }
}
