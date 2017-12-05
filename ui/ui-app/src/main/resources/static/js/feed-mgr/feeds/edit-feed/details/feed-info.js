define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular, moduleName) {

    var thinkbigFeedInfo = function () {
        return {
            restrict: "EA",
            bindToController: {
                selectedTabIndex: "="
            },
            controllerAs: "vm",
            scope: {},
            templateUrl: "js/feed-mgr/feeds/edit-feed/details/feed-info.html",
            controller: "FeedInfoController"
        };
    };

    var FeedInfoController = function ($injector,$ocLazyLoad,FeedService, UiComponentsService) {
        var self = this;
        /**
         * Flag if we have fully initialized or not
         * @type {boolean}
         */
        this.initialized=false;
        /**
         * The feed Model
         * @type {*}
         */
        this.model = FeedService.editFeedModel;

        /**
         * flag to render the custom presteps
         * @type {boolean}
         */
        this.renderPreStepTemplates = false;

        // Determine table option
        if (this.model.registeredTemplate.templateTableOption === null) {
            if (this.model.registeredTemplate.defineTable) {
                this.model.registeredTemplate.templateTableOption = "DEFINE_TABLE";
            } else if (this.model.registeredTemplate.dataTransformation) {
                this.model.registeredTemplate.templateTableOption = "DATA_TRANSFORMATION";
            } else {
                this.model.registeredTemplate.templateTableOption = "NO_TABLE";
            }
        }

        if (this.model.registeredTemplate.templateTableOption !== "NO_TABLE") {
            UiComponentsService.getTemplateTableOption(this.model.registeredTemplate.templateTableOption)
                .then(function (tableOption) {
                    if(tableOption.totalPreSteps >0){
                        self.renderPreStepTemplates = true;
                    }

                   if(angular.isDefined(tableOption.initializeScript) && angular.isDefined(tableOption.initializeServiceName) && tableOption.initializeScript != null &&  tableOption.initializeServiceName != null) {
                       $ocLazyLoad.load([tableOption.initializeScript]).then(function(file){
                           var serviceName = tableOption.initializeServiceName;
                           if(angular.isDefined(serviceName)) {
                               var svc = $injector.get(serviceName);
                               if (angular.isDefined(svc) && angular.isFunction(svc.initializeEditFeed)) {
                                   var feedModel = FeedService.editFeedModel;
                                   svc.initializeEditFeed(tableOption,feedModel);
                               }
                           }
                           self.initialized = true
                       });

                   }
                   else {
                       self.initialized = true
                   }
                });
            }
            else {
            self.initialized=true;
        }
    };

    angular.module(moduleName).controller("FeedInfoController", ["$injector","$ocLazyLoad","FeedService","UiComponentsService", FeedInfoController]);
    angular.module(moduleName).directive("thinkbigFeedInfo", [thinkbigFeedInfo]);
});
