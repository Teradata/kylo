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

import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";;

export class FeedCreationErrorService {
    constructor (private $mdDialog:any, private $filter:any) {

        function parseNifiFeedForErrors(nifiFeed:any, errorMap:any) {
            var count = 0;

            if (nifiFeed != null) {

                if (nifiFeed.errorMessages != null && nifiFeed.errorMessages.length > 0) {
                    angular.forEach(nifiFeed.errorMessages, function (msg:any) {
                        errorMap['FATAL'].push({category: 'General', message: msg});
                        count++;
                    })
                }

                if (nifiFeed.feedProcessGroup != null) {
                    angular.forEach(nifiFeed.feedProcessGroup.errors, function (processor:any) {
                        if (processor.validationErrors) {
                            angular.forEach(processor.validationErrors, function (error:any) {
                                var copy:any = {};
                                angular.extend(copy, error);
                                angular.extend(copy, processor);
                                copy.validationErrors = null;
                                errorMap[error.severity].push(copy);
                                count++;
                            });
                        }
                    });
                }
                if (errorMap['FATAL'].length == 0) {
                    delete errorMap['FATAL'];
                }
                if (errorMap['WARN'].length == 0) {
                    delete errorMap['WARN'];
                }
            }
            return count;

        }

        function buildErrorMapAndSummaryMessage() {
            var count = 0;
            var errorMap:any = {"FATAL": [], "WARN": []};
            if (data.feedError.nifiFeed != null && data.feedError.response.status < 500) {

                count = parseNifiFeedForErrors(data.feedError.nifiFeed, errorMap);
                data.feedError.feedErrorsData = errorMap;
                data.feedError.feedErrorsCount = count;

                if (data.feedError.feedErrorsCount == 1) {

                    data.feedError.message = data.feedError.feedErrorsCount + $filter('translate')('views.FeedCreationErrorService.iiwf');
                    data.feedError.isValid = false;
                }
                else if (data.feedError.feedErrorsCount >=2  || data.feedError.feedErrorsCount <= 4) {

                    data.feedError.message = $filter('translate')('views.FeedCreationErrorService.Found') + data.feedError.feedErrorsCount + $filter('translate')('views.FeedCreationErrorService.iiwf2');
                    data.feedError.isValid = false;
                }
                else if (data.feedError.feedErrorsCount >= 5) {

                    data.feedError.message = $filter('translate')('views.FeedCreationErrorService.Found') + data.feedError.feedErrorsCount + $filter('translate')('views.FeedCreationErrorService.iiwf2');
                    data.feedError.isValid = false;
                }
                else {
                    data.feedError.isValid = true;
                }
            }
            else if (data.feedError.response.status === 502) {
                data.feedError.message = 'Error creating feed, bad gateway'
            } else if (data.feedError.response.status === 503) {
                data.feedError.message = 'Error creating feed, service unavailable'
            } else if (data.feedError.response.status === 504) {
                data.feedError.message = 'Error creating feed, gateway timeout'
            } else if (data.feedError.response.status === 504) {
                data.feedError.message = 'Error creating feed, HTTP version not supported'
            } else {
                data.feedError.message = 'Error creating feed.'
            }

        }

        function newErrorData() {
            return {
                isValid: false,
                hasErrors: false,
                feedName: '',
                nifiFeed: {},
                message: '',
                feedErrorsData: {},
                feedErrorsCount: 0
            };
        }

        var data:any = {
            feedError: {
                isValid: false,
                hasErrors: false,
                feedName: '',
                nifiFeed: {},
                message: '',
                feedErrorsData: {},
                feedErrorsCount: 0,
            },
            buildErrorData: function (feedName:any, response:any) {
                this.feedError.feedName = feedName;
                this.feedError.nifiFeed = response.data;
                this.feedError.response = response;
                buildErrorMapAndSummaryMessage();
                this.feedError.hasErrors = this.feedError.feedErrorsCount > 0;
            },
            parseNifiFeedErrors: function (nifiFeed:any, errorMap:any) {
                return parseNifiFeedForErrors(nifiFeed, errorMap);
            },
            reset: function () {
                angular.extend(this.feedError, newErrorData());
            },
            hasErrors: function () {
                return this.feedError.hasErrors;
            },
            showErrorDialog: function () {

                $mdDialog.show({
                    controller: 'FeedErrorDialogController',
                    templateUrl: 'js/feed-mgr/feeds/define-feed/feed-error-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {}
                }).then(function (msg:any) {
                    //respond to action in dialog if necessary... currently dont need to do anything
                }, function () {

                });
            }

        };
        return data;

    }
    
}


var controller = function ($scope:any, $mdDialog:any, FeedCreationErrorService:any) {
    var self = this;

    var errorData = FeedCreationErrorService.feedError;
    $scope.feedName = errorData.feedName;
    $scope.createdFeed = errorData.nifiFeed;
    $scope.isValid = errorData.isValid;
    $scope.message = errorData.message;
    $scope.feedErrorsData = errorData.feedErrorsData;
    $scope.feedErrorsCount = errorData.feedErrorsCount;

    $scope.fixErrors = function () {
        $mdDialog.hide('fixErrors');
    }

    $scope.hide = function () {
        $mdDialog.hide();
    };

    $scope.cancel = function () {
        $mdDialog.cancel();
    };

};

angular.module(moduleName).controller('FeedErrorDialogController',["$scope","$mdDialog","FeedCreationErrorService","$filter",controller]);

angular.module(moduleName).factory('FeedCreationErrorService',["$mdDialog","$filter", FeedCreationErrorService]);
