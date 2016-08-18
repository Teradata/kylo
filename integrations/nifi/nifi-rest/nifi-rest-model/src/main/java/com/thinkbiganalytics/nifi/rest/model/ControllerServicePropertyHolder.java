package com.thinkbiganalytics.nifi.rest.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

/**
 * Created by sr186054 on 5/8/16.
 */
public class ControllerServicePropertyHolder {
    private List<ControllerServiceProperty> controllerServicePropertyList;
    private boolean valid;


    public ControllerServicePropertyHolder(List<ControllerServiceProperty> controllerServicePropertyList) {
        this.controllerServicePropertyList = controllerServicePropertyList;
        validate();
    }

    private void validate(){
        valid = false;
        if(this.controllerServicePropertyList == null || this.controllerServicePropertyList.isEmpty()){
            valid = true;
        }
        else {
           ControllerServiceProperty invalidProperty = Iterables.tryFind(controllerServicePropertyList, new Predicate<ControllerServiceProperty>() {
                @Override
                public boolean apply(ControllerServiceProperty controllerServiceProperty) {
                    return !controllerServiceProperty.isValid();
                }
            }).orNull();
            if(invalidProperty == null){
                valid = true;
            }
        }
    }

    public List<ControllerServiceProperty> getControllerServicePropertyList() {
        return controllerServicePropertyList;
    }

    public void setControllerServicePropertyList(List<ControllerServiceProperty> controllerServicePropertyList) {
        this.controllerServicePropertyList = controllerServicePropertyList;
    }

    public boolean isValid() {
        return valid;
    }


}
