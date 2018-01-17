package com.thinkbiganalytics.install.inspector.service;

import com.thinkbiganalytics.install.inspector.inspection.Inspection;
import com.thinkbiganalytics.install.inspector.inspection.InspectionStatus;
import com.thinkbiganalytics.install.inspector.inspection.Configuration;
import com.thinkbiganalytics.install.inspector.inspection.Path;
import com.thinkbiganalytics.install.inspector.repository.InspectionRepository;
import com.thinkbiganalytics.install.inspector.repository.ConfigurationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class InspectionService {

    @Autowired
    private InspectionRepository inspectionRepo;

    @Autowired
    private ConfigurationRepository configRepo;

    public Page<Inspection> getAllConfigChecks() {
        return new PageImpl<>(inspectionRepo.getAll());
    }

    public InspectionStatus execute(int configId, int configCheckId) {
        return inspectionRepo.get(configCheckId).execute(configRepo.get(configId));
    }

    public Configuration setPath(Path path) {
        return configRepo.createConfiguration(path);
    }
}
