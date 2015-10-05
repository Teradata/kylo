/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import com.thinkbiganalytics.alerts.spi.AlertSource;

/**
 *
 * @author Sean Felten
 */
public interface Alert {
    interface ID extends Serializable { }
    
    enum State { CREATED, UNHANDLED, IN_PROCESS, HANDLED, CLEARED }
    
    enum Level { INFO, WARNING, MINO, MAJOR, CRITICAL, FATAL }
    

    URI getType();
    
    String getDescription();
    
    Level getLevel();
    
    AlertSource getSource();
    
    boolean isRespondable();
    
    List<AlertChangeEvent> getEvents();
    
    <C> C getContent();
}
