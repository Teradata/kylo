package com.thinkbiganalytics.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.util.IdentifierUtil;

/**
 * Created by sr186054 on 9/23/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ScheduleIdentifier {
    private String name;
    private String group = "DEFAULT";


    public ScheduleIdentifier(){
        this.name = createUniqueName(this.group);
    }

    public ScheduleIdentifier(@JsonProperty("name")String name, @JsonProperty("group")String group){
        this.name = name;
        this.group = group;
        if(this.group == null || this.group.trim().equals("")) {
            this.group = "DEFAULT";
        }
    }

    public String getName() {
        return name;
    }


    public String getGroup() {
        return group;
    }



    @JsonIgnore
    public String getUniqueName(){
        return name+"_"+ScheduleIdentifier.createUniqueName(this.group);
    }
    @JsonIgnore
    public static String createUniqueName(String item) {
        return IdentifierUtil.createUniqueName(item);
    }



    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + (this.group == null?0:this.group.hashCode());
        result1 = 31 * result1 + (this.name == null?0:this.name.hashCode());
        return result1;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            ScheduleIdentifier other = (ScheduleIdentifier)obj;
            if(this.group == null) {
                if(other.group != null) {
                    return false;
                }
            } else if(!this.group.equals(other.group)) {
                return false;
            }

            if(this.name == null) {
                if(other.name != null) {
                    return false;
                }
            } else if(!this.name.equals(other.name)) {
                return false;
            }

            return true;
        }
    }

    public int compareTo(ScheduleIdentifier o) {
        if(this.group.equals("DEFAULT") && !o.group.equals("DEFAULT")) {
            return -1;
        } else if(!this.group.equals("DEFAULT") && o.group.equals("DEFAULT")) {
            return 1;
        } else {
            int r = this.group.compareTo(o.getGroup());
            return r != 0?r:this.name.compareTo(o.getName());
        }
    }
    
}
