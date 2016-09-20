package com.thinkbiganalytics.metadata.jpa.app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface KyloVersionRepository extends JpaRepository<JpaKyloVersion, UUID> {



}
