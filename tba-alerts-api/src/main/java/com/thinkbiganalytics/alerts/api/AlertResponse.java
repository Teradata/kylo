/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 *
 * @author Sean Felten
 */
public interface AlertResponse {

    <C> void inProgress(C content);
    
    <C> void handle(C content);
    
    <C> void unHandle(C content);
    
    void clear();
}
