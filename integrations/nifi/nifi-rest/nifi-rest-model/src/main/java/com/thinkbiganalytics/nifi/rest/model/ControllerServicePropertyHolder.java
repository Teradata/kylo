package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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
