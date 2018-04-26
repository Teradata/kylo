define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-schedule.html',
            controller: "FeedScheduleController",
            link: function ($scope, element, attrs, controller) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
            }
        };
    };
    var FeedScheduleController = /** @class */ (function () {
        function FeedScheduleController($scope, $http, $mdDialog, $q, accessControlService, EntityAccessControlService, FeedService, RestUrlService, $filter) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$q = $q;
            this.accessControlService = accessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.$filter = $filter;
            // define(['angular','feed-mgr/feeds/edit-feed/module-name','pascalprecht.translate'], function (angular,moduleName) {
            // var self = this;
            this.versions = false;
            /**
             * Indicates if the feed schedule may be edited.
             * @type {boolean}
             */
            this.allowEdit = !this.versions;
            /**
             * The data model for the feed
             * @type {data.editFeedModel|{}|*}
             */
            this.model = this.FeedService.editFeedModel;
            this.versionFeedModel = this.FeedService.versionFeedModel;
            this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
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
            this.scheduleFeedForm = {};
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
            * All possible schedule strategies
            * @type {*[]}
            */
            this.allScheduleStrategies = [{ label: this.$filter('translate')('views.feed-schedule.Cron'), value: "CRON_DRIVEN" }, { label: this.$filter('translate')('views.feed-schedule.Timer'), value: "TIMER_DRIVEN" }, { label: this.$filter('translate')('views.feed-schedule.T/E'), value: "TRIGGER_DRIVEN" },
                { label: "On primary node", value: "PRIMARY_NODE_ONLY" }];
            /**
             * Array of strategies filtered for this feed
             * @type {any[]}
             */
            this.scheduleStrategies = [];
            this.versions = this.$scope.versions;
        }
        /**
         * The model stores the timerAmount and timerUnits together as 1 string.
         * This will parse that string and set each component in the controller
         */
        FeedScheduleController.prototype.parseTimer = function () {
            this.timerAmount = parseInt(this.editModel.schedule.schedulingPeriod);
            var startIndex = this.editModel.schedule.schedulingPeriod.indexOf(" ");
            if (startIndex != -1) {
                this.timerUnits = this.editModel.schedule.schedulingPeriod.substring(startIndex + 1);
            }
        };
        /**
         * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
         */
        FeedScheduleController.prototype.timerChanged = function () {
            if (this.timerAmount < 0) {
                this.timerAmount = null;
            }
            if (this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
                this.showTimerAlert();
            }
            this.editModel.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
            this.validate();
        };
        FeedScheduleController.prototype.showTimerAlert = function (ev) {
            this.$mdDialog.show(this.$mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title('Warning. Rapid Timer')
                .textContent('Warning.  You have this feed scheduled for a very fast timer.  Please ensure you want this feed scheduled this fast before you proceed.')
                .ariaLabel('Warning Fast Timer')
                .ok('Got it!')
                .targetEvent(ev));
        };
        ;
        /**
     * When the strategy changes ensure the defaults are set
     */
        FeedScheduleController.prototype.onScheduleStrategyChange = function () {
            if (this.editModel.schedule.schedulingStrategy == "CRON_DRIVEN") {
                if (this.editModel.schedule.schedulingPeriod != this.FeedService.DEFAULT_CRON) {
                    this.setCronDriven();
                }
            }
            else if (this.editModel.schedule.schedulingStrategy == "TIMER_DRIVEN") {
                this.setTimerDriven();
            }
            else if (this.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY") {
                this.setPrimaryNodeOnly();
            }
        };
        ;
        /**
         * Different templates have different schedule strategies.
         * Filter out those that are not needed based upon the template
         */
        FeedScheduleController.prototype.updateScheduleStrategies = function () {
            var _this = this;
            // Filter schedule strategies
            this.scheduleStrategies = _.filter(this.allScheduleStrategies, function (strategy) {
                if (_this.model.registeredTemplate.allowPreconditions) {
                    return (strategy.value === "TRIGGER_DRIVEN");
                }
                else if (strategy.value === "PRIMARY_NODE_ONLY") {
                    return (_this.isClustered && !_this.supportsExecutionNode);
                }
                else {
                    return (strategy.value !== "TRIGGER_DRIVEN");
                }
            });
        };
        /**
         * Called when editing this section
         * copy the model to the {@code editModel} object
         */
        FeedScheduleController.prototype.onEdit = function () {
            //copy the model
            this.editModel.category = { systemName: this.FeedService.editFeedModel.category.systemName };
            this.editModel.systemFeedName = this.FeedService.editFeedModel.systemFeedName;
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
        ;
        FeedScheduleController.prototype.onCancel = function () {
        };
        /**
         * When saving copy the editModel and save it
         * @param ev
         */
        FeedScheduleController.prototype.onSave = function (ev) {
            var _this = this;
            var isValid = this.validate();
            if (isValid) {
                //save changes to the model
                this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-schedule.Saving'), this.model.feedName);
                var copy = angular.copy(this.FeedService.editFeedModel);
                copy.schedule = this.editModel.schedule;
                copy.userProperties = null;
                //Server may have updated value. Don't send via UI.
                copy.historyReindexingStatus = undefined;
                this.FeedService.saveFeedModel(copy).then(function (response) {
                    _this.FeedService.hideFeedSavingDialog();
                    _this.editableSection = false;
                    //save the changes back to the model
                    _this.model.schedule = _this.editModel.schedule;
                    //Get the updated value from the server.
                    _this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
                }, function (response) {
                    _this.FeedService.hideFeedSavingDialog();
                    _this.FeedService.buildErrorData(_this.model.feedName, response);
                    _this.FeedService.showFeedErrorsDialog();
                    //make it editable
                    _this.editableSection = true;
                });
            }
        };
        /**
         * Remove the precondition from the schedule
         * @param $index
         */
        FeedScheduleController.prototype.deletePrecondition = function ($index) {
            if (this.editModel.schedule.preconditions != null) {
                this.editModel.schedule.preconditions.splice($index, 1);
            }
        };
        /**
         * show the dialog allowing users to modify/add preconditions
         * @param index
         */
        FeedScheduleController.prototype.showPreconditionDialog = function (index) {
            var _this = this;
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
                .then(function (msg) {
                _this.validate();
            }, function () {
            });
        };
        ;
        /**
         * Validates the inputs are good
         * @returns {*}
         */
        FeedScheduleController.prototype.validate = function () {
            //cron expression validation is handled via the cron-expression validator
            var valid = (this.editModel.schedule.schedulingStrategy == 'CRON_DRIVEN') ||
                (this.editModel.schedule.schedulingStrategy == 'TIMER_DRIVEN' && this.timerAmount != undefined && this.timerAmount != null) ||
                (this.editModel.schedule.schedulingStrategy == 'TRIGGER_DRIVEN' && this.editModel.schedule.preconditions != null && this.editModel.schedule.preconditions.length > 0) ||
                (this.editModel.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY" && this.timerAmount != undefined && this.timerAmount != null);
            this.isValid = valid && this.scheduleFeedForm.$valid;
            return this.isValid;
        };
        /**
         * Force the model and timer to be set to Timer with the defaults
         */
        FeedScheduleController.prototype.setTimerDriven = function () {
            this.editModel.schedule.schedulingStrategy = 'TIMER_DRIVEN';
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.editModel.schedule.schedulingPeriod = "5 min";
        };
        /**
         * Force the model to be set to Cron
         */
        FeedScheduleController.prototype.setCronDriven = function () {
            this.editModel.schedule.schedulingStrategy = 'CRON_DRIVEN';
            this.editModel.schedule.schedulingPeriod = this.FeedService.DEFAULT_CRON;
        };
        /**
         * Force the model to be set to Triggger
         */
        FeedScheduleController.prototype.setTriggerDriven = function () {
            this.editModel.schedule.schedulingStrategy = 'TRIGGER_DRIVEN';
        };
        /**
         * Set the scheduling strategy to 'On primary node'.
         */
        FeedScheduleController.prototype.setPrimaryNodeOnly = function () {
            this.editModel.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.editModel.schedule.schedulingPeriod = "5 min";
        };
        /**
         * Force the model to be set to the Default strategy
         */
        FeedScheduleController.prototype.setDefaultScheduleStrategy = function () {
            if (this.editModel.inputProcessorType != '' && (this.editModel.schedule.schedulingStrategy.touched == false || this.editModel.schedule.schedulingStrategy.touched == undefined)) {
                if (this.editModel.inputProcessorType.indexOf("GetFile") >= 0) {
                    this.setTimerDriven();
                }
                else if (this.editModel.inputProcessorType.indexOf("GetTableData") >= 0) {
                    this.setCronDriven();
                }
                else if (this.editModel.inputProcessorType.indexOf("TriggerFeed") >= 0) {
                    this.setTriggerDriven();
                }
            }
        };
        FeedScheduleController.prototype.$onInit = function () {
            var _this = this;
            /**
             * update the default strategies in the list
             */
            this.updateScheduleStrategies();
            this.$q.when(this.accessControlService.hasPermission(this.EntityAccessControlService.FEEDS_EDIT, this.model, this.EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                _this.allowEdit = !_this.versions && access && !_this.model.view.schedule.disabled;
            });
            // Detect if NiFi is clustered
            this.$http.get(this.RestUrlService.NIFI_STATUS).then(function (response) {
                _this.isClustered = (angular.isDefined(response.data.clustered) && response.data.clustered);
                _this.supportsExecutionNode = (_this.isClustered && angular.isDefined(response.data.version) && !response.data.version.match(/^0\.|^1\.0/));
                _this.updateScheduleStrategies();
            });
            /**
             * Watch the model and update it if not set.
             */
            this.$scope.$watch(function () {
                return _this.FeedService.editFeedModel;
            }, function (newVal) {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = _this.FeedService.editFeedModel;
                }
            });
            if (this.versions) {
                this.$scope.$watch(function () {
                    return _this.FeedService.versionFeedModel;
                }, function (newVal) {
                    _this.versionFeedModel = _this.FeedService.versionFeedModel;
                });
                this.$scope.$watch(function () {
                    return _this.FeedService.versionFeedModelDiff;
                }, function (newVal) {
                    _this.versionFeedModelDiff = _this.FeedService.versionFeedModelDiff;
                });
            }
        };
        FeedScheduleController.prototype.diff = function (path) {
            return this.FeedService.diffOperation(path);
        };
        ;
        FeedScheduleController.prototype.diffCollection = function (path) {
            return this.FeedService.diffCollectionOperation(path);
        };
        FeedScheduleController.$inject = ["$scope", "$http", "$mdDialog", "$q", "AccessControlService", "EntityAccessControlService", "FeedService", "RestUrlService", "$filter"];
        return FeedScheduleController;
    }());
    exports.FeedScheduleController = FeedScheduleController;
    angular.module(moduleName).controller('FeedScheduleController', FeedScheduleController);
    angular.module(moduleName)
        .directive('thinkbigFeedSchedule', directive);
});
//# sourceMappingURL=feed-schedule.js.map