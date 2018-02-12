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
define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigDefineFeedGeneralInfo', '^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-general-info.html',
            controller: "DefineFeedGeneralInfoController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        };
    };
    var DefineFeedGeneralInfoController = /** @class */ (function () {
        function DefineFeedGeneralInfoController($scope, $log, $http, $mdToast, RestUrlService, FeedService, CategoriesService) {
            var _this = this;
            this.$scope = $scope;
            this.$log = $log;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.CategoriesService = CategoriesService;
            /**
             * The angular form
             * @type {{}}
             */
            this.defineFeedGeneralForm = {};
            this.templates = [];
            this.model = this.FeedService.createFeedModel;
            this.isValid = false;
            this.stepperController = null;
            // Contains existing system feed names for the current category
            this.existingFeedNames = {};
            this.categorySearchText = '';
            this.categoriesService = this.CategoriesService;
            /**
             * are we populating the feed name list for validation
             * @type {boolean}
             */
            this.populatingExsitngFeedNames = false;
            this.searchTextChange = function (text) {
                //   $log.info('Text changed to ' + text);
            };
            this.categorySearchTextChanged = this.searchTextChange;
            this.selectedItemChange = function (item) {
                //only allow it if the category is there and the 'createFeed' flag is true
                if (item != null && item != undefined && item.createFeed) {
                    this.model.category.name = item.name;
                    this.model.category.id = item.id;
                    this.model.category.systemName = item.systemName;
                    this.setSecurityGroups(item.name);
                    this.validateUniqueFeedName();
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                        this.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                    }
                }
                else {
                    this.model.category.name = null;
                    this.model.category.id = null;
                    this.model.category.systemName = null;
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                        this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                    }
                    if (item && item.createFeed == false) {
                        if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                            this.defineFeedGeneralForm['category'].$setValidity('accessDenied', false);
                        }
                    }
                    else {
                        if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['category']) {
                            this.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                        }
                    }
                }
            };
            this.categorySelectedItemChange = this.selectedItemChange;
            this.existingFeedNameKey = function (categoryName, feedName) {
                return categoryName + "." + feedName;
            };
            /**
            * updates the {@code existingFeedNames} object with the latest feed names from the server
            * @returns {promise}
            */
            this.populateExistingFeedNames = function () {
                var _this = this;
                if (!this.populatingExsitngFeedNames) {
                    this.populatingExsitngFeedNames = true;
                    this.FeedService.getFeedNames().then();
                    return this.$http.get(this.RestUrlService.OPS_MANAGER_FEED_NAMES).then(function (response) {
                        _this.existingFeedNames = {};
                        if (response.data != null && response.data != null) {
                            angular.forEach(response.data, function (categoryAndFeed) {
                                var categoryName = categoryAndFeed.substr(0, categoryAndFeed.indexOf('.'));
                                var feedName = categoryAndFeed.substr(categoryAndFeed.indexOf('.') + 1);
                                _this.existingFeedNames[categoryAndFeed] = feedName;
                            });
                            _this.populatingExsitngFeedNames = false;
                        }
                    }, function () {
                        _this.populatingExsitngFeedNames = false;
                    });
                }
            };
            this._validate = function () {
                //validate to ensure the name is unique in this category
                if (this.model && this.model.category && this.existingFeedNames[this.existingFeedNameKey(this.model.category.systemName, this.model.systemFeedName)]) {
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                        this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', false);
                    }
                }
                else {
                    if (this.defineFeedGeneralForm && this.defineFeedGeneralForm['feedName']) {
                        this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                    }
                }
            };
            this.validateUniqueFeedName = function () {
                var _this = this;
                if (this.model && this.model.id && this.model.id.length > 0) {
                    this.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                }
                else if (_.isEmpty(this.existingFeedNames)) {
                    if (!this.populatingExsitngFeedNames) {
                        this.populateExistingFeedNames().then(function () {
                            _this._validate();
                        });
                    }
                }
                else {
                    this._validate();
                }
            };
            //  getRegisteredTemplates();
            this.validate = function () {
                var valid = this.isNotEmpty(this.model.category.name) && this.isNotEmpty(this.model.feedName) && this.isNotEmpty(this.model.templateId);
                this.isValid = valid;
            };
            this.setSecurityGroups = function (newVal) {
                if (newVal) {
                    var category = this.categoriesService.findCategoryByName(newVal);
                    if (category != null) {
                        var securityGroups = category.securityGroups;
                        this.model.securityGroups = securityGroups;
                    }
                }
            };
            this.isNotEmpty = function (item) {
                return item != null && item != undefined && item != '';
            };
            this.onTemplateChange = function () {
            };
            /**
             * Return a list of the Registered Templates in the system
             * @returns {HttpPromise}
             */
            this.getRegisteredTemplates = function () {
                var successFn = function (response) {
                    _this.templates = response.data;
                };
                var errorFn = function (err) {
                };
                var promise = _this.$http.get(_this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
                promise.then(successFn, errorFn);
                return promise;
            };
            this.stepNumber = parseInt(this.stepIndex) + 1;
            this.populateExistingFeedNames();
            $scope.$watch(function () {
                return _this.model.id;
            }, function (newVal) {
                if (newVal == null && (angular.isUndefined(_this.model.cloned) || _this.model.cloned == false)) {
                    _this.category = null;
                }
                else {
                    _this.category = _this.model.category;
                }
            });
            var feedNameWatch = $scope.$watch(function () {
                return _this.model.feedName;
            }, function (newVal) {
                FeedService.getSystemName(newVal).then(function (response) {
                    _this.model.systemFeedName = response.data;
                    _this.validateUniqueFeedName();
                    _this.validate();
                });
            });
            $scope.$watch(function () {
                return _this.model.category.name;
            }, function (newVal) {
                _this.validate();
            });
            var templateIdWatch = $scope.$watch(function () {
                return _this.model.templateId;
            }, function (newVal) {
                _this.validate();
            });
            $scope.$on('$destroy', function () {
                feedNameWatch();
                templateIdWatch();
                _this.model = null;
            });
        }
        ;
        return DefineFeedGeneralInfoController;
    }());
    exports.DefineFeedGeneralInfoController = DefineFeedGeneralInfoController;
    angular.module(moduleName).controller('DefineFeedGeneralInfoController', ["$scope", "$log", "$http", "$mdToast", "RestUrlService", "FeedService", "CategoriesService", DefineFeedGeneralInfoController]);
    angular.module(moduleName)
        .directive('thinkbigDefineFeedGeneralInfo', directive);
});
//# sourceMappingURL=DefineFeedGeneralInfoDirective.js.map