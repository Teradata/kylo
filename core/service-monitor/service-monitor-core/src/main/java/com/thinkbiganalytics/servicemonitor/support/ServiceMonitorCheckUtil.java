package com.thinkbiganalytics.servicemonitor.support;

/*-
 * #%L
 * thinkbig-service-monitor-core
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


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility that helps parse the services defined in the .properties file for what services/components a service should check.
 * This can be parsed in a property file with the following notation:
 * {SERVICE_NAME}/[{COMPONENT_NAME},{COMPONENT_NAME}],{SERVICE_NAME}... COMPONENT_NAMES
 */
public class ServiceMonitorCheckUtil {

    public static final String ALL_COMPONENTS = "ALL";

    /**
     * get all the services names as a list
     */
    public static List<String> getServiceNames(String services) {
        List<String> list = new ArrayList<>();
        for (String service : StringUtils.split(services, ",")) {
            String serviceName = service;
            if (service.contains("/")) {
                serviceName = StringUtils.substringBefore(serviceName, "/");
            }
            list.add(serviceName);
        }
        return list;
    }


    /**
     * get a map of the Service and any components that should be checked within that service.
     */
    public static Map<String, List<String>> getMapOfServiceAndComponents(String services) {
        Map<String, List<String>> map =
            new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        if (StringUtils.isNotBlank(services)) {
            String finalServiceString = services;
            if (services.contains("/")) {
                int i = 1;
                String serviceName = null;
                for (String servicePart : StringUtils.split(services, "/")) {
                    //service name is the first string before the /

                    if (serviceName == null) {
                        if (servicePart.contains(",")) {
                            serviceName = StringUtils.substringAfterLast(servicePart, ",");
                        } else {
                            serviceName = servicePart;
                        }
                    } else {
                        String components = "";
                        String origComponents = "";
                        if (servicePart.contains("]")) {
                            components = StringUtils.substringBeforeLast(servicePart, "]");
                            components = StringUtils.substringAfter(components, "[");
                            origComponents = "[" + components + "]";
                        } else {
                            components = StringUtils.substringBefore(servicePart, ",");
                            origComponents = components;
                        }
                        String[] componentsArr = StringUtils.split(components, ",");
                        map.put(serviceName, Arrays.asList(componentsArr));

                        //now remove these from the finalServiceString
                        finalServiceString = StringUtils.replace(finalServiceString, serviceName + "/" + origComponents, "");

                        //reset serviceName
                        serviceName = StringUtils.substringAfterLast(servicePart, ",");

                    }
                    i++;
                }
            }

            for (String service : StringUtils.split(finalServiceString, ",")) {
                String serviceName = service;
                map.put(serviceName, Arrays.asList(new String[]{ALL_COMPONENTS}));
            }
        }
        return map;
    }

}
