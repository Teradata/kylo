/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use file except in compliance with the License.
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
import {RestUrlConstants} from "./RestUrlConstants"
   export class RestUrlService {


        ROOT = RestUrlConstants.ROOT
        ADMIN_BASE_URL = RestUrlConstants.ADMIN_BASE_URL
        ADMIN_V2_BASE_URL = RestUrlConstants.ADMIN_V2_BASE_URL;
        SECURITY_BASE_URL = RestUrlConstants.SECURITY_BASE_URL
        TEMPLATES_BASE_URL = RestUrlConstants.TEMPLATES_BASE_URL
        FEEDS_BASE_URL = RestUrlConstants.FEEDS_BASE_URL
        SLA_BASE_URL = RestUrlConstants.SLA_BASE_URL
        CONTROLLER_SERVICES_BASE_URL = RestUrlConstants.CONTROLLER_SERVICES_BASE_URL
        SCHEMA_DISCOVERY_BASE_URL = RestUrlConstants.SCHEMA_DISCOVERY_BASE_URL
        GET_TEMPLATES_URL = RestUrlConstants.GET_TEMPLATES_URL
        GET_UNREGISTERED_TEMPLATES_URL = RestUrlConstants.GET_UNREGISTERED_TEMPLATES_URL
        HADOOP_AUTHORIZATATION_BASE_URL = RestUrlConstants.HADOOP_AUTHORIZATATION_BASE_URL
        UI_BASE_URL = RestUrlConstants.UI_BASE_URL
        DOMAIN_TYPES_BASE_URL = RestUrlConstants.DOMAIN_TYPES_BASE_URL

        UPLOAD_SAMPLE_TABLE_FILE = RestUrlConstants.UPLOAD_SAMPLE_TABLE_FILE
        UPLOAD_SPARK_SAMPLE_FILE = RestUrlConstants.UPLOAD_SPARK_SAMPLE_FILE
        LIST_FILE_PARSERS = RestUrlConstants.LIST_FILE_PARSERS
        LIST_SPARK_FILE_PARSERS = RestUrlConstants.LIST_SPARK_FILE_PARSERS

        VALIDATE_CRON_EXPRESSION_URL = RestUrlConstants.VALIDATE_CRON_EXPRESSION_URL

        PREVIEW_CRON_EXPRESSION_URL = RestUrlConstants.PREVIEW_CRON_EXPRESSION_URL

        GET_SYSTEM_NAME = RestUrlConstants.GET_SYSTEM_NAME

        ICONS_URL = RestUrlConstants.ICONS_URL
        ICON_COLORS_URL = RestUrlConstants.ICON_COLORS_URL

        CODE_MIRROR_TYPES_URL = RestUrlConstants.CODE_MIRROR_TYPES_URL

        CATEGORIES_URL = RestUrlConstants.CATEGORIES_URL

        SEARCH_URL = RestUrlConstants.SEARCH_URL

        HIVE_SERVICE_URL = RestUrlConstants.HIVE_SERVICE_URL

        SPARK_SHELL_SERVICE_URL = RestUrlConstants.SPARK_SHELL_SERVICE_URL

        ///TEMPLATE REGISTRATION

        REGISTER_TEMPLATE_URL = RestUrlConstants.REGISTER_TEMPLATE_URL;

        SAVE_TEMPLATE_ORDER_URL = RestUrlConstants.SAVE_TEMPLATE_ORDER_URL;

        GET_REGISTERED_TEMPLATES_URL = RestUrlConstants.GET_REGISTERED_TEMPLATES_URL

        GET_REGISTERED_TEMPLATE_PROPERTIES_URL = RestUrlConstants.GET_REGISTERED_TEMPLATE_PROPERTIES_URL;

        GET_REGISTERED_TEMPLATE_URL = RestUrlConstants.GET_REGISTERED_TEMPLATE_URL;

        REGISTERED_TEMPLATE_NIFI_INPUT_PORTS = RestUrlConstants.REGISTERED_TEMPLATE_NIFI_INPUT_PORTS;

        REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS = RestUrlConstants.REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS;

        REGISTERED_TEMPLATE_NIFI_ALL_PORTS = RestUrlConstants.REGISTERED_TEMPLATE_NIFI_ALL_PORTS;

        TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS = RestUrlConstants.TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS;

        TEMPLATE_FLOW_INFORMATION = RestUrlConstants.TEMPLATE_FLOW_INFORMATION;

        DISABLE_REGISTERED_TEMPLATE_URL = RestUrlConstants.DISABLE_REGISTERED_TEMPLATE_URL;

        ENABLE_REGISTERED_TEMPLATE_URL = RestUrlConstants.ENABLE_REGISTERED_TEMPLATE_URL;

        DELETE_REGISTERED_TEMPLATE_URL = RestUrlConstants.DELETE_REGISTERED_TEMPLATE_URL;

        REMOTE_PROCESS_GROUP_AWARE = RestUrlConstants.REMOTE_PROCESS_GROUP_AWARE

        ALL_REUSABLE_FEED_INPUT_PORTS = RestUrlConstants.ALL_REUSABLE_FEED_INPUT_PORTS


        ROOT_INPUT_PORTS = RestUrlConstants.ROOT_INPUT_PORTS

        CONFIGURATION_PROPERTIES_URL = RestUrlConstants.CONFIGURATION_PROPERTIES_URL
        METADATA_PROPERTY_NAMES_URL = RestUrlConstants.METADATA_PROPERTY_NAMES_URL

        GET_DATASOURCE_TYPES = RestUrlConstants.GET_DATASOURCE_TYPES

        //FEED URLS

        CREATE_FEED_FROM_TEMPLATE_URL = RestUrlConstants.CREATE_FEED_FROM_TEMPLATE_URL

        MERGE_FEED_WITH_TEMPLATE = RestUrlConstants.MERGE_FEED_WITH_TEMPLATE

        GET_FEEDS_URL = RestUrlConstants.GET_FEEDS_URL

        GET_FEED_NAMES_URL = RestUrlConstants.GET_FEED_NAMES_URL

        GET_POSSIBLE_FEED_PRECONDITIONS_URL = RestUrlConstants.GET_POSSIBLE_FEED_PRECONDITIONS_URL

        GET_POSSIBLE_SLA_METRIC_OPTIONS_URL = RestUrlConstants.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL

        GET_POSSIBLE_SLA_ACTION_OPTIONS_URL = RestUrlConstants.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL

        VALIDATE_SLA_ACTION_URL = RestUrlConstants.VALIDATE_SLA_ACTION_URL

        SAVE_FEED_SLA_URL = RestUrlConstants.SAVE_FEED_SLA_URL;

        SAVE_SLA_URL = RestUrlConstants.SAVE_SLA_URL

        DELETE_SLA_URL = RestUrlConstants.DELETE_SLA_URL

        GET_FEED_SLA_URL = RestUrlConstants.GET_FEED_SLA_URL

        GET_SLA_BY_ID_URL = RestUrlConstants.GET_SLA_BY_ID_URL

        GET_SLA_AS_EDIT_FORM = RestUrlConstants.GET_SLA_AS_EDIT_FORM

        GET_SLAS_URL = RestUrlConstants.GET_SLAS_URL

        GET_CONTROLLER_SERVICES_TYPES_URL = RestUrlConstants.GET_CONTROLLER_SERVICES_TYPES_URL

        GET_CONTROLLER_SERVICES_URL = RestUrlConstants.GET_CONTROLLER_SERVICES_URL

        GET_CONTROLLER_SERVICE_URL = RestUrlConstants.GET_CONTROLLER_SERVICE_URL

        CONTROLLER_SERVICES_PREVIEW_QUERY_URL = RestUrlConstants.CONTROLLER_SERVICES_PREVIEW_QUERY_URL
        
        FEED_VERSIONS_URL = RestUrlConstants.FEED_VERSIONS_URL
        
        FEED_VERSION_ID_URL = RestUrlConstants.FEED_VERSION_ID_URL
        
        FEED_VERSIONS_DIFF_URL = RestUrlConstants.FEED_VERSIONS_DIFF_URL

        FEED_PROFILE_STATS_URL = RestUrlConstants.FEED_PROFILE_STATS_URL

        FEED_PROFILE_SUMMARY_URL = RestUrlConstants.FEED_PROFILE_SUMMARY_URL

        FEED_PROFILE_VALID_RESULTS_URL = RestUrlConstants.FEED_PROFILE_VALID_RESULTS_URL

        FEED_PROFILE_INVALID_RESULTS_URL = RestUrlConstants.FEED_PROFILE_INVALID_RESULTS_URL

        ENABLE_FEED_URL = RestUrlConstants.ENABLE_FEED_URL

        DISABLE_FEED_URL = RestUrlConstants.DISABLE_FEED_URL

        START_FEED_URL = RestUrlConstants.START_FEED_URL

        UPLOAD_FILE_FEED_URL = RestUrlConstants.UPLOAD_FILE_FEED_URL

        FEED_DETAILS_BY_NAME_URL = RestUrlConstants.FEED_DETAILS_BY_NAME_URL

        CATEGORY_DETAILS_BY_SYSTEM_NAME_URL = RestUrlConstants.CATEGORY_DETAILS_BY_SYSTEM_NAME_URL

        CATEGORY_DETAILS_BY_ID_URL = RestUrlConstants.CATEGORY_DETAILS_BY_ID_URL

        /**
         * Gets the URL for retrieving the user fields for a new feed.
         *
         * @param {string} categoryId the category id
         * @returns {string} the URL
         */
        GET_FEED_USER_FIELDS_URL = RestUrlConstants.GET_FEED_USER_FIELDS_URL

        /**
         * URL for retrieving the user fields for a new category.
         * @type {string}
         */
        GET_CATEGORY_USER_FIELD_URL = RestUrlConstants.GET_CATEGORY_USER_FIELD_URL

        // Endpoint for administration of user fields
        ADMIN_USER_FIELDS = RestUrlConstants.ADMIN_USER_FIELDS

        //Field Policy Urls

        AVAILABLE_STANDARDIZATION_POLICIES = RestUrlConstants.AVAILABLE_STANDARDIZATION_POLICIES
        AVAILABLE_VALIDATION_POLICIES = RestUrlConstants.AVAILABLE_VALIDATION_POLICIES

        ADMIN_IMPORT_TEMPLATE_URL = RestUrlConstants.ADMIN_IMPORT_TEMPLATE_URL

        ADMIN_EXPORT_TEMPLATE_URL = RestUrlConstants.ADMIN_EXPORT_TEMPLATE_URL

        ADMIN_EXPORT_FEED_URL = RestUrlConstants.ADMIN_EXPORT_FEED_URL

        ADMIN_IMPORT_FEED_URL = RestUrlConstants.ADMIN_IMPORT_FEED_URL

        ADMIN_UPLOAD_STATUS_CHECK = RestUrlConstants.ADMIN_UPLOAD_STATUS_CHECK;

        // Hadoop Security Authorization
        HADOOP_SECURITY_GROUPS = RestUrlConstants.HADOOP_SECURITY_GROUPS

        // Security service URLs

        SECURITY_GROUPS_URL = RestUrlConstants.SECURITY_GROUPS_URL

        SECURITY_USERS_URL = RestUrlConstants.SECURITY_USERS_URL

        FEED_LINEAGE_URL = RestUrlConstants.FEED_LINEAGE_URL

        // Feed history data reindexing endpoint
        FEED_HISTORY_CONFIGURED = RestUrlConstants.FEED_HISTORY_CONFIGURED


        /**
         * Generates a URL for listing the controller services under the specified process group.
         *
         * @param {string} processGroupId the process group id
         * @returns {string} the URL for listing controller services
         */
        LIST_SERVICES_URL = RestUrlConstants.LIST_SERVICES_URL

        /**
         * The endpoint for retrieving the list of available Hive partition functions.
         *
         * @type {string}
         */
        PARTITION_FUNCTIONS_URL = RestUrlConstants.PARTITION_FUNCTIONS_URL

        /**
         * The endpoint for retrieving the NiFi status.
         * @type {string}
         */
        NIFI_STATUS = RestUrlConstants.NIFI_STATUS

        /**
         * the endpoint for determining if NiFi is up or not
         * @type {string}
         */
        IS_NIFI_RUNNING_URL = RestUrlConstants.IS_NIFI_RUNNING_URL

        /**
         * The endpoint for retrieving data sources.
         * @type {string}
         */
        GET_DATASOURCES_URL = RestUrlConstants.GET_DATASOURCES_URL

        /**
         * The endpoint for querying a data source.
         */
        QUERY_DATASOURCE_URL = RestUrlConstants.QUERY_DATASOURCE_URL

        /**
         * The endpoint for querying a data source.
         */
        PREVIEW_DATASOURCE_URL = RestUrlConstants.PREVIEW_DATASOURCE_URL

        GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL = RestUrlConstants.GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL

        /**
         * Get/Post roles changes for a Feed entity
         * @param feedId the feed id
         * @returns {string} the url to get/post feed role changes
         */
        FEED_ROLES_URL = RestUrlConstants.FEED_ROLES_URL

        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        CATEGORY_ROLES_URL = RestUrlConstants.CATEGORY_ROLES_URL

        /**
         * Get/Post roles changes for a Category entity
         * @param categoryId the category id
         * @returns {string} the url to get/post category role changes
         */
        CATEGORY_FEED_ROLES_URL = RestUrlConstants.CATEGORY_FEED_ROLES_URL

        /**
         * Get/Post roles changes for a Template entity
         * @param templateId the Template id
         * @returns {string} the url to get/post Template role changes
         */
        TEMPLATE_ROLES_URL = RestUrlConstants.TEMPLATE_ROLES_URL

        /**
         * Endpoint for roles changes to a Datasource entity.
         * @param {string} datasourceId the datasource id
         * @returns {string} the url for datasource role changes
         */
        DATASOURCE_ROLES_URL = RestUrlConstants.DATASOURCE_ROLES_URL

        CONNECTOR_ROLES_URL = RestUrlConstants.CONNECTOR_ROLES_URL

        /**
         * The URL for retrieving the list of template table option plugins.
         * @type {string}
         */
        UI_TEMPLATE_TABLE_OPTIONS = RestUrlConstants.UI_TEMPLATE_TABLE_OPTIONS

        /**
         * The URL for retrieving the list of templates for custom rendering with nifi processors
         * @type {string}
         */
        UI_PROCESSOR_TEMPLATES = RestUrlConstants.UI_PROCESSOR_TEMPLATES

        /**
         * return a list of the categorySystemName.feedSystemName
         * @type {string}
         */
        OPS_MANAGER_FEED_NAMES = RestUrlConstants.OPS_MANAGER_FEED_NAMES

        /**
         * Formats a date as a string.
         */
        FORMAT_DATE = RestUrlConstants.FORMAT_DATE

        /**
         * Parses a string as a date.
         */
        PARSE_DATE = RestUrlConstants.PARSE_DATE
    }