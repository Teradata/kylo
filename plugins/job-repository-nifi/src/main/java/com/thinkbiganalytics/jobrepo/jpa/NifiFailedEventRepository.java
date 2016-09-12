package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiFailedEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiFailedEventRepository extends JpaRepository<NifiFailedEvent, NifiFailedEvent.NiFiFailedEventPK>, QueryDslPredicateExecutor<NifiFailedEvent> {


}
