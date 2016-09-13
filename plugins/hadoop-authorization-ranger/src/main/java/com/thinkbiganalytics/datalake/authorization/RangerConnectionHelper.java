package com.thinkbiganalytics.datalake.authorization;

import java.util.ArrayList;

public class RangerConnectionHelper {

    private static ArrayList<String> hdfsPermissionList = new ArrayList<String>();
    private static ArrayList<String> hivePermissionList = new ArrayList<String>();

    private String groupList;
    private String permissionList;
    private String permissionLevel;
    private String categoryName;
    private String feedName;
    private String hdfsRepositoryName;
    private String hiveRepositoryName;

    public static ArrayList<String> getHdfsPermissionList() {
        return hdfsPermissionList;
    }

    public static void setHdfsPermissionList(ArrayList<String> hdfsPermissionList) {
        RangerConnectionHelper.hdfsPermissionList = hdfsPermissionList;
    }

    public static ArrayList<String> getHivePermissionList() {
        return hivePermissionList;
    }

    public static void setHivePermissionList(ArrayList<String> hivePermissionList) {
        RangerConnectionHelper.hivePermissionList = hivePermissionList;
    }

    public String getGroupList() {
        return groupList;
    }

    public void setGroupList(String groupList) {
        this.groupList = groupList;
    }

    public String getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(String permissionList) {
        this.permissionList = permissionList;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getHdfsRepositoryName() {
        return hdfsRepositoryName;
    }

    public void setHdfsRepositoryName(String hdfsRepositoryName) {
        this.hdfsRepositoryName = hdfsRepositoryName;
    }

    public String getHiveRepositoryName() {
        return hiveRepositoryName;
    }

    public void setHiveRepositoryName(String hiveRepositoryName) {
        this.hiveRepositoryName = hiveRepositoryName;
    }

}
