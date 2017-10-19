package com.thinkbiganalytics.cluster;

/*-
 * #%L
 * kylo-cluster-manager-core
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

import java.io.Serializable;

/**
 * Created by sr186054 on 10/13/17.
 */
public class EnsureMessageDeliveryMessage implements Serializable{

    private static final long serialVersionUID = -7627012532184562122L;

    private String messageId;
    enum MESSAGE_ACTION {
        SENT,RECEIVED;
    }
    private MESSAGE_ACTION messageAction;

    public EnsureMessageDeliveryMessage(String messageId, MESSAGE_ACTION messageAction) {
        this.messageId = messageId;
        this.messageAction = messageAction;
    }

    public String getMessageId() {
        return messageId;
    }

    public MESSAGE_ACTION getMessageAction() {
        return messageAction;
    }
}
