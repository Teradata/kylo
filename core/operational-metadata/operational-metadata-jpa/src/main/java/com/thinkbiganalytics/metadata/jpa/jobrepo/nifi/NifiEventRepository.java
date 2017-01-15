package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventRepository extends JpaRepository<JpaNifiEvent, JpaNifiEvent.NiFiEventPK> {


    @Query(value = "SELECT max(nifiEvent.eventId) from JpaNifiEvent nifiEvent")
    public Long findMaxEventId();

    @Query(value = "SELECT max(nifiEvent.eventId) from JpaNifiEvent nifiEvent where nifiEvent.clusterNodeId = :clusterNodeId")
    public Long findMaxEventId(@Param("clusterNodeId") String clusterNodeId);

}
