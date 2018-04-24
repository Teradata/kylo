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
    var DefineFeedScheduleController = /** @class */ (function () {
        function DefineFeedScheduleController($scope, $http, $mdDialog, $timeout, RestUrlService, FeedService, StateService, StepperService, CategoriesService, BroadcastService, $filter, FeedCreationErrorService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$timeout = $timeout;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.StateService = StateService;
            this.StepperService = StepperService;
            this.CategoriesService = CategoriesService;
            this.BroadcastService = BroadcastService;
            this.$filter = $filter;
            this.FeedCreationErrorService = FeedCreationErrorService;
            /**
                 * The stepperController will be accessible shortly after this controller is created.
                 * This indicates the amount of time it should wait in an attempt to wire itself with the controller
                 * @type {number}
                 */
            this.waitForStepperControllerRetryAmount = 0;
            /**
                 * The Timer amount with default
                 * @type {number}
                 */
            this.timerAmount = 5;
            /**
                 * the timer units with default
                 * @type {string}
                 */
            this.timerUnits = "min";
            /**
                 * flag indicates the data is valid
                 * @type {boolean}
                 */
            this.isValid = false;
            /**
                 * The object that is populated after the Feed is created and returned from the server
                 * @type {null}
                 */
            this.createdFeed = null;
            /**
                 * Indicates if any errors exist from the server  upon saving
                 * @type {Array}
                 */
            this.feedErrorsData = [];
            /**
                 * reference to error count so the UI can show it
                 * @type {number}
                 */
            this.feedErrorsCount = 0;
            /**
                 * Indicates that NiFi is clustered.
                 *
                 * @type {boolean}
                 */
            this.isClustered = true;
            this.savingFeed = false;
            /**
                 * Indicates that NiFi supports the execution node property.
                 * @type {boolean}
                 */
            this.supportsExecutionNode = true;
            /**
             * Get notified when a step is changed/becomes active
             */
            BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, function (event, index) {
                if (index == parseInt(_this.stepIndex)) {
                    _this.updateScheduleStrategies();
                    //make sure the selected strategy is valid
                    _this.setDefaultScheduleStrategy();
                }
            });
            /**
             * get notified when any step changes its state (becomes enabled/disabled)
             * This is needed to block out the save button if a step is invalid/disabled
             */
            BroadcastService.subscribe($scope, StepperService.STEP_STATE_CHANGED_EVENT, function (event, index) { return _this.validate(); });
            /**
             * The model
             */
            this.model = FeedService.createFeedModel;
            /**
             * update the default strategies in the list
             */
            this.updateScheduleStrategies();
            /**
             * Validate the form
             */
            this.validate();
            // Detect if NiFi is clustered
            $http.get(RestUrlService.NIFI_STATUS).then(function (response) {
                this.isClustered = (angular.isDefined(response.data.clustered) && response.data.clustered);
                this.supportsExecutionNode = (angular.isDefined(response.data.version) && !response.data.version.match(/^0\.|^1\.0/));
                this.updateScheduleStrategies();
            });
        }
        DefineFeedScheduleController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        DefineFeedScheduleController.prototype.ngOnInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
        };
        /**
             * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
             */
        DefineFeedScheduleController.prototype.timerChanged = function () {
            if (this.timerAmount < 0) {
                this.timerAmount = null;
            }
            if (this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
                this.showTimerAlert();
            }
            this.model.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
            this.validate();
        };
        ;
        DefineFeedScheduleController.prototype.showTimerAlert = function () {
            this.$mdDialog.show(this.$mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title('Warning. Rapid Timer')
                .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                .ariaLabel('Warning Fast Timer')
                .ok('Got it!')
            // .targetEvent(ev)   %%%% Need To discuss this with greg %%%%%
            );
        };
        ;
        /**
         * When the strategy changes ensure the defaults are set
         */
        DefineFeedScheduleController.prototype.onScheduleStrategyChange = function () {
            this.model.schedule.schedulingStrategyTouched = true;
            if (this.model.schedule.schedulingStrategy == "CRON_DRIVEN") {
                if (this.model.schedule.schedulingPeriod != this.FeedService.DEFAULT_CRON) {
                    this.setCronDriven();
                }
            }
            else if (this.model.schedule.schedulingStrategy == "TIMER_DRIVEN") {
                this.setTimerDriven();
            }
            else if (this.model.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY") {
                if (this.supportsExecutionNode) {
                    this.setTimerDriven();
                    this.model.schedule.schedulingStrategy = "PRIMARY";
                }
                else {
                    this.setPrimaryNodeOnly();
                }
            }
            this.validate();
        };
        ;
        DefineFeedScheduleController.prototype.deletePrecondition = function ($index) {
            if (this.model.schedule.preconditions != null) {
                this.model.schedule.preconditions.splice($index, 1);
            }
        };
        ;
        DefineFeedScheduleController.prototype.showPreconditionDialog = function (index) {
            if (index == undefined) {
                index = null;
            }
            this.$mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: 'js/feed-mgr/feeds/shared/define-feed-preconditions-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: this.model,
                    index: index
                }
            }).then(function () {
                this.validate();
            });
        };
        ;
        /**
        * attempt to wire the stepper controller references
        * @param callback
        */
        DefineFeedScheduleController.prototype.waitForStepperController = function (model, callback) {
            if (model.stepperController) {
                model.waitForStepperControllerRetryAmount = 0;
                callback();
            }
            else {
                if (model.waitForStepperControllerRetryAmount < 20) {
                    model.waitForStepperControllerRetryAmount++;
                    model.$timeout(function () {
                        model.waitForStepperController(model, callback);
                    }, 10);
                }
            }
        };
        /**
        * validate the inputs and model data
        */
        DefineFeedScheduleController.prototype.validate = function () {
            var _this = this;
            //cron expression validation is handled via the cron-expression validator
            var valid = (this.model.schedule.schedulingStrategy == "CRON_DRIVEN") ||
                (this.model.schedule.schedulingStrategy == "TIMER_DRIVEN" && this.timerAmount != undefined && this.timerAmount != null) ||
                (this.model.schedule.schedulingStrategy == "TRIGGER_DRIVEN" && this.model.schedule.preconditions != null && this.model.schedule.preconditions.length > 0) ||
                (this.model.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY" && this.timerAmount != undefined && this.timerAmount != null);
            if (valid) {
                this.waitForStepperController(this, function () {
                    //since the access control step can be disabled, we care about everything before that step, so we will check the step prior to this step
                    _this.isValid = _this.stepperController.arePreviousStepsComplete(_this.stepIndex - 1);
                });
            }
            else {
                this.isValid = valid;
            }
        };
        /**
           * Show activity
           */
        DefineFeedScheduleController.prototype.showProgress = function () {
            if (this.stepperController) {
                this.stepperController.showProgress = true;
            }
        };
        /**
         * hide progress activity
         */
        DefineFeedScheduleController.prototype.hideProgress = function () {
            if (this.stepperController) {
                this.stepperController.showProgress = false;
            }
        };
        /**
             * Create the feed, save it to the server, populate the {@code createdFeed} object upon save
             */
        DefineFeedScheduleController.prototype.createFeed = function () {
            if (this.defineFeedScheduleForm.$valid) {
                this.savingFeed = true;
                this.showProgress();
                this.createdFeed = null;
                this.FeedService.saveFeedModel(this.model).then(function (response) {
                    this.createdFeed = response.data;
                    this.savingFeed = false;
                    this.StateService.FeedManager().Feed().navigateToDefineFeedComplete(this.createdFeed, null);
                    //  this.showCompleteDialog();
                }, function (response) {
                    this.savingFeed = false;
                    this.createdFeed = response.data;
                    this.FeedCreationErrorService.buildErrorData(this.model.feedName, response);
                    this.hideProgress();
                    this.FeedCreationErrorService.showErrorDialog();
                });
            }
        };
        ;
        /**
             * Different templates have different schedule strategies.
             * Filter out those that are not needed based upon the template
             */
        DefineFeedScheduleController.prototype.updateScheduleStrategies = function () {
            var _this = this;
            // Filter schedule strategies
            var allowPreconditions = (this.model.allowPreconditions && this.model.inputProcessorType.indexOf("TriggerFeed") >= 0);
            /**
             * All possible schedule strategies
             * @type {*[]}
             */
            var allScheduleStrategies = [{ label: this.$filter('translate')('views.DefineFeedScheduleDirective.Cron'), value: "CRON_DRIVEN" }, { label: this.$filter('translate')('views.DefineFeedScheduleDirective.Timer'), value: "TIMER_DRIVEN" }, { label: this.$filter('translate')('views.DefineFeedScheduleDirective.T/E'), value: "TRIGGER_DRIVEN" },
                { label: "On primary node", value: "PRIMARY_NODE_ONLY" }];
            this.scheduleStrategies = _.filter(allScheduleStrategies, function (strategy) {
                if (allowPreconditions) {
                    return (strategy.value === "TRIGGER_DRIVEN");
                }
                else if (strategy.value === "PRIMARY_NODE_ONLY") {
                    return _this.isClustered && !_this.supportsExecutionNode;
                }
                else {
                    return (strategy.value !== "TRIGGER_DRIVEN");
                }
            });
            var self = this;
            // Check if last strategy is valid
            if (this.model.schedule.schedulingStrategy) {
                var validStrategy = _.some(this.scheduleStrategies, function (strategy) {
                    return strategy.value == self.model.schedule.schedulingStrategy;
                });
                if (!validStrategy) {
                    self.model.schedule.schedulingStrategyTouched = false;
                }
            }
        };
        /**
         * Force the model and timer to be set to Timer with the defaults
         */
        DefineFeedScheduleController.prototype.setTimerDriven = function () {
            this.model.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.model.schedule.schedulingPeriod = "5 min";
        };
        /**
         * Force the model to be set to Cron
         */
        DefineFeedScheduleController.prototype.setCronDriven = function () {
            this.model.schedule.schedulingStrategy = 'CRON_DRIVEN';
            this.model.schedule.schedulingPeriod = this.FeedService.DEFAULT_CRON;
        };
        /**
         * Force the model to be set to Triggger
         */
        DefineFeedScheduleController.prototype.setTriggerDriven = function () {
            this.model.schedule.schedulingStrategy = 'TRIGGER_DRIVEN';
        };
        /**
         * Set the scheduling strategy to 'On primary node'.
         */
        DefineFeedScheduleController.prototype.setPrimaryNodeOnly = function () {
            this.model.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.model.schedule.schedulingPeriod = "5 min";
        };
        DefineFeedScheduleController.prototype.setDefaultScheduleStrategy = function () {
            if (angular.isUndefined(this.model.cloned) || this.model.cloned == false) {
                if (this.model.inputProcessorType != '' && (this.model.schedule.schedulingStrategyTouched == false || this.model.schedule.schedulingStrategyTouched == undefined)) {
                    if (this.model.inputProcessorType.indexOf("GetFile") >= 0) {
                        this.setTimerDriven();
                    }
                    else if (this.model.inputProcessorType.indexOf("GetTableData") >= 0) {
                        this.setCronDriven();
                    }
                    else if (this.model.inputProcessorType.indexOf("TriggerFeed") >= 0) {
                        this.setTriggerDriven();
                    }
                    this.model.schedule.schedulingStrategyTouched = true;
                }
                else if (this.model.schedule.schedulingPeriod != '') {
                    var split = this.model.schedule.schedulingPeriod.split(' ');
                    this.timerAmount = split[0];
                    this.timerUnits = split[1];
                }
            }
            else {
                var split = this.model.schedule.schedulingPeriod.split(' ');
                this.timerAmount = split[0];
                this.timerUnits = split[1];
            }
        };
        DefineFeedScheduleController.$inject = ["$scope", "$http", "$mdDialog", "$timeout", "RestUrlService", "FeedService", "StateService",
            "StepperService", "CategoriesService", "BroadcastService", "$filter", "FeedCreationErrorService"];
        return DefineFeedScheduleController;
    }());
    exports.DefineFeedScheduleController = DefineFeedScheduleController;
    angular.module(moduleName).
        component("thinkbigDefineFeedSchedule", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedScheduleController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-schedule.html',
    });
});
//# sourceMappingURL=DefineFeedScheduleDirective.js.map