/*-
 * #%L
 * thinkbig-ui-feed-manager
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

 
import * as angular from 'angular';
import * as _ from "underscore";
import { Injectable } from '@angular/core';
const moduleName = require('feed-mgr/module-name');

import "../module";

@Injectable()
export class RestUrlService {
    
        ROOT = "";
        ADMIN_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/admin";
        ADMIN_V2_BASE_URL = this.ROOT + "/proxy/v2/feedmgr/admin";
        SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
        TEMPLATES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/templates";
        FEEDS_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/feeds";
        SLA_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/sla";
        CONTROLLER_SERVICES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services";
        SCHEMA_DISCOVERY_BASE_URL = this.ROOT + "/proxy/v1/schema-discovery";
        GET_TEMPLATES_URL = this.TEMPLATES_BASE_URL;
        GET_UNREGISTERED_TEMPLATES_URL = this.TEMPLATES_BASE_URL + "/unregistered";
        HADOOP_AUTHORIZATATION_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/hadoop-authorization";
        UI_BASE_URL = this.ROOT + "/api/v1/ui";
        DOMAIN_TYPES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/domain-types";

        UPLOAD_SAMPLE_TABLE_FILE = this.SCHEMA_DISCOVERY_BASE_URL + "/hive/sample-file";
        UPLOAD_SPARK_SAMPLE_FILE = this.SCHEMA_DISCOVERY_BASE_URL + "/spark/sample-file";
        LIST_FILE_PARSERS = this.SCHEMA_DISCOVERY_BASE_URL + "/file-parsers";
        LIST_SPARK_FILE_PARSERS = this.SCHEMA_DISCOVERY_BASE_URL + "/spark-file-parsers";

        VALIDATE_CRON_EXPRESSION_URL = this.ROOT + "/proxy/v1/feedmgr/util/cron-expression/validate";

        PREVIEW_CRON_EXPRESSION_URL = this.ROOT + "/proxy/v1/feedmgr/util/cron-expression/preview";

        GET_SYSTEM_NAME = this.ROOT + "/proxy/v1/feedmgr/util/system-name";

        ICONS_URL = this.ROOT + "/proxy/v1/feedmgr/util/icons";
        ICON_COLORS_URL = this.ROOT + "/proxy/v1/feedmgr/util/icon-colors";

        CODE_MIRROR_TYPES_URL = this.ROOT + "/proxy/v1/feedmgr/util/codemirror-types";

        CATEGORIES_URL = this.ROOT + "/proxy/v1/feedmgr/categories";

        SEARCH_URL = this.ROOT + "/proxy/v1/feedmgr/search";

        HIVE_SERVICE_URL = this.ROOT + "/proxy/v1/hive";

        SPARK_SHELL_SERVICE_URL = this.ROOT + "/proxy/v1/spark/shell";

        ///TEMPLATE REGISTRATION

        REGISTER_TEMPLATE_URL = () => {
            return this.TEMPLATES_BASE_URL + "/register";
        }

        SAVE_TEMPLATE_ORDER_URL = this.TEMPLATES_BASE_URL + "/order";

        GET_REGISTERED_TEMPLATES_URL = this.TEMPLATES_BASE_URL + "/registered";

        GET_REGISTERED_TEMPLATE_PROPERTIES_URL = (templateId:any) => {
            return this.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/properties";
        }

        GET_REGISTERED_TEMPLATE_URL = (templateId:any) => {
            return this.GET_REGISTERED_TEMPLATES_URL + "/" + templateId;
        }

        REGISTERED_TEMPLATE_NIFI_INPUT_PORTS = (nifiTemplateId:any) => {
            return this.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/input-ports";
        }

        REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS = (nifiTemplateId:any) => {
            return this.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/output-ports";
        }

        REGISTERED_TEMPLATE_NIFI_ALL_PORTS = (nifiTemplateId:any) => {
            return this.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/ports";
        }

        TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS = (nifiTemplateId:any) => {
            return this.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/datasource-definitions";
        }

        TEMPLATE_FLOW_INFORMATION = (nifiTemplateId:any) => {
            return this.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/flow-info";
        }

        DISABLE_REGISTERED_TEMPLATE_URL = (templateId:any) => {
            return this.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/disable";
        }
        ENABLE_REGISTERED_TEMPLATE_URL = (templateId:any)=>  {
            return this.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/enable";
        }
        DELETE_REGISTERED_TEMPLATE_URL = (templateId:any) => {
            return this.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/delete";
        }

        REMOTE_PROCESS_GROUP_AWARE = this.TEMPLATES_BASE_URL+"/remote-process-group/status";

        ALL_REUSABLE_FEED_INPUT_PORTS = this.ROOT + "/proxy/v1/feedmgr/nifi/reusable-input-ports";


        ROOT_INPUT_PORTS = this.ROOT + "/proxy/v1/feedmgr/nifi/root-input-ports";

        CONFIGURATION_PROPERTIES_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/configuration/properties";
        METADATA_PROPERTY_NAMES_URL = this.ROOT + "/proxy/v1/feedmgr/metadata-properties";

        GET_DATASOURCE_TYPES = this.ROOT + "/proxy/v1/metadata/datasource/types";

        //FEED URLS

        CREATE_FEED_FROM_TEMPLATE_URL = this.FEEDS_BASE_URL;

        MERGE_FEED_WITH_TEMPLATE = function (feedId:any) {
            return this.GET_FEEDS_URL + "/" + feedId + "/merge-template";
        }

        GET_FEEDS_URL = this.FEEDS_BASE_URL;

        GET_FEED_NAMES_URL = this.FEEDS_BASE_URL + "/names";

        GET_POSSIBLE_FEED_PRECONDITIONS_URL = this.FEEDS_BASE_URL + "/possible-preconditions";

        GET_POSSIBLE_SLA_METRIC_OPTIONS_URL = this.SLA_BASE_URL + "/available-metrics";

        GET_POSSIBLE_SLA_ACTION_OPTIONS_URL = this.SLA_BASE_URL + "/available-responders";

        VALIDATE_SLA_ACTION_URL = this.SLA_BASE_URL + "/action/validate";

        SAVE_FEED_SLA_URL = function (feedId:any) {
            return this.SLA_BASE_URL + "/feed/" + feedId;
        }
        SAVE_SLA_URL = this.SLA_BASE_URL;

        DELETE_SLA_URL = function (slaId:any) {
            return this.SLA_BASE_URL + "/" + slaId;
        }

        GET_FEED_SLA_URL = function (feedId:any) {
            return this.FEEDS_BASE_URL + "/" + feedId + "/sla";
        }

        GET_SLA_BY_ID_URL = function (slaId:any) {
            return this.SLA_BASE_URL + "/" + slaId;
        }

        GET_SLA_AS_EDIT_FORM = function (slaId:any) {
            return this.SLA_BASE_URL + "/" + slaId + "/form-object";
        }

        GET_SLAS_URL = this.SLA_BASE_URL;

        GET_CONTROLLER_SERVICES_TYPES_URL = this.CONTROLLER_SERVICES_BASE_URL + "/types";

        GET_CONTROLLER_SERVICES_URL = this.CONTROLLER_SERVICES_BASE_URL;

        GET_CONTROLLER_SERVICE_URL = (serviceId:any) => {
            return this.CONTROLLER_SERVICES_BASE_URL + "/" + serviceId;
        }

        CONTROLLER_SERVICES_PREVIEW_QUERY_URL = (serviceId:any, schema:string, table:string, limit:number) => {
            return this.CONTROLLER_SERVICES_BASE_URL + "/" + serviceId;
        }
        
        FEED_VERSIONS_URL = (feedId:any) => {
        		return this.GET_FEEDS_URL + "/" + feedId + "/versions";
        }
        
        FEED_VERSION_ID_URL = (feedId:any, verId:any) => {
        	return this.FEED_VERSIONS_URL(feedId) + "/" + verId;
        }
        
        FEED_VERSIONS_DIFF_URL = (feedId:any, verId1:any, verId2:any) => {
        		return this.FEED_VERSION_ID_URL(feedId, verId1) + "/diff/" + verId2;
        }

        FEED_PROFILE_STATS_URL = (feedId:any) => {
            return this.GET_FEEDS_URL + "/" + feedId + "/profile-stats";
        }

        FEED_PROFILE_SUMMARY_URL = (feedId:any) => {
            return this.GET_FEEDS_URL + "/" + feedId + "/profile-summary";
        }

        FEED_PROFILE_VALID_RESULTS_URL = (feedId:any, processingDttm:any) => {
            return this.GET_FEEDS_URL + "/" + feedId + "/profile-valid-results";
        }

        FEED_PROFILE_INVALID_RESULTS_URL = (feedId:any, processingDttm:any) => {
            return this.GET_FEEDS_URL + "/" + feedId + "/profile-invalid-results";
        }

        ENABLE_FEED_URL = (feedId:any) => {
            return this.FEEDS_BASE_URL + "/enable/" + feedId;
        }
        DISABLE_FEED_URL = (feedId:any) => {
            return this.FEEDS_BASE_URL + "/disable/" + feedId;
        }

        START_FEED_URL = (feedId:any) => {
            return this.FEEDS_BASE_URL + "/start/" + feedId;
        }

        UPLOAD_FILE_FEED_URL = (feedId:any) => {
            return this.FEEDS_BASE_URL + "/" + feedId + "/upload-file";
        }

        FEED_DETAILS_BY_NAME_URL = (feedName:any) => {
            return this.FEEDS_BASE_URL + "/by-name/" + feedName;
        };

        CATEGORY_DETAILS_BY_SYSTEM_NAME_URL = (categoryName:any) => {
            return this.CATEGORIES_URL + "/by-name/" + categoryName;
        };

        CATEGORY_DETAILS_BY_ID_URL = (categoryId:any) => {
            return this.CATEGORIES_URL + "/by-id/" + categoryId;
        };

        /**
         * Gets the URL for retrieving the user fields for a new feed.
         *
         * @param {string} categoryId the category id
         * @returns {string} the URL
         */
        GET_FEED_USER_FIELDS_URL = (categoryId:any) => {
            return this.CATEGORIES_URL + "/" + categoryId + "/user-fields";
        };

        /**
         * URL for retrieving the user fields for a new category.
         * @type {string}
         */
        GET_CATEGORY_USER_FIELD_URL = this.CATEGORIES_URL + "/user-fields";

        // Endpoint for administration of user fields
        ADMIN_USER_FIELDS = this.ADMIN_BASE_URL + "/user-fields";

        //Field Policy Urls

        AVAILABLE_STANDARDIZATION_POLICIES = this.ROOT + "/proxy/v1/field-policies/standardization";
        AVAILABLE_VALIDATION_POLICIES = this.ROOT + "/proxy/v1/field-policies/validation";

        ADMIN_IMPORT_TEMPLATE_URL = this.ADMIN_V2_BASE_URL + "/import-template";

        ADMIN_EXPORT_TEMPLATE_URL = this.ADMIN_BASE_URL + "/export-template";

        ADMIN_EXPORT_FEED_URL = this.ADMIN_BASE_URL + "/export-feed";

        ADMIN_IMPORT_FEED_URL = this.ADMIN_V2_BASE_URL + "/import-feed";

        ADMIN_UPLOAD_STATUS_CHECK = function (key:any) {
            return this.ADMIN_BASE_URL + "/upload-status/" + key;
        };

        // Hadoop Security Authorization
        HADOOP_SECURITY_GROUPS = this.HADOOP_AUTHORIZATATION_BASE_URL + "/groups";

        // Security service URLs

        SECURITY_GROUPS_URL = this.SECURITY_BASE_URL + "/groups";

        SECURITY_USERS_URL = this.SECURITY_BASE_URL + "/users";

        FEED_LINEAGE_URL = function (feedId:any) {
            return this.ROOT + "/proxy/v1/metadata/feed/" + feedId + "/lineage";
        };

        // Feed history data reindexing endpoint
        FEED_HISTORY_CONFIGURED = this.ROOT + "/proxy/v1/metadata/feed/data-history-reindex-configured";


        /**
         * Generates a URL for listing the controller services under the specified process group.
         *
         * @param {string} processGroupId the process group id
         * @returns {string} the URL for listing controller services
         */
        LIST_SERVICES_URL = (processGroupId:any) => {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/process-group/" + processGroupId;
        };

        /**
         * The endpoint for retrieving the list of available Hive partition functions.
         *
         * @type {string}
         */
        PARTITION_FUNCTIONS_URL = this.ROOT + "/proxy/v1/feedmgr/util/partition-functions";

        /**
         * The endpoint for retrieving the NiFi status.
         * @type {string}
         */
        NIFI_STATUS = this.ROOT + "/proxy/v1/feedmgr/nifi/status";

        /**
         * the endpoint for determining if NiFi is up or not
         * @type {string}
         */
        IS_NIFI_RUNNING_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/running";

        /**
         * The endpoint for retrieving data sources.
         * @type {string}
         */
        GET_DATASOURCES_URL = this.ROOT + "/proxy/v1/metadata/datasource";

        /**
         * The endpoint for querying a data source.
         */
        QUERY_DATASOURCE_URL = (id:any) => {
            return this.ROOT + "/proxy/v1/metadata/datasource/" + id + "/query";
        };

        /**
         * The endpoint for querying a data source.
         */
        PREVIEW_DATASOURCE_URL = (id:any, schema:string, table:string, limit:number) => {
            return this.ROOT + "/proxy/v1/metadata/datasource/" + id + "/preview/" + schema + "/" + table + "?limit=" + limit;
        };

        GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL = (id:any) => {
            return this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + id + "/references";
        }

        /**
         * Get/Post roles changes for a Feed entity
         * @param feedId the feed id
         * @returns {string} the url to get/post feed role changes
         */
        FEED_ROLES_URL = (feedId:any) => {
            return this.FEEDS_BASE_URL + "/" + feedId + "/roles"
        };

        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        CATEGORY_ROLES_URL = function (categoryId:any) {
            return this.CATEGORIES_URL + "/" + categoryId + "/roles"
        };

        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        CATEGORY_FEED_ROLES_URL = function (categoryId:any) {
            return this.CATEGORIES_URL + "/" + categoryId + "/feed-roles"
        };

        /**
         * Get/Post roles changes for a Template entity
         * @param templateId the Template id
         * @returns {string} the url to get/post Template role changes
         */
        TEMPLATE_ROLES_URL = function (templateId:any) {
            return this.TEMPLATES_BASE_URL + "/registered/" + templateId + "/roles"
        };

        /**
         * Endpoint for roles changes to a Datasource entity.
         * @param {string} datasourceId the datasource id
         * @returns {string} the url for datasource role changes
         */
        DATASOURCE_ROLES_URL = (datasourceId:any) => {
            return this.GET_DATASOURCES_URL + "/" + datasourceId + "/roles";
        };

        /**
         * The URL for retrieving the list of template table option plugins.
         * @type {string}
         */
        UI_TEMPLATE_TABLE_OPTIONS = this.UI_BASE_URL + "/template-table-options";

        /**
         * The URL for retrieving the list of templates for custom rendering with nifi processors
         * @type {string}
         */
        UI_PROCESSOR_TEMPLATES = this.UI_BASE_URL + "/processor-templates";

        /**
         * return a list of the categorySystemName.feedSystemName
         * @type {string}
         */
        OPS_MANAGER_FEED_NAMES = "/proxy/v1/feeds/names";

        /**
         * Formats a date as a string.
         */
        FORMAT_DATE = "/proxy/v1/feedmgr/util/format-date";

        /**
         * Parses a string as a date.
         */
        PARSE_DATE = "/proxy/v1/feedmgr/util/parse-date";
    }

    angular.module(moduleName).service('RestUrlService',RestUrlService);
