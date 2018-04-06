package com.thinkbiganalytics.feedmgr.rest.model;
/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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
public class RemoteProcessGroupInputPort {


    private String templateName;
    private String inputPortName;
    private boolean selected;
    private boolean existing;


    public RemoteProcessGroupInputPort() {

    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName) {
        this.templateName = templateName;
        this.inputPortName = inputPortName;
    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName, boolean selected) {
       this(templateName,inputPortName);
        this.selected = selected;
    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName, boolean selected, boolean existing) {
        this.templateName = templateName;
        this.inputPortName = inputPortName;
        this.selected = selected;
        this.existing = existing;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getInputPortName() {
        return inputPortName;
    }

    public void setInputPortName(String inputPortName) {
        this.inputPortName = inputPortName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RemoteProcessGroupInputPort)) {
            return false;
        }

        RemoteProcessGroupInputPort that = (RemoteProcessGroupInputPort) o;

        if (templateName != null ? !templateName.equals(that.templateName) : that.templateName != null) {
            return false;
        }
        if (inputPortName != null ? !inputPortName.equals(that.inputPortName) : that.inputPortName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = templateName != null ? templateName.hashCode() : 0;
        result = 31 * result + (inputPortName != null ? inputPortName.hashCode() : 0);
        return result;
    }
}
