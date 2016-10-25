package com.thinkbiganalytics.nifi.processor;

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

    /** Component log */
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
