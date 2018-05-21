package com.thinkbiganalytics.nifi.v2.savepoint;
/*-
 * #%L
 * kylo-nifi-core-processors
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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.flowfile.FlowFile;

public class SavepointFlowFileFilterUtil {


    public static boolean isExpired(FlowFile flowFile, long expirationDuration) {
        boolean expired = false;
        String waitStartTimestamp = flowFile.getAttribute(SavepointConstants.SAVEPOINT_START_TIMESTAMP);
        if (waitStartTimestamp != null) {
            long lWaitStartTimestamp = 0L;
            try {
                lWaitStartTimestamp = Long.parseLong(waitStartTimestamp);
            } catch (NumberFormatException nfe) {
                expired = false;
            }

            // check for expiration
            long now = System.currentTimeMillis();
            if (now > (lWaitStartTimestamp + expirationDuration)) {
                expired = true;
            }
        }
        return expired;
    }



    public static boolean isNew(FlowFile flowFile, String processorId) {
        String savepointProcessor = flowFile.getAttribute(SavepointConstants.SAVEPOINT_PROCESSOR_ID);
        return StringUtils.isBlank(savepointProcessor) || !processorId.equalsIgnoreCase(savepointProcessor);
    }

}
