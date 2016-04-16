package com.thinkbiganalytics.servicemonitor.support;


import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Utility that helps parse the services defined in the .properties file for what services/components Ambari should track
 * {SERVICE_NAME}/[{COMPONENT_NAME},{COMPONENT_NAME}],{SERVICE_NAME}...
 * COMPONENT_NAMES are optional
 *
 * Example application.properties
 * ambari.services.status=HIVE/[HIVE_CLIENT],HDFS
 *   - this will track the HIVE Service and just the HIVE_CLIENT
 * ambari.services.status=HDFS,HIVE,MAPREDUCE2,SQOOP
 *   - this will track all services and all components
 *
 */
public class ServiceMonitorCheckUtil {
    public static String ALL_COMPONENTS = "ALL";

    public static List<String>getServiceNames(String services){
       List<String> list = new ArrayList<>();
        for(String service: StringUtils.split(services,",")) {
            String serviceName = service;
            if (service.contains("/")) {
                serviceName = StringUtils.substringBefore(serviceName, "/");
            }
            list.add(serviceName);
        }
        return list;
    }



    public static Map<String,List<String>>getMapOfServiceAndComponents(String services){
        Map<String, List<String>> map =
                new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        if(StringUtils.isNotBlank(services)) {
          String finalServiceString = services;
            if(services.contains("/")){
                int i = 1;
                String serviceName = null;
                for(String servicePart : StringUtils.split(services,"/")){
                    //service name is the first string before the /

                    if (serviceName == null) {
                        if(servicePart.contains(",")) {
                            serviceName = StringUtils.substringAfterLast(servicePart, ",");
                        }
                        else {
                            serviceName = servicePart;
                        }
                    }else {
                        String components = "";
                        String origComponents= "";
                        if(servicePart.contains("]")){
                            components = StringUtils.substringBeforeLast(servicePart,"]");
                            components = StringUtils.substringAfter(components, "[");
                            origComponents = "["+components+"]";
                        }
                        else {
                            components = StringUtils.substringBefore(servicePart,",");
                            origComponents = components;
                        }
                           String[] componentsArr = StringUtils.split(components, ",");
                        map.put(serviceName, Arrays.asList(componentsArr));


                        //now remove these from the finalServiceString
                        finalServiceString = StringUtils.replace(finalServiceString,serviceName+"/"+origComponents,"");

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
