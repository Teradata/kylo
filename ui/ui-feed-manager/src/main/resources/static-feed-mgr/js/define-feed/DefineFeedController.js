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
(function() {

    var controller = function($scope, $stateParams, $http, AccessControlService, FeedService, RestUrlService, StateService) {

        var self = this;

        /**
         * Indicates if feeds may be imported from an archive.
         * @type {boolean}
         */
        self.allowImport = false;

        //     StateService.navigateToFeeds();

        this.layout = 'first'
        this.stepperUrl = null;
        this.totalSteps = null;
        this.template = null;
        self.model = FeedService.createFeedModel;

        var self = this;
        self.allTemplates = [];
        self.firstTemplates = [];
        self.displayMoreLink = false;
        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        function getRegisteredTemplates() {
            var successFn = function(response) {

                if (response.data) {

                    var data = _.chain(response.data).filter(function (template) {
                        return template.state == 'ENABLED'
                    }).sortBy('order')
                            .value();

                    if (data.length > 1) {
                        self.displayMoreLink = true;
                    }
                    self.allTemplates = data;
                    self.firstTemplates = _.first(data, 3);

                }

            }
            var errorFn = function(err) {

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };

        this.more = function() {
            this.layout = 'all';
        }

        this.gotoImportFeed = function() {
            StateService.navigatetoImportFeed();
        }
        getRegisteredTemplates();

        this.selectTemplate = function(template) {
            self.model.templateId = template.id;
            self.model.templateName = template.templateName;
            //setup some initial data points for the template
            self.model.defineTable = template.defineTable;
            self.model.allowPreconditions = template.allowPreconditions;
            self.model.dataTransformationFeed = template.dataTransformation;
            if (template.defineTable) {
                self.totalSteps = 6;
                self.stepperUrl = 'js/define-feed/define-feed-stepper.html'
            }
            else if (template.dataTransformation) {
                self.totalSteps = 8;
                self.stepperUrl = 'js/define-feed/define-feed-data-transform-stepper.html'
            }
            else {
                self.totalSteps = 4;
                self.stepperUrl = 'js/define-feed/define-feed-no-table-stepper.html'
            }
        }

        self.cancelStepper = function() {
            //or just reset the url
            FeedService.resetFeed();
            self.stepperUrl = null;
            //StateService.navigateToFeeds();
        }

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowImport= AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions);
                });
    };

    angular.module(MODULE_FEED_MGR).controller('DefineFeedController', controller);

}());
