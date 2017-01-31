package com.thinkbiganalytics.nifi.processor;

/*-
 * #%L
 * thinkbig-nifi-framework-api
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

import com.thinkbiganalytics.nifi.logging.ComponentLogFactory;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;

/**
 * A standard implementation of {@link Processor} that works with all versions of NiFi.
 */
public abstract class AbstractNiFiProcessor extends AbstractProcessor {

    /*
     * Component log
     */
    private ComponentLog log;

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        // Create Spring application context
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
        applicationContext.refresh();

        // Get component log
        final ComponentLogFactory logFactory = applicationContext.getBean(ComponentLogFactory.class);
        log = logFactory.getLog(context);
    }

    /**
     * Gets the logger for this processor.
     *
     * @return the component log
     */
    protected final ComponentLog getLog() {
        return log;
    }
}
