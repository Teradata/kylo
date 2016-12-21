/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Sean Felten
 */
public interface JpaAlertRepository extends JpaRepository<JpaAlert, JpaAlert.ID> {


    @Query("select alert from JpaAlert as alert "
            + "where alert.createdTime > :since")
//            + "join alert.events as event "
//            + "where event.changeTime > :since")
//    + "where (event.state = 'CREATED' or event.state = 'UNHANDLED') and event.changeTime > :since")
    List<JpaAlert> findAlertsSince(@Param("since") DateTime since);

}
