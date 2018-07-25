/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

import com.thinkbiganalytics.metadata.api.IndexControlled;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

/**
 *
 */
public interface IndexControlledMixin extends IndexControlled, WrappedNodeMixin {

    default boolean isAllowIndexing() {
        String allowIndexing = JcrPropertyUtil.getProperty(getNode(), JcrPropertyConstants.ALLOW_INDEXING, "Y"); // returns Y if property doesn't exist
        return allowIndexing.equals("Y");
    }

    default void setAllowIndexing(boolean allowIndexing) {
        setProperty(JcrPropertyConstants.ALLOW_INDEXING, allowIndexing?"Y":"N");
    }

}
