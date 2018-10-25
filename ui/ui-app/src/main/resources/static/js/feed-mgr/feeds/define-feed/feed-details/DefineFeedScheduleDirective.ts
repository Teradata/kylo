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
const moduleName = require('../module-name');

export class DefineFeedScheduleController {
    stepperController: any;
    /**
         * The stepperController will be accessible shortly after this controller is created.
         * This indicates the amount of time it should wait in an attempt to wire itself with the controller
         * @type {number}
         */
    waitForStepperControllerRetryAmount: number = 0;
    /**
         * Reference to this step number
         * @type {number}
    */
    stepNumber: number;
    model: any;
    /**
         * The Timer amount with default
         * @type {number}
         */
    timerAmount: number = 5;
    /**
         * the timer units with default
         * @type {string}
         */
    timerUnits: string = "min";
    /**
         * flag indicates the data is valid
         * @type {boolean}
         */
    isValid: boolean = false;
    /**
         * the angular form
         * @type {{}}
         */
    defineFeedScheduleForm: any;
    /**
         * The object that is populated after the Feed is created and returned from the server
         * @type {null}
         */
    createdFeed: any = null;
    /**
         * Indicates if any errors exist from the server  upon saving
         * @type {Array}
         */
    feedErrorsData: any = [];
    /**
         * reference to error count so the UI can show it
         * @type {number}
         */
    feedErrorsCount: number = 0;
    /**
         * Indicates that NiFi is clustered.
         *
         * @type {boolean}
         */
    isClustered: boolean = true;
    savingFeed: boolean = false;
    /**
         * Indicates that NiFi supports the execution node property.
         * @type {boolean}
         */
    supportsExecutionNode: boolean = true;
    scheduleStrategies: any;
    stepIndex: any;
    totalSteps: number;

    $onInit() {
        this.ngOnInit();
    }
    
    ngOnInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    static readonly $inject = ["$scope", "$http", "$mdDialog", "$timeout", "RestUrlService", "FeedService", "StateService",
        "StepperService", "CategoriesService", "BroadcastService", "$filter", "FeedCreationErrorService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private $mdDialog: angular.material.IDialogService, private $timeout: angular.ITimeoutService
        , private RestUrlService: any, private FeedService: any, private StateService: any
        , private StepperService: any, private CategoriesService: any, private BroadcastService: any
        , private $filter: angular.IFilterService, private FeedCreationErrorService: any) {
        /**
         * Get notified when a step is changed/becomes active
         */
        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, (event: any, index: any) => {
            if (index == parseInt(this.stepIndex)) {

                this.updateScheduleStrategies();
                //make sure the selected strategy is valid

                this.setDefaultScheduleStrategy();
            }
        });
        /**
         * get notified when any step changes its state (becomes enabled/disabled)
         * This is needed to block out the save button if a step is invalid/disabled
         */
        BroadcastService.subscribe($scope, StepperService.STEP_STATE_CHANGED_EVENT, (event: any, index: any) => this.validate());
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
        $http.get(RestUrlService.NIFI_STATUS).then( (response: any) => {
            this.isClustered = (angular.isDefined(response.data.clustered) && response.data.clustered);
            this.supportsExecutionNode = (angular.isDefined(response.data.version) && !response.data.version.match(/^0\.|^1\.0/));
            this.updateScheduleStrategies();
        });
    }
    /**
         * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
         */
        timerChanged() {
            if (this.timerAmount < 0) {
                this.timerAmount = null;
            }
            if (!this.model.isStream && this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
                this.showTimerAlert();
            }
            this.model.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
            this.validate();
        };

        showTimerAlert() {
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title('Warning. Rapid Timer')
                    .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                    .ariaLabel('Warning Fast Timer')
                    .ok('Got it!')
                    // .targetEvent(ev)   %%%% Need To discuss this with greg %%%%%
            );
        };

        /**
         * When the strategy changes ensure the defaults are set
         */
        onScheduleStrategyChange() {
            this.model.schedule.schedulingStrategyTouched = true;
            if (this.model.schedule.schedulingStrategy == "CRON_DRIVEN") {
                if (this.model.schedule.schedulingPeriod != this.FeedService.DEFAULT_CRON) {
                    this.setCronDriven();
                }
            } else if (this.model.schedule.schedulingStrategy == "TIMER_DRIVEN") {
                this.setTimerDriven();
            } else if (this.model.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY") {
                if (this.supportsExecutionNode) {
                    this.setTimerDriven();
                    this.model.schedule.schedulingStrategy = "PRIMARY";
                } else {
                    this.setPrimaryNodeOnly();
                }
            }
            this.validate();
        };
        deletePrecondition($index: any) {
            if (this.model.schedule.preconditions != null) {
                this.model.schedule.preconditions.splice($index, 1);
            }
        };

        showPreconditionDialog(index: any) {
            if (index == undefined) {
                index = null;
            }
            this.$mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: '../../shared/define-feed-preconditions-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: this.model,
                    index: index
                }
            }).then(() =>{
                this.validate();
            });
        };
    /**
    * attempt to wire the stepper controller references
    * @param callback
    */
    waitForStepperController(model:DefineFeedScheduleController, callback: any) {
        if (model.stepperController) {
            model.waitForStepperControllerRetryAmount = 0;
            callback();
        }
        else {
            if (model.waitForStepperControllerRetryAmount < 20) {
                model.waitForStepperControllerRetryAmount++;
                model.$timeout(() =>{
                    model.waitForStepperController(model,callback)
                }, 10);
            }
        }
    }
    /**
    * validate the inputs and model data
    */
    validate() {
        //cron expression validation is handled via the cron-expression validator
        var valid = (this.model.schedule.schedulingStrategy == "CRON_DRIVEN") ||
            (this.model.schedule.schedulingStrategy == "TIMER_DRIVEN" && this.timerAmount != undefined && this.timerAmount != null) ||
            (this.model.schedule.schedulingStrategy == "TRIGGER_DRIVEN" && this.model.schedule.preconditions != null && this.model.schedule.preconditions.length > 0) ||
            (this.model.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY" && this.timerAmount != undefined && this.timerAmount != null);
        if (valid) {
            this.waitForStepperController(this,() => {
                //since the access control step can be disabled, we care about everything before that step, so we will check the step prior to this step
                this.isValid = this.stepperController.arePreviousStepsComplete(this.stepIndex - 1)
            });

        }
        else {
            this.isValid = valid;
        }
    }
    /**
       * Show activity
       */
    showProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = true;
        }
    }

    /**
     * hide progress activity
     */
    hideProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = false;
        }
    }

    /**
         * Create the feed, save it to the server, populate the {@code createdFeed} object upon save
         */
    createFeed() {
        if (this.defineFeedScheduleForm.$valid) {
            this.savingFeed = true;
            this.showProgress();

            this.createdFeed = null;

            this.FeedService.saveFeedModel(this.model).then((response: any) => {
                this.createdFeed = response.data;
                this.savingFeed = false;
                this.StateService.FeedManager().Feed().navigateToDefineFeedComplete(this.createdFeed, null);

                //  this.showCompleteDialog();
            }, (response: any) => {
                this.savingFeed = false;
                this.createdFeed = response.data;
                this.FeedCreationErrorService.buildErrorData(this.model.feedName, response);
                this.hideProgress();
                this.FeedCreationErrorService.showErrorDialog();
            });
        }
    };
    /**
         * Different templates have different schedule strategies.
         * Filter out those that are not needed based upon the template
         */
    updateScheduleStrategies() {
        // Filter schedule strategies
        var allowPreconditions = (this.model.allowPreconditions && this.model.inputProcessorType.indexOf("TriggerFeed") >= 0);
        /**
         * All possible schedule strategies
         * @type {*[]}
         */
        var allScheduleStrategies = [{ label: this.$filter('translate')('views.DefineFeedScheduleDirective.Cron'), value: "CRON_DRIVEN" }, { label: this.$filter('translate')('views.DefineFeedScheduleDirective.Timer'), value: "TIMER_DRIVEN" }, { label: this.$filter('translate')('views.DefineFeedScheduleDirective.T/E'), value: "TRIGGER_DRIVEN" },
        { label: "On primary node", value: "PRIMARY_NODE_ONLY" }];
        this.scheduleStrategies = _.filter(allScheduleStrategies, (strategy: any) => {
            if (allowPreconditions) {
                return (strategy.value === "TRIGGER_DRIVEN");
            } else if (strategy.value === "PRIMARY_NODE_ONLY") {
                return this.isClustered && !this.supportsExecutionNode;
            } else {
                return (strategy.value !== "TRIGGER_DRIVEN");
            }
        });
        var self = this;
        // Check if last strategy is valid
        if (this.model.schedule.schedulingStrategy) {
            var validStrategy = _.some(this.scheduleStrategies, (strategy: any) => {
                return strategy.value == self.model.schedule.schedulingStrategy;
            });
            if (!validStrategy) {
                self.model.schedule.schedulingStrategyTouched = false;
            }
        }
    }

    /**
     * Force the model and timer to be set to Timer with the defaults
     */
    setTimerDriven() {
        this.model.schedule.schedulingStrategy = 'TIMER_DRIVEN';
        this.timerAmount = 5;
        this.timerUnits = "min";
        this.model.schedule.schedulingPeriod = "5 min";
    }
    
        setStreamTimerDriven() {
            this.model.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            this.timerAmount = 0;
            this.timerUnits = "sec";
            this.model.schedule.schedulingPeriod = "0 sec";
        }

    /**
     * Force the model to be set to Cron
     */
    setCronDriven() {
        this.model.schedule.schedulingStrategy = 'CRON_DRIVEN';
        this.model.schedule.schedulingPeriod = this.FeedService.DEFAULT_CRON;
    }

    /**
     * Force the model to be set to Triggger
     */
    setTriggerDriven() {
        this.model.schedule.schedulingStrategy = 'TRIGGER_DRIVEN'
    }

    /**
     * Set the scheduling strategy to 'On primary node'.
     */
    setPrimaryNodeOnly() {
        this.model.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
        this.timerAmount = 5;
        this.timerUnits = "min";
        this.model.schedule.schedulingPeriod = "5 min";
    }

    setDefaultScheduleStrategy() {
        if (angular.isUndefined(this.model.cloned) || this.model.cloned == false) {
            if (this.model.inputProcessorType != '' && (this.model.schedule.schedulingStrategyTouched == false || this.model.schedule.schedulingStrategyTouched == undefined)) {
                    if(angular.isDefined(this.model.isStream) && this.model.isStream) {
                        this.setStreamTimerDriven();
                    }
                    else if (this.model.inputProcessorType.indexOf("GetFile") >= 0) {
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
        } else {
            var split = this.model.schedule.schedulingPeriod.split(' ');
            this.timerAmount = split[0];
            this.timerUnits = split[1];

        }
    }

}
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
        templateUrl: './define-feed-schedule.html',
    });