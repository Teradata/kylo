/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(MODULE_FEED_MGR).service('RestUrlService', function () {

    var self = this;

    this.ROOT = "";
    this.TEMPLATES_BASE_URL =this.ROOT+"/api/v1/feedmgr/templates";
    this.FEEDS_BASE_URL = this.ROOT+"/api/v1/feedmgr/feeds";
    this.CONTROLLER_SERVICES_BASE_URL = this.ROOT+"/api/v1/nifi/controller-services";
    this.GET_TEMPLATES_URL = self.TEMPLATES_BASE_URL;
    this.GET_UNREGISTERED_TEMPLATES_URL = self.TEMPLATES_BASE_URL+"/unregistered";

    this.UPLOAD_SAMPLE_TABLE_FILE = self.FEEDS_BASE_URL+"/table/sample-file";

    this.VALIDATE_CRON_EXPRESSION_URL = this.ROOT+"/api/v1/feedmgr/util/cron-expression/validate";

    this.PREVIEW_CRON_EXPRESSION_URL =this.ROOT+"/api/v1/feedmgr/util/cron-expression/preview";

    this.CODE_MIRROR_TYPES_URL = this.ROOT+"/api/v1/feedmgr/util/codemirror-types";


    this.CATEGORIES_URL =this.ROOT+"/api/v1/feedmgr/categories";

    this.ELASTIC_SEARCH_URL = this.ROOT+"/api/v1/feedmgr/search";

    this.HIVE_SERVICE_URL = this.ROOT+"/api/v1/hive";

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

    this.CONFIGURATION_PROPERTIES_URL =this.ROOT+"/api/v1/feedmgr/nifi/configuration/properties";
    this.METADATA_PROPERTY_NAMES_URL  =this.ROOT+"/api/v1/feedmgr/metadata-properties";



    //FEED URLS


    this.CREATE_FEED_FROM_TEMPLATE_URL = self.FEEDS_BASE_URL;

    this.MERGE_FEED_WITH_TEMPLATE = function(feedId){
        return self.GET_FEEDS_URL+"/"+feedId+"/merge-template";
    }


    this.GET_FEEDS_URL = self.FEEDS_BASE_URL;

    this.GET_FEED_NAMES_URL = self.FEEDS_BASE_URL+"/names";

    this.GET_POSSIBLE_FEED_PRECONDITIONS_URL = self.FEEDS_BASE_URL+"/possible-preconditions";

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

});