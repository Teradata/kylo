import * as angular from 'angular';
import 'pascalprecht.translate';
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var thinkbigFeedInfo = function () {
    return {
        restrict: "EA",
        bindToController: {
            selectedTabIndex: "="
        },
            scope: {
                versions: '=?'
            },
        controllerAs: "vm",
        templateUrl: "js/feed-mgr/feeds/edit-feed/details/feed-info.html",
            controller: "FeedInfoController",
            link: function ($scope:any, element:any, attrs:any, controller:any) {
                if ($scope.versions == undefined) {
                    $scope.versions = false;
                }
            }
    };
};



export class FeedInfoController {

        versions:any = this.$scope.versions;
        /**
         * Flag if we have fully initialized or not
         * @type {boolean}
         */
        initialized:boolean=false;
        /**
         * The feed Model
         * @type {*}
         */
        model:any = this.FeedService.editFeedModel;

        /**
         * flag to render the custom presteps
         * @type {boolean}
         */
        renderPreStepTemplates:boolean = false;



// define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular, moduleName) {

    constructor (private $scope:any, private $injector:any,private $ocLazyLoad:any,private FeedService:any, private UiComponentsService:any) {

        var self = this;
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
                .then((tableOption:any) => {
                    if(tableOption.totalPreSteps >0){
                        self.renderPreStepTemplates = true;
                    }

                   if(angular.isDefined(tableOption.initializeScript) && angular.isDefined(tableOption.initializeServiceName) && tableOption.initializeScript != null &&  tableOption.initializeServiceName != null) {
                       $ocLazyLoad.load([tableOption.initializeScript]).then((file:any) =>{
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

    }

}

    angular.module(moduleName).controller("FeedInfoController", ["$scope", "$injector","$ocLazyLoad","FeedService","UiComponentsService", FeedInfoController]);
    angular.module(moduleName).directive("thinkbigFeedInfo", [thinkbigFeedInfo]);
