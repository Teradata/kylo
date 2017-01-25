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
/**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
angular.module(MODULE_FEED_MGR).service('StateService', function ($state) {

    var self = this;

    this.navigateToFeedDetails = function (feedId, tabIndex) {
        if (tabIndex == null || tabIndex == undefined) {
            tabIndex = 0;
        }
        $state.go('feed-details', {feedId: feedId, tabIndex: tabIndex});
    }

    this.navigateToDefineFeed = function(templateId){
        $state.go('define-feed',{templateId:templateId});
    }

    this.navigateToDefineFeedComplete = function(feedModel,error){
        $state.go('define-feed-complete',{feedModel:feedModel,error:error});
    }

    this.navigateToFeeds = function(){
        $state.go('feeds');
    }

    this.navigateToRegisterTemplate = function(){
        $state.go('register-template');
    }

    this.navigateToRegisterTemplateComplete = function (message, templateModel, error) {
        $state.go('register-template-complete', {message: message, templateModel: templateModel, error: error});
    }

    this.navigateToImportTemplate = function(){
        $state.go('import-template');
    }


    this.navigatetoImportFeed = function(){
        $state.go('import-feed');
    }
    this.navigateToRegisterNifiTemplate = function(){
        $state.go('register-nifi-template',{registeredTemplateId:null,nifiTemplateId:null});
    }

    this.navigateToRegisteredTemplate = function(templateId, nifiTemplateId){
        $state.go('register-nifi-template',{registeredTemplateId:templateId,nifiTemplateId:nifiTemplateId});
    }

    this.navigateToServiceLevelAgreements = function(){
        $state.go('service-level-agreements');
    }

    this.navigateToTable = function(schema,table){
        $state.go('table',{schema:schema,tableName:table});
    }
    this.navigateToTables = function(){
        $state.go('tables');
    }

    this.navigateToCategoryDetails = function(categoryId){
        $state.go('category-details',{categoryId:categoryId});
    }

    this.navigateToCategories = function(){
        $state.go('categories');
    }

    this.navigateToSearch = function(){
       $state.go('search');
    }

    this.navigateToProfileSummary = function(feedId){
        $state.go('feed-details.profile-summary',{feedId:feedId})
    }
    this.navigateToProfileValidResults = function(feedId,processingdttm) {
        $state.go('feed-details.profile-valid',{feedId:feedId,processingdttm:processingdttm})
    }
    this.navigateToProfileInvalidResults = function(feedId,processingdttm) {
        $state.go('feed-details.profile-invalid',{feedId:feedId,processingdttm:processingdttm})
    }
    this.navigateToProfileStats = function(feedId,processingdttm) {
        $state.go('feed-details.profile-stats',{feedId:feedId,processingdttm:processingdttm})
    }

    /**
     * Navigates to the Groups page.
     */
    this.navigateToGroups = function() {
        $state.go("groups");
    };

    /**
     * Navigates to the Group Details page.
     *
     * @param {string} [opt_groupId] the system name of the group
     */
    this.navigateToGroupDetails = function(opt_groupId) {
        var safeGroupId = angular.isString(opt_groupId) ? encodeURIComponent(opt_groupId) : null;
        $state.go("group-details", {groupId: safeGroupId});
    };

    /**
     * Navigates to the Users page.
     */
    this.navigateToUsers = function() {
        $state.go("users");
    };

    /**
     * Navigates to the User Details page.
     *
     * @param {string} [opt_userId] the system name of the user
     */
    this.navigateToUserDetails = function(opt_userId) {
        var safeUserId = angular.isString(opt_userId) ? encodeURIComponent(opt_userId) : null;
        $state.go("user-details", {userId: safeUserId});
    };

    /**
     * Navigates to the Templates page.
     */
    this.navigateToRegisteredTemplates = function() {
        $state.go("registered-templates");
    };

    /**
     * Navigates to the Home page.
     */
    this.navigateToHome = function() {
        $state.go("home");
    };
});
