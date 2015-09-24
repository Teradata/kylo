package com.thinkbiganalytics.scheduler;

import java.util.UUID;

/**
 * Created by sr186054 on 9/23/15.
 */
public class ScheduleIdentifier {
    private String name;
    private String groupName = "DEFAULT";


    public ScheduleIdentifier(){
        this.name = createUniqueName(this.groupName);
    }

    public ScheduleIdentifier(String name, String groupName){
        this.name = name;
        this.groupName = groupName;
        if(this.groupName == null || this.groupName.trim().equals("")) {
            this.groupName = "DEFAULT";
        }
    }

    public String getName() {
        return name;
    }


    public String getGroupName() {
        return groupName;
    }



    public String getUniqueName(){
        return name+"_"+ScheduleIdentifier.createUniqueName(this.groupName);
    }
    public static String createUniqueName(String item) {

        String n1 = UUID.randomUUID().toString();
        String n2 = UUID.nameUUIDFromBytes(item.getBytes()).toString();
        return String.format("%s-%s", new Object[]{n2.substring(24), n1});
    }
}
