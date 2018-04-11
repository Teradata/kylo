define(["require", "exports", "angular", "underscore", "../../shared/checkAll"], function (require, exports, angular, _, checkAll_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    // import {CheckAll, IndexCheckAll, ProfileCheckAll } from './DefineFeedDetailsCheckAll';
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedDataProcessingController = /** @class */ (function () {
        function DefineFeedDataProcessingController($scope, $http, $mdDialog, $mdExpansionPanel, RestUrlService, FeedService, BroadcastService, StepperService, Utils, DomainTypesService, FeedTagService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$mdExpansionPanel = $mdExpansionPanel;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.BroadcastService = BroadcastService;
            this.StepperService = StepperService;
            this.Utils = Utils;
            this.DomainTypesService = DomainTypesService;
            this.FeedTagService = FeedTagService;
            this.isValid = true;
            this.selectedColumn = {};
            /**
             * List of available domain types.
             * @type {DomainType[]}
             */
            this.availableDomainTypes = [];
            /**
            * The form in angular
            * @type {{}}
            */
            this.dataProcessingForm = {};
            this.tagChips = { searchText: null, selectedItem: null };
            this.compressionOptions = ['NONE'];
            this.model = FeedService.createFeedModel;
            this.profileCheckAll = new checkAll_1.CheckAll('profile', true);
            this.indexCheckAll = new checkAll_1.CheckAll('index', false);
            DomainTypesService.findAll().then(function (domainTypes) {
                this.availableDomainTypes = domainTypes;
            });
            this.feedTagService = FeedTagService;
            this.mergeStrategies = angular.copy(FeedService.mergeStrategies);
            if (this.model.id == null && angular.isDefined(this.defaultMergeStrategy)) {
                this.model.table.targetMergeStrategy = this.defaultMergeStrategy;
            }
            FeedService.updateEnabledMergeStrategy(this.model, this.mergeStrategies);
            this.BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, function (event, index) {
                if (index == parseInt(_this.stepIndex)) {
                    _this.validateMergeStrategies();
                    // Update the data type display
                    _.each(_this.model.table.tableSchema.fields, function (columnDef, idx) {
                        columnDef.dataTypeDisplay = _this.FeedService.getDataTypeDisplay(columnDef);
                        var policy = _this.model.table.fieldPolicies[idx];
                        policy.name = columnDef.name;
                    });
                    _this.profileCheckAll.setup(_this.model.table);
                    _this.indexCheckAll.setup(_this.model.table);
                }
            });
            this.allCompressionOptions = FeedService.compressionOptions;
            this.targetFormatOptions = FeedService.targetFormatOptions;
            // Open panel by default
            this.expandFieldPoliciesPanel();
            // Initialize UI
            this.onTableFormatChange();
        }
        DefineFeedDataProcessingController.prototype.$onInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
        };
        ;
        DefineFeedDataProcessingController.prototype.expandFieldPoliciesPanel = function () {
            this.$mdExpansionPanel().waitFor('panelFieldPolicies').then(function (instance) {
                instance.expand();
            });
        };
        ;
        DefineFeedDataProcessingController.prototype.validateMergeStrategies = function () {
            var validPK = this.FeedService.enableDisablePkMergeStrategy(this.model, this.mergeStrategies);
            this.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidPKOption', validPK);
            var validRollingSync = this.FeedService.enableDisableRollingSyncMergeStrategy(this.model, this.mergeStrategies);
            this.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', validRollingSync);
            this.isValid = validRollingSync && validPK;
        };
        DefineFeedDataProcessingController.prototype.onChangeMergeStrategy = function () {
            this.validateMergeStrategies();
        };
        ;
        DefineFeedDataProcessingController.prototype.getSelectedColumn = function () {
            return this.selectedColumn;
        };
        ;
        DefineFeedDataProcessingController.prototype.onSelectedColumn = function (index) {
            var selectedColumn = this.model.table.tableSchema.fields[index];
            var firstSelection = this.selectedColumn == null;
            this.selectedColumn = selectedColumn;
            if (firstSelection) {
                //trigger scroll to stick the selection to the screen
                this.Utils.waitForDomElementReady('#selectedColumnPanel2', function () {
                    angular.element('#selectedColumnPanel2').triggerHandler('stickIt');
                });
            }
            // Ensure tags is an array
            if (!angular.isArray(selectedColumn.tags)) {
                selectedColumn.tags = [];
            }
        };
        ;
        DefineFeedDataProcessingController.prototype.onTableFormatChange = function () {
            var format = this.model.table.targetFormat;
            if (format == 'STORED AS ORC') {
                this.compressionOptions = this.allCompressionOptions['ORC'];
            }
            else if (format == 'STORED AS PARQUET') {
                this.compressionOptions = this.allCompressionOptions['PARQUET'];
            }
            else {
                this.compressionOptions = ['NONE'];
            }
        };
        ;
        DefineFeedDataProcessingController.prototype.findProperty = function (key) {
            return _.find(this.model.inputProcessor.properties, function (property) {
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        };
        DefineFeedDataProcessingController.prototype.showFieldRuleDialog = function (field) {
            this.$mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: this.model,
                    field: field
                }
            })
                .then(function () {
                if (angular.isObject(field.$currentDomainType)) {
                    var domainStandardization = _.map(field.$currentDomainType.fieldPolicy.standardization, _.property("name"));
                    var domainValidation = _.map(field.$currentDomainType.fieldPolicy.validation, _.property("name"));
                    var fieldStandardization = _.map(field.standardization, _.property("name"));
                    var fieldValidation = _.map(field.validation, _.property("name"));
                    if (!angular.equals(domainStandardization, fieldStandardization) || !angular.equals(domainValidation, fieldValidation)) {
                        delete field.$currentDomainType;
                        field.domainTypeId = null;
                    }
                }
            });
        };
        ;
        /**
             * Display a confirmation when the domain type of a field is changed and there are existing standardizers and validators.
             *
             * @param {FieldPolicy} policy the field policy
        */
        DefineFeedDataProcessingController.prototype.onDomainTypeChange = function (policy) {
            // Check if removing domain type
            if (!angular.isString(policy.domainTypeId) || policy.domainTypeId === "") {
                delete policy.$currentDomainType;
                return;
            }
            // Find domain type from id
            var domainType = _.find(this.availableDomainTypes, function (domainType) {
                return (domainType.id === policy.domainTypeId);
            });
            // Apply domain type to field
            var promise;
            if ((domainType.field.derivedDataType !== null && (domainType.field.derivedDataType !== policy.field.derivedDataType || domainType.field.precisionScale !== policy.field.precisionScale))
                || (angular.isArray(policy.standardization) && policy.standardization.length > 0)
                || (angular.isArray(policy.field.tags) && policy.field.tags.length > 0)
                || (angular.isArray(policy.validation) && policy.validation.length > 0)) {
                promise = this.$mdDialog.show({
                    controller: "ApplyDomainTypeDialogController",
                    escapeToClose: false,
                    fullscreen: true,
                    parent: angular.element(document.body),
                    templateUrl: "js/feed-mgr/shared/apply-domain-type/apply-domain-type-dialog.html",
                    locals: {
                        domainType: domainType,
                        field: policy.field
                    }
                });
            }
            else {
                promise = Promise.resolve();
            }
            promise.then(function () {
                // Set domain type
                this.FeedService.setDomainTypeForField(policy.field, policy, domainType);
                // Update field properties
                delete policy.field.$allowDomainTypeConflict;
                policy.field.dataTypeDisplay = this.FeedService.getDataTypeDisplay(policy.field);
                policy.name = policy.field.name;
            }, function () {
                // Revert domain type
                policy.domainTypeId = angular.isDefined(policy.$currentDomainType) ? policy.$currentDomainType.id : null;
            });
        };
        ;
        /**
             * Transforms the specified chip into a tag.
             * @param {string} chip the chip
             * @returns {Object} the tag
        */
        DefineFeedDataProcessingController.prototype.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip };
        };
        ;
        DefineFeedDataProcessingController.$inject = ["$scope", "$http", "$mdDialog", "$mdExpansionPanel", "RestUrlService", "FeedService",
            "BroadcastService", "StepperService", "Utils", "DomainTypesService", "FeedTagService"];
        return DefineFeedDataProcessingController;
    }());
    exports.DefineFeedDataProcessingController = DefineFeedDataProcessingController;
    angular.module(moduleName)
        .component('thinkbigDefineFeedDataProcessing', {
        bindings: {
            defaultMergeStrategy: "@",
            stepIndex: '@'
        },
        require: {
            stepperController: '^thinkbigStepper'
        },
        controllerAs: 'vm',
        controller: DefineFeedDataProcessingController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-data-processing.html',
    });
});
//# sourceMappingURL=DefineFeedDataProcessingDirective.js.map