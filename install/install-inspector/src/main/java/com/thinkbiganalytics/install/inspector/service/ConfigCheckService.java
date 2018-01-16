package com.thinkbiganalytics.install.inspector.service;

import com.thinkbiganalytics.install.inspector.check.ConfigCheck;
import com.thinkbiganalytics.install.inspector.check.ConfigStatus;
import com.thinkbiganalytics.install.inspector.check.Configuration;
import com.thinkbiganalytics.install.inspector.check.Path;
import com.thinkbiganalytics.install.inspector.repository.ConfigCheckRepository;
import com.thinkbiganalytics.install.inspector.repository.ConfigRepository;

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
public class ConfigCheckService {

    @Autowired
    private ConfigCheckRepository checkRepo;

    @Autowired
    private ConfigRepository configRepo;

    public Page<ConfigCheck> getAllConfigChecks() {
        return new PageImpl<>(checkRepo.getAll());
    }

    public ConfigStatus execute(int configId, int configCheckId) {
        return checkRepo.get(configCheckId).execute(configRepo.get(configId));
    }

    public Configuration setPath(Path path) {
        return configRepo.createConfiguration(path);
    }
}
