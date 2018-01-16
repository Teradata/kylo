package com.thinkbiganalytics.install.inspector.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.thinkbiganalytics.install.inspector.check.ConfigCheck;
import com.thinkbiganalytics.install.inspector.check.ConfigStatus;
import com.thinkbiganalytics.install.inspector.check.Configuration;
import com.thinkbiganalytics.install.inspector.check.Path;
import com.thinkbiganalytics.install.inspector.security.AuthoritiesConstants;
import com.thinkbiganalytics.install.inspector.service.ConfigCheckService;
import com.thinkbiganalytics.install.inspector.web.rest.util.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;
import javax.websocket.server.PathParam;

/**
 * REST controller for configuration checks.
 */
@RestController
@RequestMapping("/api")
public class ConfigCheckResource {

    private final Logger log = LoggerFactory.getLogger(ConfigCheckResource.class);

    private final ConfigCheckService configCheckService;


    public ConfigCheckResource(ConfigCheckService configCheckService) {
        this.configCheckService = configCheckService;
    }

    /**
     * GET /config : get all configuration checks
     *
     * @return the ResponseEntity with status 200 (OK) and with body all configuration checks
     */
    @GetMapping("/check")
    @Timed
    public ResponseEntity<List<ConfigCheck>> getAllConfigChecks() {
        final Page<ConfigCheck> page = configCheckService.getAllConfigChecks();
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/config");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * POST  /config  : Set new configuration path
     * <p>
     *
     * @param installPath installation path
     */
    @PostMapping("/config")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Configuration> setKyloPath(@Valid @RequestBody Path installPath) throws URISyntaxException {
        log.debug("REST request to set new Kylo installation path : {}", installPath);
        Configuration config = configCheckService.setPath(installPath);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    /**
     * POST  /config  : Run configuration check
     * <p>
     *
     * @param configId configuration id
     * @param checkId configuration check id
     */
    @GetMapping("/config/{configId}/{checkId}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<ConfigStatus> runConfigCheck(@Valid @PathParam("configId") int configId, @Valid @PathParam("checkId") int checkId) throws URISyntaxException {
        log.debug("REST request to execute configuration check : {}", checkId);
        ConfigStatus status = configCheckService.execute(configId, checkId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
