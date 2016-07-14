/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-schedule.html',
            controller: "FeedScheduleController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $mdDialog, FeedService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.editModel = {};
        this.editableSection = false;


        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        })

        //if the Model doesnt support Preconditions dont allow it in the list
        var allScheduleStrategies = [{label: "Cron", value: "CRON_DRIVEN"}, {label: "Timer", value: "TIMER_DRIVEN"}, {label: "Trigger/Event", value: "TRIGGER_DRIVEN"}];

        function updateScheduleStrategies() {
            self.scheduleStrategies = allScheduleStrategies;
            if (!self.model.registeredTemplate.allowPreconditions) {
                self.scheduleStrategies = _.reject(allScheduleStrategies, function (strategy) {
                    return strategy.value == 'TRIGGER_DRIVEN';
                });
            }
        }

        updateScheduleStrategies();

        this.onScheduleStrategyChange = function() {
            if(self.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') {
                if(self.editModel.schedule.schedulingPeriod !="* * * * * ?" ) {
                    self.editModel.schedule.schedulingPeriod = "* * * * * ?";
                }
            }
            else if(self.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN'){
                self.editModel.schedule.schedulingPeriod = "5 min";
            }
        };


        this.onEdit = function(){
            //copy the model
            self.editModel.schedule = angular.copy(FeedService.editFeedModel.schedule);
        }

        this.onCancel = function() {

        }
        this.onSave = function (ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving Feed " + self.model.feedName, self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);
            copy.schedule = self.editModel.schedule;
            FeedService.saveFeedModel(copy).then(function (response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.schedule = self.editModel.schedule;
            }, function (response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        }

        this.deletePrecondition = function ($index) {
            if (self.editModel.schedule.preconditions != null) {
                self.editModel.schedule.preconditions.splice($index, 1);
            }
        }
        this.showPreconditionDialog = function (index) {
            $mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: 'js/define-feed/feed-details/feed-preconditions/define-feed-preconditions-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: self.editModel,
                    index: index
                }
            })
                .then(function (msg) {

                }, function () {

                });
        };


    };


    angular.module(MODULE_FEED_MGR).controller('FeedScheduleController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedSchedule', directive);

})();
