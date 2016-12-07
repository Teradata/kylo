/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.template;

import java.util.Objects;

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate.ID;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate.State;

/**
 *
 * @author Sean Felten
 */
public class TemplateChange extends MetadataChange {
    
    private static final long serialVersionUID = 1L;
    
    private final FeedManagerTemplate.ID templateId;
    private final FeedManagerTemplate.State templateState;
    
    public TemplateChange(ChangeType change, String descr, ID templateId, State state) {
        super(change, descr);
        this.templateId = templateId;
        this.templateState = state;
    }
    
    public TemplateChange(ChangeType change, ID templateId, State state) {
        this(change, "", templateId, state);
    }


    public FeedManagerTemplate.ID getTemplateId() {
        return templateId;
    }

    public FeedManagerTemplate.State getTemplateState() {
        return templateState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.templateId, this.templateState);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateChange) {
            TemplateChange that = (TemplateChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.templateId, this.templateId) &&
                   Objects.equals(this.templateState, that.templateState);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Template change ");
        return sb
                        .append("(").append(getChange()).append(") - ")
                        .append("ID: ").append(this.templateId)
                        .append(" state: ").append(this.templateState)
                        .toString();
            
    }

}
