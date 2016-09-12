package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventRepository extends JpaRepository<NifiEvent, NifiEvent.NiFiEventPK>, QueryDslPredicateExecutor<NifiEvent> {


}
