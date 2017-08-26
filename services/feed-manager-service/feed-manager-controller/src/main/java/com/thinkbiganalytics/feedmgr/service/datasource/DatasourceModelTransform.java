package com.thinkbiganalytics.feedmgr.service.datasource;

/*-
 * #%L
 * kylo-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDetails;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.JdbcDatasourceDetails;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DerivedDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.UserDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Transform {@code Datasource}s between domain and REST objects.
 */
public class DatasourceModelTransform {

    private static final Logger log = LoggerFactory.getLogger(DatasourceModelTransform.class);

    /**
     * Level of detail to include when transforming objects.
     */
    public enum Level {
        /**
         * Include sensitive fields in result
         */
        ADMIN,

        /**
         * Include everything except sensitive fields in result
         */
        FULL,

        /**
         * Include basic field and connections in result
         */
        CONNECTIONS,

        /**
         * Include only basic fields in result
         */
        BASIC
    }

    /**
     * Provides access to {@code Datasource} domain objects
     */
    @Nonnull
    private final DatasourceProvider datasourceProvider;

    /**
     * Encrypts strings
     */
    @Nonnull
    private final TextEncryptor encryptor;

    /**
     * NiFi REST client
     */
    @Nonnull
    private final NiFiRestClient nifiRestClient;

    /**
     * Security service
     */
    @Nonnull
    private final SecurityService securityService;

    /**
     * Constructs a {@code DatasourceModelTransform}.
     *
     * @param datasourceProvider the {@code Datasource} domain object provider
     * @param encryptor          the text encryptor
     * @param nifiRestClient     the NiFi REST client
     * @param securityService    the security service
     */
    public DatasourceModelTransform(@Nonnull final DatasourceProvider datasourceProvider, @Nonnull final TextEncryptor encryptor, @Nonnull final NiFiRestClient nifiRestClient,
                                    @Nonnull final SecurityService securityService) {
        this.datasourceProvider = datasourceProvider;
        this.encryptor = encryptor;
        this.nifiRestClient = nifiRestClient;
        this.securityService = securityService;
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @param level  the level of detail
     * @return the REST object
     * @throws IllegalArgumentException if the domain object cannot be converted
     */
    public Datasource toDatasource(@Nonnull final com.thinkbiganalytics.metadata.api.datasource.Datasource domain, @Nonnull final Level level) {
        if (domain instanceof com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource) {
            final DerivedDatasource ds = new DerivedDatasource();
            updateDatasource(ds, (com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource) domain, level);
            return ds;
        } else if (domain instanceof com.thinkbiganalytics.metadata.api.datasource.UserDatasource) {
            return toDatasource((com.thinkbiganalytics.metadata.api.datasource.UserDatasource) domain, level);
        } else {
            throw new IllegalArgumentException("Not a supported datasource class: " + domain.getClass());
        }
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @param level  the level of detail
     * @return the REST object
     * @throws IllegalArgumentException if the domain object cannot be converted
     */
    public UserDatasource toDatasource(@Nonnull final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain, @Nonnull final Level level) {
        final DatasourceDetails details = domain.getDetails().orElse(null);
        if (details == null) {
            final UserDatasource userDatasource = new UserDatasource();
            updateDatasource(userDatasource, domain, level);
            return userDatasource;
        } else if (details instanceof JdbcDatasourceDetails) {
            final JdbcDatasource jdbcDatasource = new JdbcDatasource();
            updateDatasource(jdbcDatasource, domain, level);
            return jdbcDatasource;
        } else {
            throw new IllegalArgumentException("Not a supported datasource details class: " + details.getClass());
        }
    }

    /**
     * Transforms the specified REST object to a domain object.
     *
     * @param ds the REST object
     * @return the domain object
     * @throws IllegalArgumentException if the REST object cannot be converted
     */
    public com.thinkbiganalytics.metadata.api.datasource.Datasource toDomain(@Nonnull final Datasource ds) {
        if (ds instanceof UserDatasource) {
            final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain;
            final boolean isNew = (ds.getId() == null);

            if (isNew) {
                domain = datasourceProvider.ensureDatasource(ds.getName(), ds.getDescription(), com.thinkbiganalytics.metadata.api.datasource.UserDatasource.class);
                ds.setId(domain.getId().toString());
            } else {
                final com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = datasourceProvider.resolve(ds.getId());
                domain = (com.thinkbiganalytics.metadata.api.datasource.UserDatasource) datasourceProvider.getDatasource(id);
            }

            if (domain == null) {
                throw new IllegalArgumentException("Could not find data source: " + ds.getId());
            }

            if (ds instanceof JdbcDatasource) {
                if (isNew) {
                    datasourceProvider.ensureDatasourceDetails(domain.getId(), JdbcDatasourceDetails.class);
                }
                updateDomain(domain, (JdbcDatasource) ds);
            } else {
                updateDomain(domain, (UserDatasource) ds);
            }

            return domain;
        } else {
            throw new IllegalArgumentException("Not a supported user datasource class: " + ds.getClass());
        }
    }

    /**
     * Updates the specified REST object with properties from the specified domain object.
     *
     * @param ds     the REST object
     * @param domain the domain object
     * @param level  the level of detail
     */
    private void updateDatasource(@Nonnull final Datasource ds, @Nonnull final com.thinkbiganalytics.metadata.api.datasource.Datasource domain, @Nonnull final Level level) {
        ds.setId(domain.getId().toString());
        ds.setDescription(domain.getDescription());
        ds.setName(domain.getName());

        // Add connections if level matches
        if (level.compareTo(Level.CONNECTIONS) <= 0) {
            for (com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc : domain.getFeedSources()) {
                Feed feed = new Feed();
                feed.setDisplayName(domainSrc.getFeed().getDisplayName());
                feed.setId(domainSrc.getFeed().getId().toString());
                feed.setState(Feed.State.valueOf(domainSrc.getFeed().getState().name()));
                feed.setSystemName(domainSrc.getFeed().getName());
                feed.setModifiedTime(domainSrc.getFeed().getModifiedTime());

                ds.getSourceForFeeds().add(feed);
            }
            for (com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest : domain.getFeedDestinations()) {
                Feed feed = new Feed();
                feed.setDisplayName(domainDest.getFeed().getDisplayName());
                feed.setId(domainDest.getFeed().getId().toString());
                feed.setState(Feed.State.valueOf(domainDest.getFeed().getState().name()));
                feed.setSystemName(domainDest.getFeed().getName());
                feed.setModifiedTime(domainDest.getFeed().getModifiedTime());

                ds.getDestinationForFeeds().add(feed);
            }
        }
    }

    /**
     * Updates the specified REST object with properties from the specified domain object.
     *
     * @param ds     the REST object
     * @param domain the domain object
     * @param level  the level of detail
     */
    public void updateDatasource(@Nonnull final DerivedDatasource ds, @Nonnull final com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource domain, @Nonnull final Level level) {
        updateDatasource((Datasource) ds, domain, level);
        ds.setProperties(domain.getProperties());
        ds.setDatasourceType(domain.getDatasourceType());
    }

    /**
     * Updates the specified REST object with properties from the specified domain object.
     *
     * @param ds     the REST object
     * @param domain the domain object
     * @param level  the level of detail
     */
    private void updateDatasource(@Nonnull final JdbcDatasource ds, @Nonnull final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain, @Nonnull final Level level) {
        updateDatasource((UserDatasource) ds, domain, level);
        domain.getDetails()
            .map(JdbcDatasourceDetails.class::cast)
            .ifPresent(details -> {
                details.getControllerServiceId().ifPresent(ds::setControllerServiceId);
                if (level.compareTo(Level.ADMIN) <= 0) {
                    ds.setPassword(encryptor.decrypt(details.getPassword()));
                }
                if (level.compareTo(Level.FULL) <= 0) {
                    // Fetch database properties from NiFi
                    details.getControllerServiceId()
                        .flatMap(id -> nifiRestClient.controllerServices().findById(id))
                        .ifPresent(controllerService -> {
                            ds.setDatabaseConnectionUrl(controllerService.getProperties().get(DatasourceConstants.DATABASE_CONNECTION_URL));
                            ds.setDatabaseDriverClassName(controllerService.getProperties().get(DatasourceConstants.DATABASE_DRIVER_CLASS_NAME));
                            ds.setDatabaseDriverLocation(controllerService.getProperties().get(DatasourceConstants.DATABASE_DRIVER_LOCATION));
                            ds.setDatabaseUser(controllerService.getProperties().get(DatasourceConstants.DATABASE_USER));
                        });
                }
            });
    }

    /**
     * Updates the specified REST object with properties from the specified domain object.
     *
     * @param ds     the REST object
     * @param domain the domain object
     * @param level  the level of detail
     */
    private void updateDatasource(@Nonnull final UserDatasource ds, @Nonnull final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain, @Nonnull final Level level) {
        updateDatasource((Datasource) ds, domain, level);
        ds.setType(domain.getType());
    }

    /**
     * Updates the specified domain object with properties from the specified REST object.
     *
     * @param domain the domain object
     * @param ds     the REST object
     */
    private void updateDomain(@Nonnull final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain, @Nonnull final JdbcDatasource ds) {
        updateDomain(domain, (UserDatasource) ds);
        domain.getDetails()
            .map(JdbcDatasourceDetails.class::cast)
            .ifPresent(details -> {
                // Look for changed properties
                final Map<String, String> properties = new HashMap<>();
                if (StringUtils.isNotBlank(ds.getDatabaseConnectionUrl())) {
                    properties.put(DatasourceConstants.DATABASE_CONNECTION_URL, ds.getDatabaseConnectionUrl());
                }
                if (StringUtils.isNotBlank(ds.getDatabaseDriverClassName())) {
                    properties.put(DatasourceConstants.DATABASE_DRIVER_CLASS_NAME, ds.getDatabaseDriverClassName());
                }
                if (ds.getDatabaseDriverLocation() != null) {
                    properties.put(DatasourceConstants.DATABASE_DRIVER_LOCATION, ds.getDatabaseDriverLocation());
                }
                if (ds.getDatabaseUser() != null) {
                    properties.put(DatasourceConstants.DATABASE_USER, ds.getDatabaseUser());
                }
                if (ds.getPassword() != null) {
                    details.setPassword(encryptor.encrypt(ds.getPassword()));
                    properties.put(DatasourceConstants.PASSWORD, StringUtils.isNotEmpty(ds.getPassword()) ? ds.getPassword() : null);
                }

                // Update or create the controller service
                ControllerServiceDTO controllerService = null;

                if (details.getControllerServiceId().isPresent()) {
                    controllerService = new ControllerServiceDTO();
                    controllerService.setId(details.getControllerServiceId().get());
                    controllerService.setName(ds.getName());
                    controllerService.setComments(ds.getDescription());
                    controllerService.setProperties(properties);
                    try {
                       controllerService = nifiRestClient.controllerServices().updateServiceAndReferencingComponents(controllerService);
                        ds.setControllerServiceId(controllerService.getId());
                    } catch (final NifiComponentNotFoundException e) {
                        log.warn("Controller service is missing for datasource: {}", domain.getId(), e);
                        controllerService = null;
                    }
                }
                if (controllerService == null) {
                    controllerService = new ControllerServiceDTO();
                    controllerService.setType("org.apache.nifi.dbcp.DBCPConnectionPool");
                    controllerService.setName(ds.getName());
                    controllerService.setComments(ds.getDescription());
                    controllerService.setProperties(properties);
                    final ControllerServiceDTO newControllerService = nifiRestClient.controllerServices().create(controllerService);
                    try {
                        nifiRestClient.controllerServices().updateStateById(newControllerService.getId(), NiFiControllerServicesRestClient.State.ENABLED);
                    } catch (final NifiClientRuntimeException nifiException) {
                        log.error("Failed to enable controller service for datasource: {}", domain.getId(), nifiException);
                        nifiRestClient.controllerServices().disableAndDeleteAsync(newControllerService.getId());
                        throw nifiException;
                    }
                    details.setControllerServiceId(newControllerService.getId());
                    ds.setControllerServiceId(newControllerService.getId());
                }
            });
    }

    /**
     * Updates the specified domain object with properties from the specified REST object.
     *
     * @param domain the domain object
     * @param ds     the REST object
     */
    private void updateDomain(@Nonnull final com.thinkbiganalytics.metadata.api.datasource.UserDatasource domain, @Nonnull final UserDatasource ds) {
        // Update properties
        domain.setDescription(ds.getDescription());
        domain.setName(ds.getName());
        domain.setType(ds.getType());

        // Update access control
        if (domain.getAllowedActions().hasPermission(DatasourceAccessControl.CHANGE_PERMS)) {
            ds.toRoleMembershipChangeList().forEach(roleMembershipChange -> securityService.changeDatasourceRoleMemberships(ds.getId(), roleMembershipChange));
        }
    }
}
