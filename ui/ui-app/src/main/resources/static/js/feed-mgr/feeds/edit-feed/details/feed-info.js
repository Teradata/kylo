define(["require", "exports", "angular", "pascalprecht.translate"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
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
            link: function ($scope, element, attrs, controller) {
                if ($scope.versions == undefined) {
                    $scope.versions = false;
                }
            }
        };
    };
    var FeedInfoController = /** @class */ (function () {
        // define(["angular", "feed-mgr/feeds/edit-feed/module-name"], function (angular, moduleName) {
        function FeedInfoController($scope, $injector, $ocLazyLoad, FeedService, UiComponentsService) {
            this.$scope = $scope;
            this.$injector = $injector;
            this.$ocLazyLoad = $ocLazyLoad;
            this.FeedService = FeedService;
            this.UiComponentsService = UiComponentsService;
            this.versions = this.$scope.versions;
            /**
             * Flag if we have fully initialized or not
             * @type {boolean}
             */
            this.initialized = false;
            /**
             * The feed Model
             * @type {*}
             */
            this.model = this.FeedService.editFeedModel;
            /**
             * flag to render the custom presteps
             * @type {boolean}
             */
            this.renderPreStepTemplates = false;
            var self = this;
            // Determine table option
            if (this.model.registeredTemplate.templateTableOption === null) {
                if (this.model.registeredTemplate.defineTable) {
                    this.model.registeredTemplate.templateTableOption = "DEFINE_TABLE";
                }
                else if (this.model.registeredTemplate.dataTransformation) {
                    this.model.registeredTemplate.templateTableOption = "DATA_TRANSFORMATION";
                }
                else {
                    this.model.registeredTemplate.templateTableOption = "NO_TABLE";
                }
            }
            if (this.model.registeredTemplate.templateTableOption !== "NO_TABLE") {
                UiComponentsService.getTemplateTableOption(this.model.registeredTemplate.templateTableOption)
                    .then(function (tableOption) {
                    if (tableOption.totalPreSteps > 0) {
                        self.renderPreStepTemplates = true;
                    }
                    if (angular.isDefined(tableOption.initializeScript) && angular.isDefined(tableOption.initializeServiceName) && tableOption.initializeScript != null && tableOption.initializeServiceName != null) {
                        $ocLazyLoad.load([tableOption.initializeScript]).then(function (file) {
                            var serviceName = tableOption.initializeServiceName;
                            if (angular.isDefined(serviceName)) {
                                var svc = $injector.get(serviceName);
                                if (angular.isDefined(svc) && angular.isFunction(svc.initializeEditFeed)) {
                                    var feedModel = FeedService.editFeedModel;
                                    svc.initializeEditFeed(tableOption, feedModel);
                                }
                            }
                            self.initialized = true;
                        });
                    }
                    else {
                        self.initialized = true;
                    }
                });
            }
            else {
                self.initialized = true;
            }
        }
        return FeedInfoController;
    }());
    exports.FeedInfoController = FeedInfoController;
    angular.module(moduleName).controller("FeedInfoController", ["$scope", "$injector", "$ocLazyLoad", "FeedService", "UiComponentsService", FeedInfoController]);
    angular.module(moduleName).directive("thinkbigFeedInfo", [thinkbigFeedInfo]);
});
//# sourceMappingURL=feed-info.js.map