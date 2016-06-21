/**
 *
 */
package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.category.Category.ID;

/**
 * @author Sean Felten
 */
public class CategoryNotFoundException extends MetadataException {

    private static final long serialVersionUID = 3867336790441208367L;

    private ID id;

    public CategoryNotFoundException(ID id) {
        super();
        this.id = id;
    }

    public CategoryNotFoundException(String message, ID id) {
        super(message);
        this.id = id;
    }

    public ID getId() {
        return id;
    }
}
