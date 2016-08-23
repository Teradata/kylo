/**
 * 
 */
package com.thinkbiganalytics.metadata.config;

/**
 * Defines an action that should be performed once the metadata system has started and been configured.
 * Any Spring bean implementing this interface will automatically be detected and invoked once configuration
 * has completed successfully.
 * @author Sean Felten
 */
public interface PostMetadataConfigAction extends Runnable {

}
