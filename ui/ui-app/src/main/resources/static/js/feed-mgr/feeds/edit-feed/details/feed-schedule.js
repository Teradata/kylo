define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-schedule.html',
            controller: "FeedScheduleController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $http, $mdDialog, $q,AccessControlService, EntityAccessControlService,FeedService, RestUrlService) {

        var self = this;

        /**
         * Indicates if the feed schedule may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * The data model for the feed
         * @type {data.editFeedModel|{}|*}
         */
        this.model = FeedService.editFeedModel;

        /**
         * The model with only the Schedule data that is populated via the {@code this#onEdit()} method
         * @type {{}}
         */
        this.editModel = {};

        this.editableSection = false;

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
         * flag to indicate if the inputs are valid
         * @type {boolean}
         */
        this.isValid = false;

        /**
         * the Angular form for validation
         * @type {{}}
         */
        self.scheduleFeedForm = {};

        /**
         * Indicates that NiFi is clustered.
         *
         * @type {boolean}
         */
        this.isClustered = true;

        /**
         * Indicates that NiFi supports the execution node property.
         * @type {boolean}
         */
        this.supportsExecutionNode = true;

        /**
         * Watch the model and update it if not set.
         */
        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        })

        /**
         * All possible schedule strategies
         * @type {*[]}
         */
        var allScheduleStrategies = [{label: "Cron", value: "CRON_DRIVEN"}, {label: "Timer", value: "TIMER_DRIVEN"}, {label: "Trigger/Event", value: "TRIGGER_DRIVEN"},
            {label: "On primary node", value: "PRIMARY_NODE_ONLY"}];

        /**
         * Different templates have different schedule strategies.
         * Filter out those that are not needed based upon the template
         */
        function updateScheduleStrategies() {
            // Filter schedule strategies
            self.scheduleStrategies = _.filter(allScheduleStrategies, function(strategy) {
                if (self.model.registeredTemplate.allowPreconditions) {
                    return (strategy.value === "TRIGGER_DRIVEN");
                } else if (strategy.value === "PRIMARY_NODE_ONLY") {
                    return (self.isClustered && !self.supportsExecutionNode);
                } else {
                    return (strategy.value !== "TRIGGER_DRIVEN");
                }
            });
        }

        /**
         * The model stores the timerAmount and timerUnits together as 1 string.
         * This will parse that string and set each component in the controller
         */
        function parseTimer() {
            self.timerAmount = parseInt(self.editModel.schedule.schedulingPeriod);
            var startIndex = self.editModel.schedule.schedulingPeriod.indexOf(" ");
            if (startIndex != -1) {
                self.timerUnits = self.editModel.schedule.schedulingPeriod.substring(startIndex + 1);
            }
        }

        /**
         * Force the model and timer to be set to Timer with the defaults
         */
        function setTimerDriven() {
            self.editModel.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            self.timerAmount = 5;
            self.timerUnits = "min";
            self.editModel.schedule.schedulingPeriod = "5 min";
        }

        /**
         * Force the model to be set to Cron
         */
        function setCronDriven() {
            self.editModel.schedule.schedulingStrategy = 'CRON_DRIVEN'
            self.editModel.schedule.schedulingPeriod = FeedService.DEFAULT_CRON;
        }

        /**
         * Force the model to be set to Triggger
         */
        function setTriggerDriven() {
            self.editModel.schedule.schedulingStrategy = 'TRIGGER_DRIVEN'
        }

        /**
         * Set the scheduling strategy to 'On primary node'.
         */
        function setPrimaryNodeOnly() {
            self.editModel.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
            self.timerAmount = 5;
            self.timerUnits = "min";
            self.editModel.schedule.schedulingPeriod = "5 min";
        }

        /**
         * Force the model to be set to the Default strategy
         */
        function setDefaultScheduleStrategy() {
            if (self.editModel.inputProcessorType != '' && (self.editModel.schedule.schedulingStrategy.touched == false || self.editModel.schedule.schedulingStrategy.touched == undefined)) {
                if (self.editModel.inputProcessorType.indexOf("GetFile") >= 0) {
                    setTimerDriven();
                }
                else if (self.editModel.inputProcessorType.indexOf("GetTableData") >= 0) {
                    setCronDriven();
                }
                else if (self.editModel.inputProcessorType.indexOf("TriggerFeed") >= 0) {
                    setTriggerDriven();
                }
            }
        }

        /**
         * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
         */
        this.timerChanged = function () {
            if (self.timerAmount < 0) {
                self.timerAmount = null;
            }
            if (self.timerAmount != null && (self.timerAmount == 0 || (self.timerAmount < 3 && self.timerUnits == 'sec'))) {
                self.showTimerAlert();
            }
            self.editModel.schedule.schedulingPeriod = self.timerAmount + " " + self.timerUnits;
            validate();
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

        /**
         * Validates the inputs are good
         * @returns {*}
         */
        function validate() {
            //cron expression validation is handled via the cron-expression validator
            var valid = (self.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') ||
                        (self.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN' && self.timerAmount != undefined && self.timerAmount != null) ||
                        (self.editModel.schedule.schedulingStrategy == 'TRIGGER_DRIVEN' && self.editModel.schedule.preconditions != null && self.editModel.schedule.preconditions.length > 0 ) ||
                        (self.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY" && self.timerAmount != undefined && self.timerAmount != null);
            self.isValid = valid && self.scheduleFeedForm.$valid;
            return self.isValid;
        }

        /**
         * update the default strategies in the list
         */
        updateScheduleStrategies();

        /**
         * When the strategy changes ensure the defaults are set
         */
        this.onScheduleStrategyChange = function() {
            if(self.editModel.schedule.schedulingStrategy == "CRON_DRIVEN") {
                if (self.editModel.schedule.schedulingPeriod != FeedService.DEFAULT_CRON) {
                    setCronDriven();
                }
            } else if(self.editModel.schedule.schedulingStrategy == "TIMER_DRIVEN") {
                setTimerDriven();
            } else if(self.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY") {
                setPrimaryNodeOnly();
            }
        };

        /**
         * Called when editing this section
         * copy the model to the {@code editModel} object
         */
        this.onEdit = function(){
            //copy the model
            self.editModel.category = {systemName: FeedService.editFeedModel.category.systemName};
            self.editModel.systemFeedName = FeedService.editFeedModelsystemFeedName;
            self.editModel.schedule = angular.copy(FeedService.editFeedModel.schedule);
            self.editModel.inputProcessorType = FeedService.editFeedModel.inputProcessorType;
            if (self.editModel.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY" && (!self.isClustered || self.supportsExecutionNode)) {
                self.editModel.schedule.schedulingStrategy = "TIMER_DRIVEN";
                if (self.supportsExecutionNode) {
                    self.editModel.schedule.executionNode = "PRIMARY";
                }
            }
            if (self.editModel.schedule.schedulingStrategy == "TIMER_DRIVEN" || self.editModel.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY") {
                parseTimer();
            }
            if (self.isClustered && (!angular.isString(self.editModel.schedule.executionNode) || self.editModel.schedule.executionNode.length === 0)) {
                self.editModel.schedule.executionNode = "ALL";
            }
            validate();
        };

        this.onCancel = function() {

        }
        /**
         * When saving copy the editModel and save it
         * @param ev
         */
        this.onSave = function (ev) {
            var isValid = validate();
            if (isValid) {
                //save changes to the model
                FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);
                var copy = angular.copy(FeedService.editFeedModel);
                copy.schedule = self.editModel.schedule;
                copy.userProperties = null;
                FeedService.saveFeedModel(copy).then(function (response) {
                    FeedService.hideFeedSavingDialog();
                    self.editableSection = false;
                    //save the changes back to the model
                    self.model.schedule = self.editModel.schedule;
                }, function (response) {
                    FeedService.hideFeedSavingDialog();
                    FeedService.buildErrorData(self.model.feedName, response);
                    FeedService.showFeedErrorsDialog();
                    //make it editable
                    self.editableSection = true;
                });
            }
        }

        /**
         * Remove the precondition from the schedule
         * @param $index
         */
        this.deletePrecondition = function ($index) {
            if (self.editModel.schedule.preconditions != null) {
                self.editModel.schedule.preconditions.splice($index, 1);
            }
        }

        /**
         * show the dialog allowing users to modify/add preconditions
         * @param index
         */
        this.showPreconditionDialog = function (index) {
            $mdDialog.show({
                controller: 'FeedPreconditionsDialogController',
                templateUrl: 'js/feed-mgr/feeds/shared/define-feed-preconditions-dialog.html',
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

        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
            self.allowEdit = access && !self.model.view.schedule.disabled;
        });

        // Detect if NiFi is clustered
        $http.get(RestUrlService.NIFI_STATUS).then(function(response) {
            self.isClustered = (angular.isDefined(response.data.clustered) && response.data.clustered);
            self.supportsExecutionNode = (self.isClustered && angular.isDefined(response.data.version) && !response.data.version.match(/^0\.|^1\.0/));
            updateScheduleStrategies();
        });
    };


    angular.module(moduleName).controller('FeedScheduleController', ["$scope","$http","$mdDialog","$q","AccessControlService","EntityAccessControlService","FeedService","RestUrlService",controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedSchedule', directive);

});
