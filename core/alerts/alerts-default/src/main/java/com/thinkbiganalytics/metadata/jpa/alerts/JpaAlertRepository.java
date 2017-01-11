/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Sean Felten
 */
public interface JpaAlertRepository extends JpaRepository<JpaAlert, JpaAlert.ID>, JpaSpecificationExecutor<JpaAlert> {


    @Query("select alert from JpaAlert as alert "
            + "where alert.createdTime > :time")
    List<JpaAlert> findAlertsAfter(@Param("time") DateTime time);

    
}
