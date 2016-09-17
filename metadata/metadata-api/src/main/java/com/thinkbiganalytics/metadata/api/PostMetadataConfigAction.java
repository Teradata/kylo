/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import org.springframework.core.annotation.Order;

/**
 * Defines an action that should be performed once the metadata system has started and been configured.
 * Any Spring bean implementing this interface will automatically be detected and invoked once configuration
 * has completed successfully.
 * @author Sean Felten
 */
@Order(PostMetadataConfigAction.DEFAULT_ORDER)
public interface PostMetadataConfigAction extends Runnable {

    public static final int DEFAULT_ORDER = 0;
}
