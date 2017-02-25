/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import java.util.Optional;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 */
public class FeedSummary<C extends Category> extends AbstractJcrAuditableSystemEntity {

    public static final String DETAILS = "tba:details";

    public static final String CATEGORY = "tba:category";
    
    private FeedDetails details;

    public FeedSummary(Node node) {
        super(node);
    }
    
    public FeedSummary(Node node, JcrCategory category) {
        super(node);
        setProperty(CATEGORY, category);
    }
    
    public Optional<FeedDetails> getFeedDetails() {
        if (this.details == null) {
            if (JcrUtil.hasNode(getNode(), DETAILS)) {
                this.details = JcrUtil.getJcrObject(getNode(), DETAILS, FeedDetails.class);
                return Optional.of(this.details);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(this.details);
        }
    }
    
    protected C getCategory(Class<? extends JcrCategory> categoryClass) {
        C category = null;
        try {
            category = (C) getProperty(CATEGORY, categoryClass);
        } catch (Exception e) {
            if (category == null) {
                try {
                    category = (C) JcrUtil.constructNodeObject(node.getParent(), categoryClass, null);
                } catch (Exception e2) {
                    throw new CategoryNotFoundException("Unable to find category on Feed for category type  " + categoryClass + ". Exception: " + e.getMessage(), null);
                }
            }
        }
        if (category == null) {
            throw new CategoryNotFoundException("Unable to find category on Feed ", null);
        }
        return category;
    }

}
