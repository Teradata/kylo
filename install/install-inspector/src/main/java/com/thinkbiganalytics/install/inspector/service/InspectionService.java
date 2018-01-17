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
 * Service class for managing inspections.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepo;

    private final ConfigurationRepository configRepo;

    @Autowired
    public InspectionService(InspectionRepository inspectionRepo, ConfigurationRepository configRepo) {
        this.inspectionRepo = inspectionRepo;
        this.configRepo = configRepo;
    }

    public Page<Inspection> getAllInspections() {
        return new PageImpl<>(inspectionRepo.getAll());
    }

    public InspectionStatus execute(int configId, int inspectionId) {
        return configRepo.get(configId).execute(inspectionRepo.get(inspectionId));
    }

    public Configuration setPath(Path path) {
        return configRepo.createConfiguration(path);
    }
}
