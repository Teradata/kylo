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

        this.timerAmount = 5;
        this.timerUnits = "min";
        this.isValid = false;
        self.scheduleFeedForm = {};


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

        function setTimerDriven() {
            self.editModel.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            self.timerAmount = 5;
            self.timerUnits = "min";
            self.editModel.schedule.schedulingPeriod = "5 min";
        }

        function setCronDriven() {
            self.editModel.schedule.schedulingStrategy = 'CRON_DRIVEN'
            self.editModel.schedule.schedulingPeriod = FeedService.DEFAULT_CRON;
        }

        function setDefaultScheduleStrategy() {
            if (self.editModel.inputProcessorType != '' && (self.editModel.schedule.schedulingStrategy.touched == false || self.editModel.schedule.schedulingStrategy.touched == undefined)) {
                if (self.editModel.inputProcessorType.indexOf("GetFile") >= 0) {
                    setTimerDriven();
                }
                else if (self.editModel.inputProcessorType.indexOf("GetTableData") >= 0) {
                    setCronDriven();
                }
            }
        }

        this.timerChanged = function () {
            if (self.timerAmount < 0) {
                self.timerAmount = null;
            }
            if (self.timerAmount != null && (self.timerAmount == 0 || (self.timerAmount < 3 && self.timerUnits == 'sec'))) {
                self.showTimerAlert();
            }
            self.editModel.schedule.schedulingPeriod = self.timerAmount + " " + self.timerUnits;
            validate();

            //!warn if < 5 seconds
        }

        function validate() {
            //cron expression validation is handled via the cron-expression validator
            var valid = (self.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') ||
                        (self.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN' && self.timerAmount != undefined && self.timerAmount != null) ||
                        (self.editModel.schedule.schedulingStrategy == 'TRIGGER_DRIVEN' && self.editModel.schedule.preconditions != null && self.editModel.schedule.preconditions.length > 0 );
            self.isValid = valid;
        }

        self.showTimerAlert = function (ev) {
            $mdDialog.show(
                $mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title('Warning. Rapid Timer')
                    .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                    .ariaLabel('Warning Fast Timer')
                    .ok('Got it!')
                    .targetEvent(ev)
            );
        };


        updateScheduleStrategies();

        this.onScheduleStrategyChange = function() {
            if(self.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') {
                if (self.editModel.schedule.schedulingPeriod != FeedService.DEFAULT_CRON) {
                    setCronDriven();
                }
            }
            else if(self.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN'){
                setTimerDriven();
            }
        };


        this.onEdit = function(){
            //copy the model
            self.editModel.schedule = angular.copy(FeedService.editFeedModel.schedule);
            self.editModel.inputProcessorType = FeedService.editFeedModel.inputProcessorType;
            validate();
        }

        this.onCancel = function() {

        }
        this.onSave = function (ev) {
            var isValid = validate();
            if (isValid) {
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
                    validate();
                }, function () {

                });
        };


    };


    angular.module(MODULE_FEED_MGR).controller('FeedScheduleController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedSchedule', directive);

})();
