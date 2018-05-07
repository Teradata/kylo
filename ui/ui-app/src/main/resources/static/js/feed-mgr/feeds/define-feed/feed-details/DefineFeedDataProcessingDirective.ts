import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');
var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            defaultMergeStrategy: "@",
            stepIndex: '@'
        },
        controllerAs: 'vm',
        require: ['thinkbigDefineFeedDataProcessing', '^thinkbigStepper'],
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-data-processing.html',
        controller: "DefineFeedDataProcessingController",
        link: function ($scope:any, element:any, attrs:any, controllers:any) {
            var thisController = controllers[0];
            var stepperController = controllers[1];
            thisController.stepperController = stepperController;
            thisController.totalSteps = stepperController.totalSteps;
        }

    };
};



export class DefineFeedDataProcessingController {


    stepIndex:string;
    isValid:boolean;
    model:any;
    stepNumber:number;
    selectedColumn:any;
    profileCheckAll:any;
    indexCheckAll:any;
    expandFieldPoliciesPanel:any;
    mergeStrategies:any;
    /**
     * List of available domain types.
     * @type {DomainType[]}
     */
    availableDomainTypes:any;
    dataProcessingForm:any;
    feedTagService:any;
    tagChips:any;
    defaultMergeStrategy:any;
    allCompressionOptions:any;
    targetFormatOptions:any;
    transformChip:any;
    compressionOptions:any;
    onTableFormatChange:any;
    onChangeMergeStrategy:any;
    getSelectedColumn:any;
    onSelectedColumn:any;
    showFieldRuleDialog:any;
    onDomainTypeChange:any;


    constructor (private $scope:any, private $http:any, private $mdDialog:any, private $mdExpansionPanel:any, private RestUrlService:any, private FeedService:any
        , private BroadcastService:any, private StepperService:any, private Utils:any, private DomainTypesService:any, private FeedTagService:any) {
        var self = this;

        this.isValid = true;
        this.model = FeedService.createFeedModel;
        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.selectedColumn = {};
        self.availableDomainTypes = [];
        DomainTypesService.findAll().then(function (domainTypes:any) {
            self.availableDomainTypes = domainTypes;
        });

        var checkAll = {
            isChecked: true,
            isIndeterminate: false,
            totalChecked: 0,
            clicked: function (checked:any) {
                if (checked) {
                    this.totalChecked++;
                }
                else {
                    this.totalChecked--;
                }
                this.markChecked();
            },
            markChecked: function () {
                if (this.totalChecked == self.model.table.fieldPolicies.length) {
                    this.isChecked = true;
                    this.isIndeterminate = false;
                }
                else if (this.totalChecked > 0) {
                    this.isChecked = false;
                    this.isIndeterminate = true;
                }
                else if (this.totalChecked == 0) {
                    this.isChecked = false;
                    this.isIndeterminate = false;
                }
            }
        };

        /**
         * Toggle Check All/None on Profile column
         * Default it to true
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
         */
        this.profileCheckAll = angular.extend({
            isChecked: true,
            isIndeterminate: false,
            toggleAll: function () {
                var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
                _.each(self.model.table.fieldPolicies, function (field:any) {
                    field.profile = checked;
                });
                if (checked) {
                    this.totalChecked = self.model.table.fieldPolicies.length;
                }
                else {
                    this.totalChecked = 0;
                }
                this.markChecked();
            },
            setup: function () {
                self.profileCheckAll.totalChecked = 0;
                _.each(self.model.table.fieldPolicies, function (field:any) {
                    if (field.profile) {
                        self.profileCheckAll.totalChecked++;
                    }
                });
                self.profileCheckAll.markChecked();
            }
        }, checkAll);

        /**
         *
         * Toggle check all/none on the index column
         *
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
         */
        this.indexCheckAll = angular.extend({
            isChecked: false,
            isIndeterminate: false,
            toggleAll: function () {
                var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
                _.each(self.model.table.fieldPolicies, function (field:any) {
                    field.index = checked;
                });
                this.isChecked = checked;

                if (checked) {
                    this.totalChecked = self.model.table.fieldPolicies.length;
                }
                else {
                    this.totalChecked = 0;
                }
                this.markChecked();
            },
            setup: function () {
                self.indexCheckAll.totalChecked = 0;
                _.each(self.model.table.fieldPolicies, function (field:any) {
                    if (field.index) {
                        self.indexCheckAll.totalChecked++;
                    }
                });
                self.indexCheckAll.markChecked();
            }
        }, checkAll);

        /**
         * The form in angular
         * @type {{}}
         */
        this.dataProcessingForm = {};

        /**
         * Provides a list of available tags.
         * @type {FeedTagService}
         */
        self.feedTagService = FeedTagService;

        /**
         * Metadata for the selected column tag.
         * @type {{searchText: null, selectedItem: null}}
         */
        self.tagChips = {searchText: null, selectedItem: null};

        this.expandFieldPoliciesPanel = function () {
            $mdExpansionPanel().waitFor('panelFieldPolicies').then(function (instance:any) {
                instance.expand();
            });
        };

        this.mergeStrategies = angular.copy(FeedService.mergeStrategies);
        if (self.model.id == null && angular.isDefined(self.defaultMergeStrategy)) {
            self.model.table.targetMergeStrategy = self.defaultMergeStrategy;
        }
        FeedService.updateEnabledMergeStrategy(self.model, self.mergeStrategies);

        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, onActiveStep)

        function onActiveStep(event:any, index:any) {
            if (index == parseInt(self.stepIndex)) {
                validateMergeStrategies();

                // Update the data type display
                _.each(self.model.table.tableSchema.fields, function (columnDef:any, idx:any) {
                    columnDef.dataTypeDisplay = FeedService.getDataTypeDisplay(columnDef);
                    var policy = self.model.table.fieldPolicies[idx];
                    policy.name = columnDef.name;
                });

                self.profileCheckAll.setup();

                self.indexCheckAll.setup();
            }
        }

        this.allCompressionOptions = FeedService.compressionOptions;

        this.targetFormatOptions = FeedService.targetFormatOptions;

        // Open panel by default
        self.expandFieldPoliciesPanel();

        this.transformChip = function (chip:any) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };

        this.compressionOptions = ['NONE'];

        this.onTableFormatChange = function (opt:any) {

            var format = self.model.table.targetFormat;
            if (format == 'STORED AS ORC') {
                self.compressionOptions = self.allCompressionOptions['ORC'];
            }
            else if (format == 'STORED AS PARQUET') {
                self.compressionOptions = self.allCompressionOptions['PARQUET'];
            }
            else {
                self.compressionOptions = ['NONE'];
            }

            if(format.indexOf('OpenCSVSerde')>=0){
                //warn the user that their schema will be switched to strings

                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Target Schema will be modified')
                        .htmlContent("Warning.  This format will change your target schema. <br/>All field data types will all be converted to  'string'. <br/>Choose a different type if you wish to preserve your defined schema.")
                        .ariaLabel('Modifying Target Schema')
                        .ok('Got it!')
                );
        }
        };

        function findProperty(key:any) {
            return _.find(self.model.inputProcessor.properties, function (property:any) {
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        }

        this.onChangeMergeStrategy = function () {
            validateMergeStrategies();
        };

        function validateMergeStrategies() {
            var validPK = FeedService.enableDisablePkMergeStrategy(self.model, self.mergeStrategies);

            self.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidPKOption', validPK);

            let validRollingSync = FeedService.enableDisableRollingSyncMergeStrategy(self.model, self.mergeStrategies);

            self.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', validRollingSync);

            self.isValid = validRollingSync && validPK;
        }

        this.getSelectedColumn = function () {
            return self.selectedColumn;
        };

        this.onSelectedColumn = function (index:any) {

            var selectedColumn = self.model.table.tableSchema.fields[index];
            var firstSelection = self.selectedColumn == null;
            self.selectedColumn = selectedColumn;

            if (firstSelection) {
                //trigger scroll to stick the selection to the screen
                Utils.waitForDomElementReady('#selectedColumnPanel2', function () {
                    angular.element('#selectedColumnPanel2').triggerHandler('stickIt');
                })
            }

            // Ensure tags is an array
            if (!angular.isArray(selectedColumn.tags)) {
                selectedColumn.tags = [];
            }
        };

        this.showFieldRuleDialog = function (field:any) {
            $mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: self.model,
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

        /**
         * Display a confirmation when the domain type of a field is changed and there are existing standardizers and validators.
         *
         * @param {FieldPolicy} policy the field policy
         */
        self.onDomainTypeChange = function (policy:any) {
            // Check if removing domain type
            if (!angular.isString(policy.domainTypeId) || policy.domainTypeId === "") {
                delete policy.$currentDomainType;
                return;
            }

            // Find domain type from id
            var domainType = _.find(self.availableDomainTypes, function (domainType:any) {
                return (domainType.id === policy.domainTypeId);
            });

            // Apply domain type to field
            var promise;

            if ((domainType.field.derivedDataType !== null && (domainType.field.derivedDataType !== policy.field.derivedDataType || domainType.field.precisionScale !== policy.field.precisionScale))
                || (angular.isArray(policy.standardization) && policy.standardization.length > 0)
                || (angular.isArray(policy.field.tags) && policy.field.tags.length > 0)
                || (angular.isArray(policy.validation) && policy.validation.length > 0)) {
                promise = $mdDialog.show({
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
            } else {
                promise = Promise.resolve();
            }

            promise.then(function () {
                // Set domain type
                FeedService.setDomainTypeForField(policy.field, policy, domainType);
                // Update field properties
                delete policy.field.$allowDomainTypeConflict;
                policy.field.dataTypeDisplay = FeedService.getDataTypeDisplay(policy.field);
                policy.name = policy.field.name;
            }, function () {
                // Revert domain type
                policy.domainTypeId = angular.isDefined(policy.$currentDomainType) ? policy.$currentDomainType.id : null;
            });
        };

        /**
         * Transforms the specified chip into a tag.
         * @param {string} chip the chip
         * @returns {Object} the tag
         */
        self.transformChip = function (chip:any) {
            return angular.isObject(chip) ? chip : {name: chip};
        };

        // Initialize UI
        this.onTableFormatChange();
    };

}

angular.module(moduleName)
    .controller('DefineFeedDataProcessingController', ["$scope", "$http", "$mdDialog", "$mdExpansionPanel", "RestUrlService", "FeedService", "BroadcastService", "StepperService", "Utils",
                                                       "DomainTypesService", "FeedTagService", DefineFeedDataProcessingController])
    .directive('thinkbigDefineFeedDataProcessing', directive);



