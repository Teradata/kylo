package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiJobExecutionParameters;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/31/16.
 */
public interface NifiJobParametersRepository extends JpaRepository<NifiJobExecutionParameters, NifiJobExecutionParameters.NifiJobExecutionParametersPK> {


}