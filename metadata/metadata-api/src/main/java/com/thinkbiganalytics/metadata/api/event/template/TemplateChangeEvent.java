/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.template;

import java.security.Principal;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;

/**
 *
 * @author Sean Felten
 */
public class TemplateChangeEvent extends AbstractMetadataEvent<TemplateChange> {

    private static final long serialVersionUID = 1L;

    public TemplateChangeEvent(TemplateChange data) {
        super(data);
    }

    public TemplateChangeEvent(TemplateChange data, Principal user) {
        super(data, user);
    }

    public TemplateChangeEvent(TemplateChange data, DateTime time, Principal user) {
        super(data, time, user);
    }

}
