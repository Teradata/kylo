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
define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedPropertiesController = /** @class */ (function () {
        function DefineFeedPropertiesController($scope, $http, $mdToast, RestUrlService, FeedTagService, FeedService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.RestUrlService = RestUrlService;
            this.FeedTagService = FeedTagService;
            this.FeedService = FeedService;
            this.tagChips = {};
            this.isValid = true;
            this.model = FeedService.createFeedModel;
            this.feedTagService = FeedTagService;
            this.tagChips.selectedItem = null;
            this.tagChips.searchText = null;
            if (angular.isUndefined(this.model.tags)) {
                this.model.tags = [];
            }
            // Update user fields when category changes
            $scope.$watch(function () { return _this.model.category.id; }, function (categoryId) {
                if (categoryId !== null) {
                    FeedService.getUserFields(categoryId)
                        .then(_this.setUserProperties);
                }
            });
        }
        /**
         * Sets the user fields for this feed.
         *
         * @param {Array} userProperties the user fields
         */
        DefineFeedPropertiesController.prototype.setUserProperties = function (userProperties) {
            var _this = this;
            // Convert old user properties to map
            var oldProperties = null;
            this.model.userProperties.forEach(function (property) {
                if (angular.isString(property.value) && property.value.length > 0) {
                    oldProperties[property.systemName] = property.value;
                }
            });
            // Set new user properties and copy values
            this.model.userProperties = angular.copy(userProperties);
            this.model.userProperties.forEach(function (property) {
                if (angular.isDefined(oldProperties[property.systemName])) {
                    property.value = oldProperties[property.systemName];
                    delete oldProperties[property.systemName];
                }
            });
            // Copy remaining old properties
            oldProperties.forEach(function (value, key) {
                _this.model.userProperties.push({ locked: false, systemName: key, value: value });
            });
        };
        DefineFeedPropertiesController.prototype.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip };
        };
        ;
        DefineFeedPropertiesController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        DefineFeedPropertiesController.prototype.ngOnInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
        };
        ;
        DefineFeedPropertiesController.$inject = ["$scope", "$http", "$mdToast", "RestUrlService", "FeedTagService", "FeedService"];
        return DefineFeedPropertiesController;
    }());
    exports.DefineFeedPropertiesController = DefineFeedPropertiesController;
    angular.module(moduleName).
        component("thinkbigDefineFeedProperties", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedPropertiesController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-properties.html',
    });
});
//# sourceMappingURL=DefineFeedPropertiesDirective.js.map