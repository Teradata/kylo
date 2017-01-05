/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.net.URI;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;

/**
 *
 * @author Sean Felten
 */
public interface AlertCriteria {

    /**
     * Sets the maximum size of the list alerts retrieved.  The default, if no
     * limit is given, is AlertSource dependent.
     * @param size the maximum number of alerts to return
     * @return the updated criteria
     */
    AlertCriteria limit(int size);

    /**
     * @param type a type upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria type(URI type, URI... others);

    /**
     * @param state a state upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria state(State state, State... others);

    /**
     * @param level a level upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria level(Level level, Level... others);

    /**
     * Filters alerts to retrieve only those newer than the specified time.
     * @param time the maximum time
     * @return the updated criteria
     */
    AlertCriteria after(DateTime time);

    /**
     * Filters alerts to retrieve only those older than the specified time.
     * @param time the minimum time
     * @return the updated criteria
     */
    AlertCriteria before(DateTime time);
}
