package com.thinkbiganalytics.feedmgr.sla;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;


public class TestSlaVelocityEmail extends VelocityEmailTemplate {

    private String emailAddress;

    private boolean success;

    private String exceptionMessage;
    public TestSlaVelocityEmail(){

    }

    public TestSlaVelocityEmail(String subject, String body, String emailAddress) {
        super(subject, body);
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
