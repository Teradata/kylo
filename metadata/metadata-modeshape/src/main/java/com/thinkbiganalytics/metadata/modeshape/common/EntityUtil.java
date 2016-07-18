package com.thinkbiganalytics.metadata.modeshape.common;


import java.nio.file.Paths;

import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;

/**
 * Utility to get basic Paths for schema
 */
public class EntityUtil {

    public static String pathForCategory(){
        return Paths.get("/metadata", "feeds").toString();
    }

    public static String pathForCategory(String categorySystemName){
        return Paths.get("/metadata", "feeds",categorySystemName).toString();
    }

    public static String pathForFeed(String categorySystemName, String feedSystemName){
        return Paths.get("/metadata", "feeds",categorySystemName,feedSystemName).toString();
    }

    public static String pathForFeedSource(String categorySystemName, String feedSystemName) {
        return Paths.get(pathForFeed(categorySystemName, feedSystemName), "feedSource").toString();
    }

    public static String pathForFeedDestination(String categorySystemName, String feedSystemName) {
        return Paths.get(pathForFeed(categorySystemName, feedSystemName), "feedDestination").toString();
    }

    public static String pathForDataSource() {
        return Paths.get("/metadata", "datasources").toString();
    }

    public static String pathForTemplates() {
        return Paths.get("/metadata", "templates").toString();
    }



    public static String asQueryProperty(String prop) {
        return "[" + prop + "]";
    }

}








