import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('../module-name');
import {CheckAll} from "../../shared/checkAll";



export class DefineFeedDataProcessingController {


    stepIndex: string;
    isValid: boolean = true;
    model: any;
    stepNumber: number;
    selectedColumn: any = {};
    mergeStrategies: any;
    /**
     * List of available domain types.
     * @type {DomainType[]}
     */
    availableDomainTypes: any = [];
    /**
    * The form in angular
    * @type {{}}
    */
    dataProcessingForm: any = {};
    /**
    * Provides a list of available tags.
    * @type {FeedTagService}
    */
    feedTagService: any;
    /**
    * Metadata for the selected column tag.
    * @type {{searchText: null, selectedItem: null}}
    */
     /**
     * Toggle Check All/None on Profile column
     * Default it to true
     * @type {CheckAll}
     */
    profileCheckAll: CheckAll;


    /**
     *
     * @type {CheckAll}
     */
    indexCheckAll: CheckAll;    
    tagChips: any = { searchText: null, selectedItem: null };
    defaultMergeStrategy: any;
    allCompressionOptions: any;
    targetFormatOptions: any;
    compressionOptions: any = ['NONE'];
    stepperController: { totalSteps: number };
    totalSteps: number;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    static readonly $inject = ["$scope", "$http", "$mdDialog", "$mdExpansionPanel", "RestUrlService", "FeedService",
        "BroadcastService", "StepperService", "Utils", "DomainTypesService", "FeedTagService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private $mdDialog: any, private $mdExpansionPanel: any, private RestUrlService: any, private FeedService: any
        , private BroadcastService: any, private StepperService: any, private Utils: any, private DomainTypesService: any, private FeedTagService: any) {

        this.model = FeedService.createFeedModel;

        this.profileCheckAll = new CheckAll('profile', true);
        this.indexCheckAll = new CheckAll( 'index', false);

        DomainTypesService.findAll().then((domainTypes: any) => {
            this.availableDomainTypes = domainTypes;
        });

        this.feedTagService = FeedTagService;

        this.mergeStrategies = angular.copy(FeedService.mergeStrategies);
        if (this.model.id == null && angular.isDefined(this.defaultMergeStrategy)) {
            this.model.table.targetMergeStrategy = this.defaultMergeStrategy;
        }
        FeedService.updateEnabledMergeStrategy(this.model, this.mergeStrategies);

        this.BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, (event:any, index: any) =>{
            if (index == parseInt(this.stepIndex)) {
                this.validateMergeStrategies();
                
                // Update the data type display
                _.each(this.model.table.tableSchema.fields, (columnDef: any, idx: any) => {
                    columnDef.dataTypeDisplay = this.FeedService.getDataTypeDisplay(columnDef);
                    var policy = this.model.table.fieldPolicies[idx];
                    policy.name = columnDef.name;
                });

                this.profileCheckAll.setup(this.model.table);
                this.indexCheckAll.setup(this.model.table);
            }
        })

        this.allCompressionOptions = FeedService.compressionOptions;

        this.targetFormatOptions = FeedService.targetFormatOptions;

        // Open panel by default
        this.expandFieldPoliciesPanel();

        // Initialize UI
        this.onTableFormatChange();
    };

    expandFieldPoliciesPanel() {
        this.$mdExpansionPanel().waitFor('panelFieldPolicies').then((instance: any) =>{
            instance.expand();
        });
    };
    validateMergeStrategies() {
        var validPK = this.FeedService.enableDisablePkMergeStrategy(this.model, this.mergeStrategies);

        this.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidPKOption', validPK);

        let validRollingSync = this.FeedService.enableDisableRollingSyncMergeStrategy(this.model, this.mergeStrategies);

        this.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', validRollingSync);

        this.isValid = validRollingSync && validPK;
    }
    onChangeMergeStrategy() {
        this.validateMergeStrategies();
    };
    getSelectedColumn() {
        return this.selectedColumn;
    };
    onSelectedColumn(index: any) {

        var selectedColumn = this.model.table.tableSchema.fields[index];
        var firstSelection = this.selectedColumn == null;
        this.selectedColumn = selectedColumn;

        if (firstSelection) {
            //trigger scroll to stick the selection to the screen
            this.Utils.waitForDomElementReady('#selectedColumnPanel2', () => {
                angular.element('#selectedColumnPanel2').triggerHandler('stickIt');
            })
        }

        // Ensure tags is an array
        if (!angular.isArray(selectedColumn.tags)) {
            selectedColumn.tags = [];
        }
    };
    onTableFormatChange() {

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
        
        if(format.indexOf('OpenCSVSerde')>=0){
            //warn the user that their schema will be switched to strings

            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title('Target Schema will be modified')
                    .htmlContent("Warning.  This format will change your target schema. <br/>All field data types will all be converted to  'string'. <br/>Choose a different type if you wish to preserve your defined schema.")
                    .ariaLabel('Modifying Target Schema')
                    .ok('Got it!')
            );
    }
    };
    findProperty(key: any) {
        return _.find(this.model.inputProcessor.properties, (property: any) =>{
            //return property.key = 'Source Database Connection';
            return property.key == key;
        });
    }
    showFieldRuleDialog(field: any) {
        this.$mdDialog.show({
            controller: 'FeedFieldPolicyRuleDialogController',
            templateUrl: '../../../shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                feed: this.model,
                field: field
            }
        })
            .then(() => {
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
    onDomainTypeChange(policy: any) {
        // Check if removing domain type
        if (!angular.isString(policy.domainTypeId) || policy.domainTypeId === "") {
            delete policy.$currentDomainType;
            return;
        }

        // Find domain type from id
        var domainType = _.find(this.availableDomainTypes, (domainType: any) => {
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
                templateUrl: "../../../shared/apply-domain-type/apply-domain-type-dialog.html",
                locals: {
                    domainType: domainType,
                    field: policy.field
                }
            });
        } else {
            promise = Promise.resolve();
        }
        promise.then(() =>{
            // Set domain type
            this.FeedService.setDomainTypeForField(policy.field, policy, domainType);
            // Update field properties
            delete policy.field.$allowDomainTypeConflict;
            policy.field.dataTypeDisplay = this.FeedService.getDataTypeDisplay(policy.field);
            policy.name = policy.field.name;
        }, () => {
            // Revert domain type
            policy.domainTypeId = angular.isDefined(policy.$currentDomainType) ? policy.$currentDomainType.id : null;
        });
    };
    /**
         * Transforms the specified chip into a tag.
         * @param {string} chip the chip
         * @returns {Object} the tag
    */
    transformChip(chip: any) {
        // If it is an object, it's already a known chip
        if (angular.isObject(chip)) {
            return chip;
        }
        // Otherwise, create a new one
        return { name: chip }
    };
}

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
        templateUrl: './define-feed-data-processing.html',
    });
