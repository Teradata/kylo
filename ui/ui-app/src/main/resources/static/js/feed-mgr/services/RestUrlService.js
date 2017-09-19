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

define(['angular', 'feed-mgr/module-name'], function (angular, moduleName) {
    return angular.module(moduleName).service('RestUrlService', function () {

        var self = this;

        this.ROOT = "";
        this.ADMIN_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/admin";
        this.ADMIN_V2_BASE_URL = this.ROOT + "/proxy/v2/feedmgr/admin";
        this.SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
        this.TEMPLATES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/templates";
        this.FEEDS_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/feeds";
        this.SLA_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/sla";
        this.CONTROLLER_SERVICES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/controller-services";
        this.SCHEMA_DISCOVERY_BASE_URL = this.ROOT + "/proxy/v1/schema-discovery";
        this.GET_TEMPLATES_URL = self.TEMPLATES_BASE_URL;
        this.GET_UNREGISTERED_TEMPLATES_URL = self.TEMPLATES_BASE_URL + "/unregistered";
        this.HADOOP_AUTHORIZATATION_BASE_URL = self.ROOT + "/proxy/v1/feedmgr/hadoop-authorization";
        this.UI_BASE_URL = this.ROOT + "/api/v1/ui";
        this.DOMAIN_TYPES_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/domain-types";

        this.UPLOAD_SAMPLE_TABLE_FILE = this.SCHEMA_DISCOVERY_BASE_URL + "/hive/sample-file";
        this.LIST_FILE_PARSERS = this.SCHEMA_DISCOVERY_BASE_URL + "/file-parsers";

        this.VALIDATE_CRON_EXPRESSION_URL = this.ROOT + "/proxy/v1/feedmgr/util/cron-expression/validate";

        this.PREVIEW_CRON_EXPRESSION_URL = this.ROOT + "/proxy/v1/feedmgr/util/cron-expression/preview";

        this.GET_SYSTEM_NAME = this.ROOT + "/proxy/v1/feedmgr/util/system-name";

        this.ICONS_URL = this.ROOT + "/proxy/v1/feedmgr/util/icons";
        this.ICON_COLORS_URL = this.ROOT + "/proxy/v1/feedmgr/util/icon-colors";

        this.CODE_MIRROR_TYPES_URL = this.ROOT + "/proxy/v1/feedmgr/util/codemirror-types";

        this.CATEGORIES_URL = this.ROOT + "/proxy/v1/feedmgr/categories";

        this.SEARCH_URL = this.ROOT + "/proxy/v1/feedmgr/search";

        this.HIVE_SERVICE_URL = this.ROOT + "/proxy/v1/hive";

        this.SPARK_SHELL_SERVICE_URL = this.ROOT + "/proxy/v1/spark/shell";

        ///TEMPLATE REGISTRATION

        this.REGISTER_TEMPLATE_URL = function () {
            return self.TEMPLATES_BASE_URL + "/register";
        }

        this.SAVE_TEMPLATE_ORDER_URL = self.TEMPLATES_BASE_URL + "/order";

        this.GET_REGISTERED_TEMPLATES_URL = self.TEMPLATES_BASE_URL + "/registered";

        this.GET_REGISTERED_TEMPLATE_PROPERTIES_URL = function (templateId) {
            return self.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/properties";
        }

        this.GET_REGISTERED_TEMPLATE_URL = function (templateId) {
            return self.GET_REGISTERED_TEMPLATES_URL + "/" + templateId;
        }

        this.REGISTERED_TEMPLATE_NIFI_INPUT_PORTS = function (nifiTemplateId) {
            return self.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/input-ports";
        }

        this.REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS = function (nifiTemplateId) {
            return self.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/output-ports";
        }

        this.REGISTERED_TEMPLATE_NIFI_ALL_PORTS = function (nifiTemplateId) {
            return self.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/ports";
        }

        this.TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS = function (nifiTemplateId) {
            return self.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/datasource-definitions";
        }

        this.TEMPLATE_FLOW_INFORMATION = function (nifiTemplateId) {
            return self.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/flow-info";
        }

        this.DISABLE_REGISTERED_TEMPLATE_URL = function (templateId) {
            return self.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/disable";
        }
        this.ENABLE_REGISTERED_TEMPLATE_URL = function (templateId) {
            return self.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/enable";
        }
        this.DELETE_REGISTERED_TEMPLATE_URL = function (templateId) {
            return self.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/delete";
        }

        this.ALL_REUSABLE_FEED_INPUT_PORTS = this.ROOT + "/proxy/v1/feedmgr/nifi/reusable-input-ports";

        this.CONFIGURATION_PROPERTIES_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/configuration/properties";
        this.METADATA_PROPERTY_NAMES_URL = this.ROOT + "/proxy/v1/feedmgr/metadata-properties";

        this.GET_DATASOURCE_TYPES = this.ROOT + "/proxy/v1/metadata/datasource/types";

        //FEED URLS

        this.CREATE_FEED_FROM_TEMPLATE_URL = self.FEEDS_BASE_URL;

        this.MERGE_FEED_WITH_TEMPLATE = function (feedId) {
            return self.GET_FEEDS_URL + "/" + feedId + "/merge-template";
        }

        this.GET_FEEDS_URL = self.FEEDS_BASE_URL;

        this.GET_FEED_NAMES_URL = self.FEEDS_BASE_URL + "/names";

        this.GET_POSSIBLE_FEED_PRECONDITIONS_URL = self.FEEDS_BASE_URL + "/possible-preconditions";

        this.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL = self.SLA_BASE_URL + "/available-metrics";

        this.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL = self.SLA_BASE_URL + "/available-responders";

        this.VALIDATE_SLA_ACTION_URL = self.SLA_BASE_URL + "/action/validate";

        this.SAVE_FEED_SLA_URL = function (feedId) {
            return self.SLA_BASE_URL + "/feed/" + feedId;
        }
        this.SAVE_SLA_URL = self.SLA_BASE_URL;

        this.DELETE_SLA_URL = function (slaId) {
            return self.SLA_BASE_URL + "/" + slaId;
        }

        this.GET_FEED_SLA_URL = function (feedId) {
            return self.FEEDS_BASE_URL + "/" + feedId + "/sla";
        }

        this.GET_SLA_BY_ID_URL = function (slaId) {
            return self.SLA_BASE_URL + "/"+slaId;
        }

        this.GET_SLA_AS_EDIT_FORM = function (slaId) {
            return self.SLA_BASE_URL + "/" + slaId + "/form-object";
        }

        this.GET_SLAS_URL = self.SLA_BASE_URL;

        this.GET_CONTROLLER_SERVICES_TYPES_URL = self.CONTROLLER_SERVICES_BASE_URL + "/types";

        this.GET_CONTROLLER_SERVICES_URL = self.CONTROLLER_SERVICES_BASE_URL;

        this.GET_CONTROLLER_SERVICE_URL = function (serviceId) {
            return self.CONTROLLER_SERVICES_BASE_URL + "/" + serviceId;
        }

        this.FEED_PROFILE_STATS_URL = function (feedId) {
            return self.GET_FEEDS_URL + "/" + feedId + "/profile-stats";
        }

        this.FEED_PROFILE_SUMMARY_URL = function (feedId) {
            return self.GET_FEEDS_URL + "/" + feedId + "/profile-summary";
        }

        this.FEED_PROFILE_VALID_RESULTS_URL = function (feedId, processingDttm) {
            return self.GET_FEEDS_URL + "/" + feedId + "/profile-valid-results";
        }

        this.FEED_PROFILE_INVALID_RESULTS_URL = function (feedId, processingDttm) {
            return self.GET_FEEDS_URL + "/" + feedId + "/profile-invalid-results";
        }

        this.ENABLE_FEED_URL = function (feedId) {
            return self.FEEDS_BASE_URL + "/enable/" + feedId;
        }
        this.DISABLE_FEED_URL = function (feedId) {
            return self.FEEDS_BASE_URL + "/disable/" + feedId;
        }
        this.UPLOAD_FILE_FEED_URL = function (feedId) {
            return self.FEEDS_BASE_URL + "/" + feedId + "/upload-file";
        }

        this.FEED_DETAILS_BY_NAME_URL = function (feedName) {
            return self.FEEDS_BASE_URL + "/by-name/" + feedName;
        };

        this.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL = function (categoryName) {
            return self.CATEGORIES_URL + "/by-name/" + categoryName;
        };

        this.CATEGORY_DETAILS_BY_ID_URL = function (categoryId) {
            return self.CATEGORIES_URL + "/by-id/" + categoryId;
        };

        /**
         * Gets the URL for retrieving the user fields for a new feed.
         *
         * @param {string} categoryId the category id
         * @returns {string} the URL
         */
        this.GET_FEED_USER_FIELDS_URL = function (categoryId) {
            return self.CATEGORIES_URL + "/" + categoryId + "/user-fields";
        };

        /**
         * URL for retrieving the user fields for a new category.
         * @type {string}
         */
        this.GET_CATEGORY_USER_FIELD_URL = self.CATEGORIES_URL + "/user-fields";

        // Endpoint for administration of user fields
        this.ADMIN_USER_FIELDS = self.ADMIN_BASE_URL + "/user-fields";

        //Field Policy Urls

        this.AVAILABLE_STANDARDIZATION_POLICIES = this.ROOT + "/proxy/v1/field-policies/standardization";
        this.AVAILABLE_VALIDATION_POLICIES = this.ROOT + "/proxy/v1/field-policies/validation";

        this.ADMIN_IMPORT_TEMPLATE_URL = self.ADMIN_V2_BASE_URL + "/import-template";

        this.ADMIN_EXPORT_TEMPLATE_URL = self.ADMIN_BASE_URL + "/export-template";

        this.ADMIN_EXPORT_FEED_URL = self.ADMIN_BASE_URL + "/export-feed";

        this.ADMIN_IMPORT_FEED_URL = self.ADMIN_V2_BASE_URL + "/import-feed";

        this.ADMIN_UPLOAD_STATUS_CHECK = function (key) {
            return self.ADMIN_BASE_URL + "/upload-status/" + key;
        };

        // Hadoop Security Authorization
        this.HADOOP_SECURITY_GROUPS = self.HADOOP_AUTHORIZATATION_BASE_URL + "/groups";

        // Security service URLs

        this.SECURITY_GROUPS_URL = self.SECURITY_BASE_URL + "/groups";

        this.SECURITY_USERS_URL = self.SECURITY_BASE_URL + "/users";

        this.FEED_LINEAGE_URL = function (feedId) {
            return self.ROOT + "/proxy/v1/metadata/feed/" + feedId + "/lineage";
        };

        /**
         * Generates a URL for listing the controller services under the specified process group.
         *
         * @param {string} processGroupId the process group id
         * @returns {string} the URL for listing controller services
         */
        this.LIST_SERVICES_URL = function (processGroupId) {
            return self.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/process-group/" + processGroupId;
        };

        /**
         * The endpoint for retrieving the list of available Hive partition functions.
         *
         * @type {string}
         */
        this.PARTITION_FUNCTIONS_URL = this.ROOT + "/proxy/v1/feedmgr/util/partition-functions";

        /**
         * The endpoint for retrieving the NiFi status.
         * @type {string}
         */
        this.NIFI_STATUS = this.ROOT + "/proxy/v1/feedmgr/nifi/status";

        /**
         * the endpoint for determining if NiFi is up or not
         * @type {string}
         */
        this.IS_NIFI_RUNNING_URL = this.ROOT + "/proxy/v1/feedmgr/nifi/running";

        /**
         * The endpoint for retrieving data sources.
         * @type {string}
         */
        this.GET_DATASOURCES_URL = this.ROOT + "/proxy/v1/metadata/datasource";

        this.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL = function(id){
            return self.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/"+id+"/references";
        }

        /**
         * Get/Post roles changes for a Feed entity
         * @param feedId the feed id
         * @returns {string} the url to get/post feed role changes
         */
        this.FEED_ROLES_URL = function (feedId) {
            return self.FEEDS_BASE_URL + "/" + feedId + "/roles"
        };

        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        this.CATEGORY_ROLES_URL = function (categoryId) {
            return self.CATEGORIES_URL + "/" + categoryId + "/roles"
        };
        
        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        this.CATEGORY_FEED_ROLES_URL = function (categoryId) {
        	return self.CATEGORIES_URL + "/" + categoryId + "/feed-roles"
        };

        /**
         * Get/Post roles changes for a Template entity
         * @param templateId the Template id
         * @returns {string} the url to get/post Template role changes
         */
        this.TEMPLATE_ROLES_URL = function (templateId) {
            return self.TEMPLATES_BASE_URL + "/registered/" + templateId + "/roles"
        };

        /**
         * Endpoint for roles changes to a Datasource entity.
         * @param {string} datasourceId the datasource id
         * @returns {string} the url for datasource role changes
         */
        this.DATASOURCE_ROLES_URL = function (datasourceId) {
            return self.GET_DATASOURCES_URL + "/" + datasourceId + "/roles";
        };

        /**
         * The URL for retrieving the list of template table option plugins.
         * @type {string}
         */
        this.UI_TEMPLATE_TABLE_OPTIONS = this.UI_BASE_URL + "/template-table-options";


        /**
         * The URL for retrieving the list of templates for custom rendering with nifi processors
         * @type {string}
         */
        this.UI_PROCESSOR_TEMPLATES = this.UI_BASE_URL + "/processor-templates";

        /**
         * return a list of the categorySystemName.feedSystemName
         * @type {string}
         */
        this.OPS_MANAGER_FEED_NAMES = "/proxy/v1/feeds/names";

    });
});
