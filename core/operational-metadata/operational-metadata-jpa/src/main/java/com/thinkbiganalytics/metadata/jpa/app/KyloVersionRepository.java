package com.thinkbiganalytics.metadata.jpa.app;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface KyloVersionRepository extends JpaRepository<JpaKyloVersion, JpaKyloVersion.KyloVersionId> {


}
