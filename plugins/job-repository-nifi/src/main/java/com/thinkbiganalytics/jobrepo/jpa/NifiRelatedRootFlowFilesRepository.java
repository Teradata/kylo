package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiRelatedRootFlowFiles;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiRelatedRootFlowFilesRepository extends JpaRepository<NifiRelatedRootFlowFiles, NifiRelatedRootFlowFiles.NifiRelatedFlowFilesPK> {


}
