import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
        },
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-schedule.html',
        controller: "FeedScheduleController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
}


export class FeedScheduleController implements ng.IComponentController {
// define(['angular','feed-mgr/feeds/edit-feed/module-name','pascalprecht.translate'], function (angular,moduleName) {


    // var self = this;
    
    /**
     * Indicates if the feed schedule may be edited.
     * @type {boolean}
     */
    allowEdit:boolean = false;

    /**
     * The data model for the feed
     * @type {data.editFeedModel|{}|*}
     */
    model:any = this.FeedService.editFeedModel;

    /**
     * The model with only the Schedule data that is populated via the {@code this#onEdit()} method
     * @type {{}}
     */
    editModel:any = {};

    editableSection:boolean = false;

    /**
     * The Timer amount with default
     * @type {number}
     */
    timerAmount:number = 5;
    /**
     * the timer units with default
     * @type {string}
     */
    timerUnits:string = "min";

    /**
     * flag to indicate if the inputs are valid
     * @type {boolean}
     */
    isValid:boolean = false;

    /**
     * the Angular form for validation
     * @type {{}}
     */
    scheduleFeedForm:any = {};

    /**
     * Indicates that NiFi is clustered.
     *
     * @type {boolean}
     */
    isClustered:boolean = true;

    /**
     * Indicates that NiFi supports the execution node property.
     * @type {boolean}
     */
    supportsExecutionNode:boolean = true;
     /**
     * All possible schedule strategies
     * @type {*[]}
     */
    allScheduleStrategies:any = [{label: this.$filter('translate')('views.feed-schedule.Cron'), value: "CRON_DRIVEN"}, {label: this.$filter('translate')('views.feed-schedule.Timer'), value: "TIMER_DRIVEN"}, {label: this.$filter('translate')('views.feed-schedule.TE'), value: "TRIGGER_DRIVEN"},
    {label: "On primary node", value: "PRIMARY_NODE_ONLY"}];

    
    /**
     * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
     */
    timerChanged = function () {
        if (this.timerAmount < 0) {
            this.timerAmount = null;
        }
        if (this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
            this.showTimerAlert();
        }
        this.editModel.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
        this.validate();
    }

    showTimerAlert = function (ev:any) {
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title('Warning. Rapid Timer')
                .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                .ariaLabel('Warning Fast Timer')
                .ok('Got it!')
                .targetEvent(ev)
        );
    };

        /**
     * When the strategy changes ensure the defaults are set
     */
    onScheduleStrategyChange = function() {
        if(this.editModel.schedule.schedulingStrategy == "CRON_DRIVEN") {
            if (this.editModel.schedule.schedulingPeriod != this.FeedService.DEFAULT_CRON) {
                this.setCronDriven();
            }
        } else if(this.editModel.schedule.schedulingStrategy == "TIMER_DRIVEN") {
            this.setTimerDriven();
        } else if(this.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY") {
            this.setPrimaryNodeOnly();
        }
    };

    /**
     * Different templates have different schedule strategies.
     * Filter out those that are not needed based upon the template
     */
    updateScheduleStrategies = function() {
        // Filter schedule strategies
        this.scheduleStrategies = _.filter(this.allScheduleStrategies, (strategy:any) => {
            if (this.model.registeredTemplate.allowPreconditions) {
                return (strategy.value === "TRIGGER_DRIVEN");
            } else if (strategy.value === "PRIMARY_NODE_ONLY") {
                return (this.isClustered && !this.supportsExecutionNode);
            } else {
                return (strategy.value !== "TRIGGER_DRIVEN");
            }
        });
    }

    /**
     * Called when editing this section
     * copy the model to the {@code editModel} object
     */
    onEdit = function(){
        //copy the model
        this.editModel.category = {systemName: this.FeedService.editFeedModel.category.systemName};
        this.editModel.systemFeedName = this.FeedService.editFeedModelsystemFeedName;
        this.editModel.schedule = angular.copy(this.FeedService.editFeedModel.schedule);
        this.editModel.inputProcessorType = this.FeedService.editFeedModel.inputProcessorType;
        if (this.editModel.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY" && (!this.isClustered || this.supportsExecutionNode)) {
            this.editModel.schedule.schedulingStrategy = "TIMER_DRIVEN";
            if (this.supportsExecutionNode) {
                this.editModel.schedule.executionNode = "PRIMARY";
            }
        }
        if (this.editModel.schedule.schedulingStrategy == "TIMER_DRIVEN" || this.editModel.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY") {
            this.parseTimer();
        }
        if (this.isClustered && (!angular.isString(this.editModel.schedule.executionNode) || this.editModel.schedule.executionNode.length === 0)) {
            this.editModel.schedule.executionNode = "ALL";
        }
        this.validate();
    };

    onCancel = function() {
        
    }
    /**
     * When saving copy the editModel and save it
     * @param ev
     */
    onSave = function (ev:any) {
        var isValid = this.validate();
        if (isValid) {
            //save changes to the model
            this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-schedule.Saving'), this.model.feedName);
            var copy = angular.copy(this.FeedService.editFeedModel);
            copy.schedule = this.editModel.schedule;
            copy.userProperties = null;
            this.FeedService.saveFeedModel(copy).then((response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.editableSection = false;
                //save the changes back to the model
                this.model.schedule = this.editModel.schedule;
            }, (response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.FeedService.buildErrorData(this.model.feedName, response);
                this.FeedService.showFeedErrorsDialog();
                //make it editable
                this.editableSection = true;
            });
        }
    }

    /**
     * Remove the precondition from the schedule
     * @param $index
     */
    deletePrecondition = ($index:any) => {
        if (this.editModel.schedule.preconditions != null) {
            this.editModel.schedule.preconditions.splice($index, 1);
        }
    }

    /**
     * show the dialog allowing users to modify/add preconditions
     * @param index
     */
    showPreconditionDialog = (index:any) => {
        this.$mdDialog.show({
            controller: 'FeedPreconditionsDialogController',
            templateUrl: 'js/feed-mgr/feeds/shared/define-feed-preconditions-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                feed: this.editModel,
                index: index
            }
        })
            .then((msg:any) => {
                this.validate();
            }, () => {

            });
    };

    /**
     * Validates the inputs are good
     * @returns {*}
     */
     validate = function() {
            //cron expression validation is handled via the cron-expression validator
            var valid = (this.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') ||
                        (this.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN' && this.timerAmount != undefined && this.timerAmount != null) ||
                        (this.editModel.schedule.schedulingStrategy == 'TRIGGER_DRIVEN' && this.editModel.schedule.preconditions != null && this.editModel.schedule.preconditions.length > 0 ) ||
                        (this.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY" && this.timerAmount != undefined && this.timerAmount != null);
                        this.isValid = valid && this.scheduleFeedForm.$valid;
            return this.isValid;
        }


        
    constructor (private $scope:any, private $http:any, private $mdDialog:any, private $q:any,private AccessControlService:any
        , private EntityAccessControlService:any,private FeedService:any, private RestUrlService:any, private $filter:any) {
    
            /**
             * Watch the model and update it if not set.
             */
            $scope.$watch(() => {
                return FeedService.editFeedModel;
            },(newVal:any) => {
                //only update the model if it is not set yet
                if(this.model == null) {
                    this.model = FeedService.editFeedModel;
                }
            });

           

        /**
         * The model stores the timerAmount and timerUnits together as 1 string.
         * This will parse that string and set each component in the controller
         */
        function parseTimer() {
            this.timerAmount = parseInt(this.editModel.schedule.schedulingPeriod);
            var startIndex = this.editModel.schedule.schedulingPeriod.indexOf(" ");
            if (startIndex != -1) {
                this.timerUnits = this.editModel.schedule.schedulingPeriod.substring(startIndex + 1);
            }
        }

        /**
         * Force the model and timer to be set to Timer with the defaults
         */
        function setTimerDriven() {
            this.editModel.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.editModel.schedule.schedulingPeriod = "5 min";
        }

        /**
         * Force the model to be set to Cron
         */
        function setCronDriven() {
            this.editModel.schedule.schedulingStrategy = 'CRON_DRIVEN'
            this.editModel.schedule.schedulingPeriod = FeedService.DEFAULT_CRON;
        }

        /**
         * Force the model to be set to Triggger
         */
        function setTriggerDriven() {
            this.editModel.schedule.schedulingStrategy = 'TRIGGER_DRIVEN'
        }

        /**
         * Set the scheduling strategy to 'On primary node'.
         */
        function setPrimaryNodeOnly() {
            this.editModel.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.editModel.schedule.schedulingPeriod = "5 min";
        }

        /**
         * Force the model to be set to the Default strategy
         */
        function setDefaultScheduleStrategy() {
            if (this.editModel.inputProcessorType != '' && (this.editModel.schedule.schedulingStrategy.touched == false || this.editModel.schedule.schedulingStrategy.touched == undefined)) {
                if (this.editModel.inputProcessorType.indexOf("GetFile") >= 0) {
                    setTimerDriven();
                }
                else if (this.editModel.inputProcessorType.indexOf("GetTableData") >= 0) {
                    setCronDriven();
                }
                else if (this.editModel.inputProcessorType.indexOf("TriggerFeed") >= 0) {
                    setTriggerDriven();
                }
            }
        }


        /**
         * update the default strategies in the list
         */
        this.updateScheduleStrategies();
        
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,this.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access:any) =>{
            this.allowEdit = access && !this.model.view.schedule.disabled;
        });

        // Detect if NiFi is clustered
        $http.get(RestUrlService.NIFI_STATUS).then((response:any) => {
            this.isClustered = (angular.isDefined(response.data.clustered) && response.data.clustered);
            this.supportsExecutionNode = (this.isClustered && angular.isDefined(response.data.version) && !response.data.version.match(/^0\.|^1\.0/));
            this.updateScheduleStrategies();
        });

            
    }
}

    angular.module(moduleName).controller('FeedScheduleController', ["$scope","$http","$mdDialog","$q","AccessControlService","EntityAccessControlService","FeedService","RestUrlService","$filter",FeedScheduleController]);

    angular.module(moduleName)
        .directive('thinkbigFeedSchedule', directive);

