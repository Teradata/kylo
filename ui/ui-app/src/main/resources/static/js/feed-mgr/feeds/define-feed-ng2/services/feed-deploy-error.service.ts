import * as angular from "angular"
import {TdDialogService} from "@covalent/core";
import {FeedNifiErrorUtil} from '../../../services/feed-nifi-error-util';

export class FeedDeployErrorService {
    constructor (private _dialogService:TdDialogService, $mdDialog:any, $filter: any) {


        function buildErrorMapAndSummaryMessage() {
            var count = 0;
            var errorMap:any = {"FATAL": [], "WARN": []};
            if (data.feedError.nifiFeed != null && data.feedError.response.status < 500) {

                count = data.parseNifiFeedForErrors(data.feedError.nifiFeed, errorMap);
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
                return FeedNifiErrorUtil.parseNifiFeedForErrors(nifiFeed, errorMap);
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