/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.List;

/**
 *
 * @author Sean Felten
 */
public interface AllowableAction {
    
    String getSystemName();
    
    String getTitle();
    
    String getDescription();

    List<AllowableAction> getSubFunctions();
}
