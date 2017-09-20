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

define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require:['thinkbigDefineFeedGeneralInfo','^thinkbigStepper'],
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
    }

    var controller =  function($scope,$log, $http,$mdToast,RestUrlService, FeedService, CategoriesService) {

        var self = this;

        /**
         * The angular form
         * @type {{}}
         */
        this.defineFeedGeneralForm = {};
        this.templates = [];
        this.model = FeedService.createFeedModel;
        this.isValid = false;
        this.stepNumber = parseInt(this.stepIndex)+1
        this.stepperController = null;

        // Contains existing system feed names for the current category
        this.existingFeedNames = {};

        this.categorySearchText = '';
        this.category;
        self.categorySelectedItemChange = selectedItemChange;
        self.categorySearchTextChanged = searchTextChange;
        self.categoriesService = CategoriesService;

        /**
         * are we populating the feed name list for validation
         * @type {boolean}
         */
        self.populatingExsitngFeedNames = false;

        function searchTextChange(text) {
         //   $log.info('Text changed to ' + text);
        }
        function selectedItemChange(item) {
            //only allow it if the category is there and the 'createFeed' flag is true
            if(item != null && item != undefined && item.createFeed) {
                self.model.category.name = item.name;
                self.model.category.id = item.id;
                self.model.category.systemName = item.systemName;
                setSecurityGroups(item.name);
                validateUniqueFeedName();

                if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['category']) {
                    self.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                }
            }
            else {
                self.model.category.name = null;
                self.model.category.id = null;
                self.model.category.systemName = null;
                if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['feedName']) {
                    self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                }

                if(item && item.createFeed == false){
                    if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['category']) {
                        self.defineFeedGeneralForm['category'].$setValidity('accessDenied', false);
                    }
                }
                else {
                    if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['category']) {
                        self.defineFeedGeneralForm['category'].$setValidity('accessDenied', true);
                    }
                }
            }
        }

        function existingFeedNameKey(categoryName, feedName) {
            return categoryName + "." + feedName;
        }

        populateExistingFeedNames();

        /**
         * updates the {@code existingFeedNames} object with the latest feed names from the server
         * @returns {promise}
         */
        function populateExistingFeedNames() {
            if(!self.populatingExsitngFeedNames) {
                self.populatingExsitngFeedNames = true;
                FeedService.getFeedNames().then()
                return $http.get(RestUrlService.OPS_MANAGER_FEED_NAMES).then(function (response) {
                    self.existingFeedNames = {};
                    if (response.data != null && response.data != null) {
                        angular.forEach(response.data, function (categoryAndFeed) {
                            var categoryName = categoryAndFeed.substr(0, categoryAndFeed.indexOf('.'));
                            var feedName = categoryAndFeed.substr(categoryAndFeed.indexOf('.')+1)
                            self.existingFeedNames[categoryAndFeed] = feedName;
                        });
                        self.populatingExsitngFeedNames = false;
                    }
                }, function () {
                    self.populatingExsitngFeedNames = false;
                });
            }
        }

        function validateUniqueFeedName() {

            function _validate() {
                //validate to ensure the name is unique in this category
                if (self.model && self.model.category && self.existingFeedNames[existingFeedNameKey(self.model.category.systemName, self.model.systemFeedName)]) {
                    if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['feedName']) {
                        self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', false);
                    }
                }
                else {
                    if (self.defineFeedGeneralForm && self.defineFeedGeneralForm['feedName']) {
                        self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
                    }
                }
            }

            if (self.model && self.model.id && self.model.id.length > 0) {
                self.defineFeedGeneralForm['feedName'].$setValidity('notUnique', true);
            } else if (_.isEmpty(self.existingFeedNames)) {
                if(!self.populatingExsitngFeedNames) {
                    populateExistingFeedNames().then(function () {
                        _validate();
                    });
                }
            } else {
                _validate();
            }

        }



      //  getRegisteredTemplates();

        function validate(){
           var valid = isNotEmpty(self.model.category.name) && isNotEmpty(self.model.feedName) && isNotEmpty(self.model.templateId);
            self.isValid = valid;
        }

        function setSecurityGroups(newVal) {
            if(newVal) {
                var category = self.categoriesService.findCategoryByName(newVal)
                if(category != null) {
                    var securityGroups = category.securityGroups;
                    self.model.securityGroups = securityGroups;
                }
            }
        }

        function isNotEmpty(item){
            return item != null && item != undefined && item != '';
        }

        this.onTemplateChange = function() {

        }

        $scope.$watch(function(){
            return self.model.id;
        },function(newVal){
            if(newVal == null && (angular.isUndefined(self.model.cloned) || self.model.cloned == false)) {
                self.category = null;
            }
            else {
                self.category = self.model.category;
            }
        })

       var feedNameWatch = $scope.$watch(function(){
            return self.model.feedName;
        },function(newVal) {
           FeedService.getSystemName(newVal).then(function (response) {
               self.model.systemFeedName = response.data;
               validateUniqueFeedName();
               validate();
           });

        });

        $scope.$watch(function(){
            return self.model.category.name;
        },function(newVal) {

            validate();
        })

      var templateIdWatch =  $scope.$watch(function(){
            return self.model.templateId;
        },function(newVal) {
            validate();
        });

        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        function getRegisteredTemplates() {
            var successFn = function (response) {
                self.templates = response.data;
            }
            var errorFn = function (err) {

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };

            $scope.$on('$destroy',function(){
                feedNameWatch();
                templateIdWatch();
                self.model = null;
            });

    };


    angular.module(moduleName).controller('DefineFeedGeneralInfoController', ["$scope","$log","$http","$mdToast","RestUrlService","FeedService","CategoriesService",controller]);

    angular.module(moduleName)
        .directive('thinkbigDefineFeedGeneralInfo', directive);

});
