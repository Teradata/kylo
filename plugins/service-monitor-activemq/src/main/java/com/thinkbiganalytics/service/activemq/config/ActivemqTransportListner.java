package com.thinkbiganalytics.service.activemq.config;

/*-
 * #%L
 * service-monitor-activemq
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



import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ActivemqTransportListner implements TransportListener
{
    private static final Logger log = Logger.getLogger(ActivemqTransportListner.class);

    @Override
    public void onCommand(Object command)
    {
        /**
         * Do Nothing
         */
    }

    @Override
    public void onException(IOException exception)
    {
        log.error("Unable to connect Activemq" , exception);
    }

    @Override
    public void transportInterupted()
    {
        log.error("Activemq service has interrupted..");
    }

    @Override
    public void transportResumed()
    {
        log.info("Activemq connection is re-established");
    }

}
