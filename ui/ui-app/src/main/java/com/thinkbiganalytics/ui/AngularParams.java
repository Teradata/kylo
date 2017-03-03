package com.thinkbiganalytics.ui;

/**
 * Helper utility to create the constructor array injection for angular modules
 */
public class AngularParams {

    private static String defaultArgs = "$scope, $mdDialog, AccessControlService, HttpService,ServicesStatusData,OpsManagerFeedService";

    public static void main(String[] args) {
        String params = "";
        if (args == null || args.length == 0) {
            args = defaultArgs.split(",");
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (params != "") {
                    params += ",";
                }
                params += "\"" + args[i].trim() + "\"";
            }
            System.out.println("["+params+",");
        }
    }
}
