/**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
angular.module(MODULE_FEED_MGR).service('StateService', function ($state) {

    var self = this;


    this.navigateToFeedDetails=function(feedId){
        $state.go('feed-details',{feedId:feedId});
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

    this.navigateToImportTemplate = function(){
        $state.go('import-template');
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

});