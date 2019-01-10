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


export class RestUrlConstants {

    static ROOT = "";
    static ADMIN_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/admin";
    static ADMIN_V2_BASE_URL = RestUrlConstants.ROOT + "/proxy/v2/feedmgr/admin";
    static SECURITY_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/security";
    static TEMPLATES_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/templates";
    static FEEDS_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/feeds";
    static SLA_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/sla";
    static CATALOG_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/catalog";
    static CONTROLLER_SERVICES_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/controller-services";
    static SCHEMA_DISCOVERY_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/schema-discovery";
    static GET_TEMPLATES_URL = RestUrlConstants.TEMPLATES_BASE_URL;
    static GET_UNREGISTERED_TEMPLATES_URL = RestUrlConstants.TEMPLATES_BASE_URL + "/unregistered";
    static HADOOP_AUTHORIZATATION_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/hadoop-authorization";
    static UI_BASE_URL = RestUrlConstants.ROOT + "/api/v1/ui";
    static DOMAIN_TYPES_BASE_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/domain-types";

    static UPLOAD_SAMPLE_TABLE_FILE = RestUrlConstants.SCHEMA_DISCOVERY_BASE_URL + "/hive/sample-file";
    static UPLOAD_SPARK_SAMPLE_FILE = RestUrlConstants.SCHEMA_DISCOVERY_BASE_URL + "/spark/sample-file";
    static LIST_FILE_PARSERS = RestUrlConstants.SCHEMA_DISCOVERY_BASE_URL + "/file-parsers";
    static LIST_SPARK_FILE_PARSERS = RestUrlConstants.SCHEMA_DISCOVERY_BASE_URL + "/spark-file-parsers";

    static VALIDATE_CRON_EXPRESSION_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/cron-expression/validate";

    static PREVIEW_CRON_EXPRESSION_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/cron-expression/preview";

    static GET_SYSTEM_NAME = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/system-name";

    static ICONS_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/icons";
    static ICON_COLORS_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/icon-colors";

    static CODE_MIRROR_TYPES_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/codemirror-types";

    static CATEGORIES_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/categories";

    static SEARCH_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/search";

    static HIVE_SERVICE_URL = RestUrlConstants.ROOT + "/proxy/v1/hive";

    static SPARK_SHELL_SERVICE_URL = RestUrlConstants.ROOT + "/proxy/v1/spark/shell";

    ///TEMPLATE REGISTRATION

    static REGISTER_TEMPLATE_URL() {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/register";
    }

    static SAVE_TEMPLATE_ORDER_URL = RestUrlConstants.TEMPLATES_BASE_URL + "/order";

    static GET_REGISTERED_TEMPLATES_URL = RestUrlConstants.TEMPLATES_BASE_URL + "/registered";

    static GET_REGISTERED_TEMPLATE_PROPERTIES_URL(templateId:string) {
        return RestUrlConstants.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/properties";
    }

    static GET_REGISTERED_TEMPLATE_URL(templateId:string) {
        return RestUrlConstants.GET_REGISTERED_TEMPLATES_URL + "/" + templateId;
    }

    static REGISTERED_TEMPLATE_NIFI_INPUT_PORTS(nifiTemplateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/input-ports";
    }

    static REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS(nifiTemplateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/output-ports";
    }

    static REGISTERED_TEMPLATE_NIFI_ALL_PORTS(nifiTemplateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/ports";
    }

    static TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS(nifiTemplateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/datasource-definitions";
    }

    static TEMPLATE_FLOW_INFORMATION(nifiTemplateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/nifi/" + nifiTemplateId + "/flow-info";
    }

    static DISABLE_REGISTERED_TEMPLATE_URL(templateId:string) {
        return RestUrlConstants.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/disable";
    }
    static ENABLE_REGISTERED_TEMPLATE_URL(templateId:string) {
        return RestUrlConstants.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/enable";
    }
    static DELETE_REGISTERED_TEMPLATE_URL(templateId:string) {
        return RestUrlConstants.GET_REGISTERED_TEMPLATES_URL + "/" + templateId + "/delete";
    }

    static REMOTE_PROCESS_GROUP_AWARE = RestUrlConstants.TEMPLATES_BASE_URL+"/remote-process-group/status";

    static ALL_REUSABLE_FEED_INPUT_PORTS = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/reusable-input-ports";


    static ROOT_INPUT_PORTS = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/root-input-ports";

    static CONFIGURATION_PROPERTIES_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/configuration/properties";
    static METADATA_PROPERTY_NAMES_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/metadata-properties";

    static GET_DATASOURCE_TYPES = RestUrlConstants.ROOT + "/proxy/v1/metadata/datasource/types";

    //FEED URLS

    static CREATE_FEED_FROM_TEMPLATE_URL = RestUrlConstants.FEEDS_BASE_URL;

    static MERGE_FEED_WITH_TEMPLATE(feedId:string) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/merge-template";
    }

    static GET_FEEDS_URL = RestUrlConstants.FEEDS_BASE_URL;

    static GET_FEED_NAMES_URL = RestUrlConstants.FEEDS_BASE_URL + "/names";

    static GET_POSSIBLE_FEED_PRECONDITIONS_URL = RestUrlConstants.FEEDS_BASE_URL + "/possible-preconditions";

    static GET_POSSIBLE_SLA_METRIC_OPTIONS_URL = RestUrlConstants.SLA_BASE_URL + "/available-metrics";

    static GET_POSSIBLE_SLA_ACTION_OPTIONS_URL = RestUrlConstants.SLA_BASE_URL + "/available-responders";

    static VALIDATE_SLA_ACTION_URL = RestUrlConstants.SLA_BASE_URL + "/action/validate";

    static SAVE_FEED_SLA_URL(feedId:string) {
        return RestUrlConstants.SLA_BASE_URL + "/feed/" + feedId;
    }
    static SAVE_SLA_URL = RestUrlConstants.SLA_BASE_URL;

    static DELETE_SLA_URL(slaId:string) {
        return RestUrlConstants.SLA_BASE_URL + "/" + slaId;
    }

    static GET_FEED_SLA_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/" + feedId + "/sla";
    }

    static GET_SLA_BY_ID_URL(slaId:string) {
        return RestUrlConstants.SLA_BASE_URL + "/" + slaId;
    }

    static GET_SLA_AS_EDIT_FORM(slaId:string) {
        return RestUrlConstants.SLA_BASE_URL + "/" + slaId + "/form-object";
    }

    static GET_SLAS_URL = RestUrlConstants.SLA_BASE_URL;

    static GET_CONTROLLER_SERVICES_TYPES_URL = RestUrlConstants.CONTROLLER_SERVICES_BASE_URL + "/types";

    static GET_CONTROLLER_SERVICES_URL = RestUrlConstants.CONTROLLER_SERVICES_BASE_URL;

    static GET_CONTROLLER_SERVICE_URL(serviceId:string) {
        return RestUrlConstants.CONTROLLER_SERVICES_BASE_URL + "/" + serviceId;
    }

    static CONTROLLER_SERVICES_PREVIEW_QUERY_URL(serviceId:string, schema:string, table:string, limit:number) {
        return RestUrlConstants.CONTROLLER_SERVICES_BASE_URL + "/" + serviceId;
    }

    static FEED_VERSIONS_URL(feedId:string) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/versions";
    }

    static FEED_VERSION_ID_URL(feedId:string, verId:string) {
        return RestUrlConstants.FEED_VERSIONS_URL(feedId) + "/" + verId;
    }

    static FEED_VERSIONS_DIFF_URL(feedId:string, verId1:string, verId2:string) {
        return RestUrlConstants.FEED_VERSION_ID_URL(feedId, verId1) + "/diff/" + verId2;
    }

    static FEED_PROFILE_STATS_URL(feedId:string) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/profile-stats";
    }

    static FEED_PROFILE_SUMMARY_URL(feedId:string, pageNumber: number, pageSize: number) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/profile-summary?page=" + pageNumber + "&pageSize=" + pageSize;
    }

    static FEED_PROFILE_VALID_RESULTS_URL(feedId:string, processingDttm:string) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/profile-valid-results";
    }

    static FEED_PROFILE_INVALID_RESULTS_URL(feedId:string, processingDttm:string) {
        return RestUrlConstants.GET_FEEDS_URL + "/" + feedId + "/profile-invalid-results";
    }

    static ENABLE_FEED_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/enable/" + feedId;
    }
    static DISABLE_FEED_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/disable/" + feedId;
    }

    static START_FEED_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/start/" + feedId;
    }

    static UPLOAD_FILE_FEED_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/" + feedId + "/upload-file";
    }

    static FEED_DETAILS_BY_NAME_URL(feedName:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/by-name/" + feedName;
    }

    static CATEGORY_DETAILS_BY_SYSTEM_NAME_URL(categoryName:string) {
        return RestUrlConstants.CATEGORIES_URL + "/by-name/" + categoryName;
    };

    static CATEGORY_DETAILS_BY_ID_URL(categoryId:string) {
        return RestUrlConstants.CATEGORIES_URL + "/by-id/" + categoryId;
    }

    /**
     * Gets the URL for retrieving the user fields for a new feed.
     *
     * @param {string} categoryId the category id
     * @returns {string} the URL
     */
    static GET_FEED_USER_FIELDS_URL(categoryId:string) {
        return RestUrlConstants.CATEGORIES_URL + "/" + categoryId + "/user-fields";
    };

    /**
     * URL for retrieving the user fields for a new category.
     * @type {string}
     */
    static GET_CATEGORY_USER_FIELD_URL = RestUrlConstants.CATEGORIES_URL + "/user-fields";

    // Endpoint for administration of user fields
    static ADMIN_USER_FIELDS = RestUrlConstants.ADMIN_BASE_URL + "/user-fields";

    //Field Policy Urls

    static AVAILABLE_STANDARDIZATION_POLICIES = RestUrlConstants.ROOT + "/proxy/v1/field-policies/standardization";
    static AVAILABLE_VALIDATION_POLICIES = RestUrlConstants.ROOT + "/proxy/v1/field-policies/validation";

    static ADMIN_IMPORT_TEMPLATE_URL = RestUrlConstants.ADMIN_V2_BASE_URL + "/import-template";

    static ADMIN_EXPORT_TEMPLATE_URL = RestUrlConstants.ADMIN_BASE_URL + "/export-template";

    static ADMIN_EXPORT_FEED_URL = RestUrlConstants.ADMIN_BASE_URL + "/export-feed";

    static ADMIN_IMPORT_FEED_URL = RestUrlConstants.ADMIN_V2_BASE_URL + "/import-feed";

    static ADMIN_UPLOAD_STATUS_CHECK(key:string) {
        return RestUrlConstants.ADMIN_BASE_URL + "/upload-status/" + key;
    };

    // Hadoop Security Authorization
    static HADOOP_SECURITY_GROUPS = RestUrlConstants.HADOOP_AUTHORIZATATION_BASE_URL + "/groups";

    // Security service URLs

    static SECURITY_GROUPS_URL = RestUrlConstants.SECURITY_BASE_URL + "/groups";

    static SECURITY_USERS_URL = RestUrlConstants.SECURITY_BASE_URL + "/users";

    static FEED_LINEAGE_URL(feedId:string) {
        return RestUrlConstants.ROOT + "/proxy/v1/metadata/feed/" + feedId + "/lineage";
    };

    // Feed history data reindexing endpoint
    static FEED_HISTORY_CONFIGURED = RestUrlConstants.ROOT + "/proxy/v1/metadata/feed/data-history-reindex-configured";


    /**
     * Generates a URL for listing the controller services under the specified process group.
     *
     * @param {string} processGroupId the process group id
     * @returns {string} the URL for listing controller services
     */
    static LIST_SERVICES_URL(processGroupId:string) {
        return RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/process-group/" + processGroupId;
    };

    /**
     * The endpoint for retrieving the list of available Hive partition functions.
     *
     * @type {string}
     */
    static PARTITION_FUNCTIONS_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/util/partition-functions";

    /**
     * The endpoint for retrieving the NiFi status.
     * @type {string}
     */
    static NIFI_STATUS = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/status";

    /**
     * the endpoint for determining if NiFi is up or not
     * @type {string}
     */
    static IS_NIFI_RUNNING_URL = RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/running";

    /**
     * The endpoint for retrieving data sources.
     * @type {string}
     */
    static GET_DATASOURCES_URL = RestUrlConstants.ROOT + "/proxy/v1/metadata/datasource";

    /**
     * The endpoint for querying a data source.
     */
    static QUERY_DATASOURCE_URL(id:string) {
        return RestUrlConstants.ROOT + "/proxy/v1/metadata/datasource/" + id + "/query";
    };

    /**
     * The endpoint for querying a data source.
     */
    static PREVIEW_DATASOURCE_URL(id:string, schema:string, table:string, limit:number) {
        return RestUrlConstants.ROOT + "/proxy/v1/metadata/datasource/" + id + "/preview/" + schema + "/" + table + "?limit=" + limit;
    };

    static GET_NIFI_CONTROLLER_SERVICE_REFERENCES_URL(id:string) {
        return RestUrlConstants.ROOT + "/proxy/v1/feedmgr/nifi/controller-services/" + id + "/references";
    }

    /**
     * Get/Post roles changes for a Feed entity
     * @param feedId the feed id
     * @returns {string} the url to get/post feed role changes
     */
    static FEED_ROLES_URL(feedId:string) {
        return RestUrlConstants.FEEDS_BASE_URL + "/" + feedId + "/roles"
    };

    /**
     * Get/Post roles changes for a Category entity
     * @param categoryId the category id
     * @returns {string} the url to get/post category role changes
     */
    static CATEGORY_ROLES_URL(categoryId:string) {
        return RestUrlConstants.CATEGORIES_URL + "/" + categoryId + "/roles"
    };

    /**
     * Get/Post roles changes for a Category entity
     * @param categoryId the category id
     * @returns {string} the url to get/post category role changes
     */
    static CATEGORY_FEED_ROLES_URL(categoryId:string) {
        return RestUrlConstants.CATEGORIES_URL + "/" + categoryId + "/feed-roles"
    };

    /**
     * Get/Post roles changes for a Template entity
     * @param templateId the Template id
     * @returns {string} the url to get/post Template role changes
     */
    static TEMPLATE_ROLES_URL(templateId:string) {
        return RestUrlConstants.TEMPLATES_BASE_URL + "/registered/" + templateId + "/roles"
    };

    /**
     * Endpoint for roles changes to a Datasource entity.
     * @param {string} datasourceId the datasource id
     * @returns {string} the url for datasource role changes
     */
    static DATASOURCE_ROLES_URL(datasourceId:string) {
        return RestUrlConstants.CATALOG_BASE_URL + "/datasource/" + encodeURIComponent(datasourceId) + "/roles";
    };

    /**
     * Endpoint for roles changes to a Datasource entity.
     * @param {string} datasourceId the datasource id
     * @returns {string} the url for datasource role changes
     */
    static CONNECTOR_ROLES_URL(connectorId:string) {
        return RestUrlConstants.CATALOG_BASE_URL + "/connector/" + encodeURIComponent(connectorId) + "/roles";
    };

    /**
     * The URL for retrieving the list of template table option plugins.
     * @type {string}
     */
    static UI_TEMPLATE_TABLE_OPTIONS = RestUrlConstants.UI_BASE_URL + "/template-table-options";

    /**
     * The URL for retrieving the list of templates for custom rendering with nifi processors
     * @type {string}
     */
    static UI_PROCESSOR_TEMPLATES = RestUrlConstants.UI_BASE_URL + "/processor-templates";

    /**
     * return a list of the categorySystemName.feedSystemName
     * @type {string}
     */
    static OPS_MANAGER_FEED_NAMES = "/proxy/v1/feeds/names";

    /**
     * Formats a date as a string.
     */
    static FORMAT_DATE = "/proxy/v1/feedmgr/util/format-date";

    /**
     * Parses a string as a date.
     */
    static PARSE_DATE = "/proxy/v1/feedmgr/util/parse-date";

    static SCHEMA_DISCOVERY_PARSE_DATA_SET = "/proxy/v1/schema-discovery/hive/dataset"

    static SCHEMA_DISCOVERY_TABLE_SETTINGS_DATA_SET = "/proxy/v1/schema-discovery/table-settings/dataset"




    constructor(){}
}
