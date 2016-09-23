/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(MODULE_FEED_MGR).service('RestUrlService', function () {

    var self = this;

    this.ROOT = "";
    this.ADMIN_BASE_URL =this.ROOT+"/proxy/v1/feedmgr/admin";
    this.SECURITY_BASE_URL = this.ROOT + "/proxy/v1/security";
    this.TEMPLATES_BASE_URL =this.ROOT+"/proxy/v1/feedmgr/templates";
    this.FEEDS_BASE_URL = this.ROOT+"/proxy/v1/feedmgr/feeds";
    this.SLA_BASE_URL = this.ROOT + "/proxy/v1/feedmgr/sla";
    this.CONTROLLER_SERVICES_BASE_URL = this.ROOT+"/proxy/v1/feedmgr/nifi/controller-services";
    this.GET_TEMPLATES_URL = self.TEMPLATES_BASE_URL;
    this.GET_UNREGISTERED_TEMPLATES_URL = self.TEMPLATES_BASE_URL+"/unregistered";
    this.HADOOP_AUTHORIZATATION_BASE_URL = self.ROOT + "/proxy/v1/feedmgr/hadoop-authorization";
    this.UPLOAD_SAMPLE_TABLE_FILE = self.FEEDS_BASE_URL+"/table/sample-file";

    this.VALIDATE_CRON_EXPRESSION_URL = this.ROOT+"/proxy/v1/feedmgr/util/cron-expression/validate";

    this.PREVIEW_CRON_EXPRESSION_URL =this.ROOT+"/proxy/v1/feedmgr/util/cron-expression/preview";

    this.GET_SYSTEM_NAME =this.ROOT+"/proxy/v1/feedmgr/util/system-name";

    this.ICONS_URL =this.ROOT+"/proxy/v1/feedmgr/util/icons";
    this.ICON_COLORS_URL =this.ROOT+"/proxy/v1/feedmgr/util/icon-colors";

    this.CODE_MIRROR_TYPES_URL = this.ROOT+"/proxy/v1/feedmgr/util/codemirror-types";


    this.CATEGORIES_URL =this.ROOT+"/proxy/v1/feedmgr/categories";

    this.ELASTIC_SEARCH_URL = this.ROOT+"/proxy/v1/feedmgr/search";

    this.HIVE_SERVICE_URL = this.ROOT+"/proxy/v1/hive";

    this.SPARK_SHELL_SERVICE_URL = this.ROOT + "/proxy/v1/spark/shell";

    ///TEMPLATE REGISTRATION

    this.REGISTER_TEMPLATE_URL = function() {
        return self.TEMPLATES_BASE_URL+"/register";
    }

    this.GET_REGISTERED_TEMPLATES_URL = self.TEMPLATES_BASE_URL+"/registered";


    this.GET_REGISTERED_TEMPLATE_PROPERTIES_URL = function(templateId) {
        return self.GET_REGISTERED_TEMPLATES_URL+"/"+templateId+"/properties";
    }

    this.GET_REGISTERED_TEMPLATE_URL = function(templateId) {
        return self.GET_REGISTERED_TEMPLATES_URL+"/"+templateId;
    }

    this.REGISTERED_TEMPLATE_NIFI_INPUT_PORTS = function(nifiTemplateId){
        return self.TEMPLATES_BASE_URL+"/nifi/"+nifiTemplateId+"/input-ports";
    }

    this.REGISTERED_TEMPLATE_NIFI_OUTPUT_PORTS = function(nifiTemplateId){
        return self.TEMPLATES_BASE_URL+"/nifi/"+nifiTemplateId+"/output-ports";
    }

    this.REGISTERED_TEMPLATE_NIFI_ALL_PORTS = function(nifiTemplateId){
        return self.TEMPLATES_BASE_URL+"/nifi/"+nifiTemplateId+"/ports";
    }

    this.ALL_REUSABLE_FEED_INPUT_PORTS = this.ROOT+"/proxy/v1/feedmgr/nifi/reusable-input-ports";

    this.CONFIGURATION_PROPERTIES_URL =this.ROOT+"/proxy/v1/feedmgr/nifi/configuration/properties";
    this.METADATA_PROPERTY_NAMES_URL  =this.ROOT+"/proxy/v1/feedmgr/metadata-properties";



    //FEED URLS


    this.CREATE_FEED_FROM_TEMPLATE_URL = self.FEEDS_BASE_URL;

    this.MERGE_FEED_WITH_TEMPLATE = function(feedId){
        return self.GET_FEEDS_URL+"/"+feedId+"/merge-template";
    }


    this.GET_FEEDS_URL = self.FEEDS_BASE_URL;

    this.GET_FEED_NAMES_URL = self.FEEDS_BASE_URL+"/names";

    this.GET_POSSIBLE_FEED_PRECONDITIONS_URL = self.FEEDS_BASE_URL+"/possible-preconditions";

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

    this.GET_SLA_AS_EDIT_FORM = function (slaId) {
        return self.SLA_BASE_URL + "/" + slaId + "/form-object";
    }

    this.GET_SLAS_URL = self.SLA_BASE_URL;

    this.GET_CONTROLLER_SERVICES_TYPES_URL = self.CONTROLLER_SERVICES_BASE_URL+"/types";

    this.GET_CONTROLLER_SERVICES_URL = self.CONTROLLER_SERVICES_BASE_URL;


    this.FEED_PROFILE_STATS_URL = function(feedId) {
        return self.GET_FEEDS_URL+"/"+feedId+"/profile-stats";
    }

    this.FEED_PROFILE_SUMMARY_URL = function(feedId) {
        return self.GET_FEEDS_URL+"/"+feedId+"/profile-summary";
    }

    this.FEED_PROFILE_VALID_RESULTS_URL = function(feedId, processingDttm) {
        return self.GET_FEEDS_URL+"/"+feedId+"/profile-valid-results";
    }

    this.FEED_PROFILE_INVALID_RESULTS_URL = function(feedId, processingDttm) {
        return self.GET_FEEDS_URL+"/"+feedId+"/profile-invalid-results";
    }

    this.ENABLE_FEED_URL = function(feedId){
        return self.FEEDS_BASE_URL+"/enable/"+feedId;
    }
    this.DISABLE_FEED_URL = function(feedId){
        return self.FEEDS_BASE_URL+"/disable/"+feedId;
    }

    /**
     * Gets the URL for retrieving the user fields for a new feed.
     *
     * @param {string} categoryId the category id
     * @returns {string} the URL
     */
    this.GET_FEED_USER_FIELDS_URL = function(categoryId) {
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

    this.AVAILABLE_STANDARDIZATION_POLICIES = this.ROOT+"/proxy/v1/field-policies/standardization";
    this.AVAILABLE_VALIDATION_POLICIES = this.ROOT+"/proxy/v1/field-policies/validation";


    this.ADMIN_IMPORT_TEMPLATE_URL = self.ADMIN_BASE_URL+"/import-template";

    this.ADMIN_EXPORT_TEMPLATE_URL = self.ADMIN_BASE_URL+"/export-template";

    this.ADMIN_EXPORT_FEED_URL = self.ADMIN_BASE_URL+"/export-feed";

    this.ADMIN_IMPORT_FEED_URL = self.ADMIN_BASE_URL+"/import-feed";

    // Hadoop Security Authorization
    this.HADOOP_SECURITY_GROUPS = self.HADOOP_AUTHORIZATATION_BASE_URL + "/groups";

    // Security service URLs

    this.SECURITY_GROUPS_URL = self.SECURITY_BASE_URL + "/groups";

    this.SECURITY_USERS_URL = self.SECURITY_BASE_URL + "/users";
});
