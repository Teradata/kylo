import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from 'underscore';
import {FeedServiceTypes} from "../../../services/FeedServiceTypes";
import {Common} from "../../../../common/CommonTypes";
import {DomainType, DomainTypesService} from "../../../services/DomainTypesService";

const moduleName = require('feed-mgr/feeds/edit-feed/module-name');


let directiveConfig = function () {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
        scope: {
            versions: '=?'
        },
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-data-policies.html',
        controller: "FeedDataPoliciesController",
        link: function ($scope: any, element: any, attrs: any, controller: any) {
            if (angular.isUndefined($scope.versions)) {
                $scope.versions = false;
            }

        }

    };
};


export class CheckAll {
    isIndeterminate: boolean = false;
    totalChecked: number = 0;
     editModel: any;

    constructor( private fieldName: string, private isChecked: boolean) {

    }

    setup(editModel:any) {
        this.editModel = editModel;
        this.totalChecked = 0;
        _.each(this.editModel.fieldPolicies, (field) => {
            if (field["profile"]) {
                this.totalChecked++;
            }
        });
        this.markChecked();
    }

    clicked(checked: boolean) {
        if (checked) {
            this.totalChecked++;
        }
        else {
            this.totalChecked--;
        }
    }

    markChecked() {
        if (angular.isDefined(this.editModel) && this.totalChecked == this.editModel.fieldPolicies.length) {
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

    toggleAll() {
        var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
        if(angular.isDefined(this.editModel) ) {
            _.each(this.editModel.fieldPolicies, (field) => {
                field[this.fieldName] = checked;
            });
            if (checked) {
                this.totalChecked = this.editModel.fieldPolicies.length;
            }
            else {
                this.totalChecked = 0;
            }
        }
        else {
            this.totalChecked = 0;
        }
        this.markChecked();
    }

}

export class Controller implements ng.IComponentController {

    /**
     * Flag to indicate if we are on the versions page or not
     * @type {boolean}
     */
    versions: boolean = false;
    /**
     * Indicates if the feed data policies may be edited.
     * @type {boolean}
     */
    allowEdit: boolean = !this.versions;
    /**
     * The Feed Model
     * @type {{} | any}
     */
    model: any = this.FeedService.editFeedModel;

    /**
     * The Model for versioning the feed
     */
    versionFeedModel: any = this.FeedService.versionFeedModel;

    /**
     * The differences between the model and version Feed Model
     * @type {any | Array}
     */
    versionFeedModelDiff: any = this.FeedService.versionFeedModelDiff;

    /**
     * The angular form
     * @type {{}}
     */
    editFeedDataPoliciesForm: any = {};

    /**
     * Flag to indicate the section is editable
     * @type {boolean}
     */
    editableSection: boolean = false;

    /**
     * List of available domain types.
     * @type {DomainType[]}
     */
    availableDomainTypes: Array<DomainType> = [];
    /**
     * The Feed Model in its Edit state.  When a user clicks the onEdit
     * @type {null}
     */
    editModel: any = null;

    /**
     * The comporession Options
     */
    compressionOptions: Common.Map<string[]>;


    /**
     * The possible Merge Strategies
     */
    mergeStrategies: FeedServiceTypes.MergeStrategy[];

    /**
     * The possible target options
     */
    targetFormatOptions: Common.LabelValue[];


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

    /**
     * map of field name to field policy
     * TODO make an interface type for the policy object
     */
    fieldNameMap: any;

    static $inject = ["$scope", "$mdDialog", "$timeout", "$q", "$compile", "$sce", "AccessControlService", "EntityAccessControlService", "FeedService", "StateService",
        "FeedFieldPolicyRuleService", "DomainTypesService", "$filter"];

    constructor(private $scope: any, private $mdDialog: angular.material.IDialogService, private $timeout: angular.ITimeoutService, private $q: angular.IQService, private $compile: angular.ICompileService, private $sce: angular.ISCEService, private AccessControlService: any, private EntityAccessControlService: any, private FeedService: any, private StateService: any, private FeedFieldPolicyRuleService: any,
                DomainTypesService: DomainTypesService, private $filter: angular.IFilterService) {

        this.versions = $scope.versions;

        this.profileCheckAll = new CheckAll('profile', true);
        this.indexCheckAll = new CheckAll( 'index', false);

        DomainTypesService.findAll().then((domainTypes: any) => {
            this.availableDomainTypes = domainTypes;
            // KYLO-251 Remove data type until schema evolution is supported
            domainTypes.forEach((domainType: any) => {
                if (domainType && domainType.field) {
                    domainType.field.derivedDataType = null;
                    domainType.field.precisionScale = null;
                }
            });
        });

        this.compressionOptions = FeedService.allCompressionOptions();
        this.mergeStrategies = angular.copy(FeedService.mergeStrategies);

        this.targetFormatOptions = FeedService.targetFormatOptions;
        this.applyDefaults();
        this.populateFieldNameMap();

    }

    $onInit() {
        this.onInit();
    }

    onInit() {

        this.$scope.$watch(() => {
            return this.FeedService.editFeedModel;
        }, (newVal: any) => {
            //only update the model if it is not set yet
            if (this.model == null) {
                this.model = this.FeedService.editFeedModel;
                this.populateFieldNameMap();
                this.applyDefaults();

            }
        });

        if (this.versions) {
            this.$scope.$watch(() => {
                return this.FeedService.versionFeedModel;
            }, (newVal: any) => {
                this.versionFeedModel = this.FeedService.versionFeedModel;
            });
            this.$scope.$watch(() => {
                return this.FeedService.versionFeedModelDiff;
            }, (newVal: any) => {
                this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
            });
        }

        //Apply the entity access permissions
        this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access: any) => {
            this.allowEdit = !this.versions && access && !this.model.view.dataPolicies.disabled
        });

    }


    /**
     * apply default values to the read only model
     */
    private applyDefaults() {
        if (this.model.table.targetFormat === undefined || this.model.table.targetFormat === '' || this.model.table.targetFormat === null) {
            //default to ORC
            this.model.table.targetFormat = 'STORED AS ORC'
        }
        if (this.model.table.options.compressionFormat === undefined || this.model.table.options.compressionFormat === '' || this.model.table.options.compressionFormat === null) {
            this.model.table.options.compressionFormat = 'NONE'
        }
    }


    private populateFieldNameMap() {
        this.fieldNameMap = {};

        _.each(this.model.table.tableSchema.fields, (field) => {
            this.fieldNameMap[field['name']] = field;
        });
    }


    /**
     * Returns the readable display name for the mergeStrategy on the edited feed model
     * @returns {*}
     */
    mergeStrategyDisplayName(model: any) {
        if (model !== undefined && model.table !== undefined) { //model will be undefined when not displaying feed version for comparison
            var mergeStrategyObject = _.find(this.FeedService.mergeStrategies, (strategy: any) => {
                return strategy.type === model.table.targetMergeStrategy;
            });
            return mergeStrategyObject !== undefined ? mergeStrategyObject.name : model.table.targetMergeStrategy
        }
        return '';
    }

    /**
     * Enable/Disable the PK Merge Strategy
     */
    onChangePrimaryKey(): void {
        this.validateMergeStrategies();
    };

    onChangeMergeStrategy(): void {
        this.validateMergeStrategies();
    }


    shouldIndexingOptionsBeDisabled(): boolean {
        return ((this.model.historyReindexingStatus === 'IN_PROGRESS') || (this.model.historyReindexingStatus === 'DIRTY'));
    }

    shouldIndexingOptionsBeEnabled(): boolean {
        return !this.shouldIndexingOptionsBeDisabled();
    }

    onEdit() {
        //copy the model
        var fieldPolicies = angular.copy(this.FeedService.editFeedModel.table.fieldPolicies);
        var fields = angular.copy(this.FeedService.editFeedModel.table.tableSchema.fields);
        //assign the field to the policy
        var fieldMap = _.groupBy(fields, (field: any) => {
            return field.name
        });
        _.each(fieldPolicies, (policy: any) => {
            var columnDef = fieldMap[policy.name][0];
            policy.columnDef = columnDef;
            if (angular.isString(policy.domainTypeId) && policy.domainTypeId !== "") {
                policy.$currentDomainType = _.find(this.availableDomainTypes, (domainType) => {
                    return policy.domainTypeId === domainType.id;
                });
                if (angular.isUndefined(policy.$currentDomainType)) {
                    policy.domainTypeId = null;
                }
            }
        });

        this.editModel = {};
        this.editModel.fieldPolicies = fieldPolicies;

        this.editModel.table = {};
        this.editModel.table.tableSchema = {};
        this.editModel.table.tableSchema.fields = fields;
        this.editModel.table.targetFormat = this.FeedService.editFeedModel.table.targetFormat;
        if (this.editModel.table.targetFormat === undefined) {
            //default to ORC
            this.editModel.table.targetFormat = 'ORC'
        }
        this.editModel.table.targetMergeStrategy = this.FeedService.editFeedModel.table.targetMergeStrategy;
        this.editModel.table.options = angular.copy(this.FeedService.editFeedModel.table.options);

        if (this.editModel.table.options.compressionFormat === undefined) {
            this.editModel.table.options.compressionFormat = 'NONE'
        }

        this.editModel.options = angular.copy(this.FeedService.editFeedModel.options);
        this.indexCheckAll.setup(this.editModel);
        this.profileCheckAll.setup(this.editModel);

        this.editModel.historyReindexingStatus = this.FeedService.editFeedModel.historyReindexingStatus;

        this.$timeout(this.validateMergeStrategies.bind(this), 400);
    }

    onCancel(): void {

    }

    getAllFieldPolicies(field: any) {
        return this.FeedFieldPolicyRuleService.getAllPolicyRules(field);
    }

    getAllVersionedFieldPolicies(policyIndex: any) {
        return this.getAllFieldPolicies(this.findVersionedPolicy(policyIndex));
    }


    findVersionedRuleName(policyIndex: any, ruleIndex: any) {
        if (this.versionFeedModel && this.versionFeedModel.table && this.versionFeedModel.table.fieldPolicies) {
            var field = this.versionFeedModel.table.fieldPolicies[policyIndex];
            var rules = this.FeedFieldPolicyRuleService.getAllPolicyRules(field);
            if (ruleIndex < rules.length) {
                return rules[ruleIndex].name;
            }
        }
        return '';
    };

    onSave(ev: any): void {

        //Identify if any indexing options were changed
        var indexChanges = {};

        for (var i = 0; i < this.FeedService.editFeedModel.table.fieldPolicies.length; i++) {
            var fieldName = this.FeedService.editFeedModel.table.fieldPolicies[i].fieldName;
            var indexOption = this.FeedService.editFeedModel.table.fieldPolicies[i].index;

            if (this.editModel.fieldPolicies[i].fieldName == fieldName) {
                if (this.editModel.fieldPolicies[i].index != indexOption) {
                    indexChanges[this.editModel.fieldPolicies[i].fieldName] = this.editModel.fieldPolicies[i].index;
                }
            }
        }

        if (Object.keys(indexChanges).length > 0) {
            //Indexing options have changed
            this.FeedService.isKyloConfiguredForFeedHistoryDataReindexing()
                .then((response: angular.IHttpResponse<any>) => {
                    if (response.data === 'true') {
                        var displayIndexChanges = "";
                        var displayIndexChangedStatus = "";
                        var displayIndexChangedStatusIndicator = "&#128269"; //magnifying glass

                        //using styles does not render correctly.
                        displayIndexChanges += "<div><font color='grey'>Data indexing for fields will be updated as below, and take effect going forward.<br>"
                            + "Do you wish to apply these changes to historical data as well?</font></div><br>"
                            + "<div></div><table ><tr><td>&nbsp;&nbsp;&nbsp;</td></td><td><b>Field</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><b>Data Indexing</b></td></font></tr>";

                        for (var key in indexChanges) {
                            displayIndexChanges += "<tr>";
                            if (indexChanges[key] == true) {
                                displayIndexChangedStatus = "<font color='green'>enabled</font>";
                            } else {
                                displayIndexChangedStatus = "<font color='red'>disabled</font>";
                            }
                            displayIndexChanges += "<td>" + displayIndexChangedStatusIndicator + "&nbsp;&nbsp;&nbsp;</td>";
                            displayIndexChanges += "<td>" + key + "</td><td>&nbsp;&nbsp;&nbsp;</td><td><td'>" + displayIndexChangedStatus + "</td>";
                            displayIndexChanges += "</tr>";
                        }
                        displayIndexChanges += "</table></div>";

                        var confirm = this.$mdDialog.confirm()
                            .title("Apply indexing changes to history data?")
                            .htmlContent(displayIndexChanges)
                            .ariaLabel("Apply indexing changes to history data?")
                            .ok("Yes")
                            .cancel("No");

                        this.$mdDialog.show(confirm).then(() => {
                            this.editModel.historyReindexingStatus = 'DIRTY';
                            this.goAheadWithSave(ev, true); //indexing changed, kylo configured, user opt-in for history reindexing
                        }, () => {
                            this.goAheadWithSave(ev, false); //indexing changed, kylo configured, user opt-out for history reindexing
                        });
                    } else {
                        this.goAheadWithSave(ev, false); //indexing changed, kylo not configured
                    }
                }, (response: angular.IHttpResponse<any>) => {
                    console.log("Unable to determine if Kylo is configured to support data history reindexing. Please check Kylo services. Moving ahead assuming it is not configured.");
                    this.goAheadWithSave(ev, false);
                });
        } else {
            this.goAheadWithSave(ev, false); //indexing not changed
        }
    }


    showFieldRuleDialog(field: any): void {
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
    }

    /**
     * Gets the domain type with the specified id.
     *
     * @param {string} domainTypeId the domain type id
     * @returns {(DomainType|null)} the domain type
     */
    getDomainType(domainTypeId: any) {
        return _.find(this.availableDomainTypes, (domainType: any) => {
            return (domainType.id === domainTypeId);
        });
    }

    /**
     * Gets the placeholder HTML for the specified domain type option.
     *
     * @param {string} domainTypeId the domain type id
     * @returns {string} the placeholder HTML
     */
    getDomainTypePlaceholder(domainTypeId: any) {
        // Find domain type from id
        var domainType = null;
        if (angular.isString(domainTypeId) && domainTypeId !== "") {
            domainType = _.find(this.availableDomainTypes, (domainType) => {
                return (domainType.id === domainTypeId);
            });
        }

        // Generate the HTML
        if (angular.isObject(domainType)) {
            var element = $("<ng-md-icon/>")
                .attr("icon", domainType.icon)
                .attr("title", domainType.title)
                .css("fill", domainType.iconColor)
                .css("margin-left", "12px");
            return this.$sce.trustAsHtml(this.$compile(element)(this.$scope)[0].outerHTML);
        } else {
            return "";
        }
    }

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
        if ((domainType.field.derivedDataType !== null
                && (domainType.field.derivedDataType !== policy.columnDef.derivedDataType || domainType.field.precisionScale !== policy.columnDef.precisionScale))
            || (angular.isArray(policy.standardization) && policy.standardization.length > 0)
            || (angular.isArray(policy.columnDef.tags) && policy.columnDef.tags.length > 0)
            || (angular.isArray(policy.validation) && policy.validation.length > 0)) {
            this.$mdDialog.show({
                controller: "ApplyDomainTypeDialogController",
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                templateUrl: "js/feed-mgr/shared/apply-domain-type/apply-domain-type-dialog.html",
                locals: {
                    domainType: domainType,
                    field: policy.columnDef
                }
            })
                .then(() => {
                    this.FeedService.setDomainTypeForField(policy.columnDef, policy, domainType);
                }, () => {
                    policy.domainTypeId = angular.isDefined(policy.$currentDomainType) ? policy.$currentDomainType.id : null;
                });
        } else {
            this.FeedService.setDomainTypeForField(policy.columnDef, policy, domainType);
        }
    }

    /**
     * Shows the Edit Field dialog for the specified field.
     *
     * @param {Object} field the field to edit
     */
    showEditFieldDialog(field: any) {
        this.$mdDialog.show({
            controller: "EditFieldDialogController",
            escapeToClose: false,
            fullscreen: true,
            parent: angular.element(document.body),
            templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-edit-field-dialog.html",
            locals: {
                field: field
            }
        }).then(() => {
            field.$edited = true;
        });
    }

    diff(path: any) {
        return this.FeedService.diffOperation(path);
    }

    diffCollection(path: any) {
        return this.FeedService.diffCollectionOperation(path);
    }

    diffPolicies(policyIdx: any) {
        return this.FeedService.joinVersionOperations(this.FeedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/standardization'), this.FeedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/validation'));
    }


    private validateMergeStrategies() {
        var valid = this.FeedService.enableDisablePkMergeStrategy(this.editModel, this.mergeStrategies);
        this.editFeedDataPoliciesForm['targetMergeStrategy'].$setValidity('invalidPKOption', valid);

        valid = this.FeedService.enableDisableRollingSyncMergeStrategy(this.model, this.mergeStrategies);
        this.editFeedDataPoliciesForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', valid);
    }

    private findVersionedPolicy(policyIndex: any) {
        if (this.versionFeedModel && this.versionFeedModel.table && this.versionFeedModel.table.fieldPolicies) {
            return this.versionFeedModel.table.fieldPolicies[policyIndex];
        }
        return '';
    }


    private goAheadWithSave(ev: any, applyHistoryReindexing: any) {

        //save changes to the model
        this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-data-policies.Saving'), this.model.feedName);
        var copy = angular.copy(this.FeedService.editFeedModel);

        if (applyHistoryReindexing === true) {
            copy.historyReindexingStatus = this.editModel.historyReindexingStatus;
        } else {
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;
        }

        copy.table.targetFormat = this.editModel.table.targetFormat;
        copy.table.fieldPolicies = this.editModel.fieldPolicies;

        //add back in the changes to the pk, nullable, created, updated tracker columns
        var policyMap = _.groupBy(copy.table.fieldPolicies, (policy: any) => {
            return policy.name
        });
        _.each(copy.table.tableSchema.fields, (field: any) => {
            //find the respective changes in the ui object for this field
            var updatedColumnDef = policyMap[field.name] != undefined ? policyMap[field.name][0] : undefined;
            if (updatedColumnDef) {
                var def = updatedColumnDef.columnDef;
                angular.extend(field, def);
            }
        });
        //strip off the added 'columnDef' property
        _.each(this.editModel.fieldPolicies, (policy: any) => {
            policy.columnDef = undefined;
        });

        copy.table.targetMergeStrategy = this.editModel.table.targetMergeStrategy;
        copy.table.options = this.editModel.table.options;
        copy.userProperties = null;
        copy.options = this.editModel.options;

        this.FeedService.saveFeedModel(copy).then((response: any) => {
            this.FeedService.hideFeedSavingDialog();
            this.editableSection = false;
            //save the changes back to the model
            this.model.table.tableSchema.fields = copy.table.tableSchema.fields;
            this.model.table.targetFormat = this.editModel.table.targetFormat;
            this.model.table.fieldPolicies = this.editModel.fieldPolicies;
            this.model.table.targetMergeStrategy = this.editModel.table.targetMergeStrategy;
            this.model.table.options = this.editModel.table.options;
            this.model.options = this.editModel.options;
            //Get the updated value from the server.
            this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
            this.populateFieldNameMap();
        }, (response: any) => {
            this.FeedService.hideFeedSavingDialog();
            this.FeedService.buildErrorData(this.model.feedName, response);
            this.FeedService.showFeedErrorsDialog();
            //make it editable
            this.editableSection = true;
        });
    }

}


export class EditFieldDialogController implements ng.IComponentController {
// define(['angular', 'feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular, moduleName) {

    constructor($scope: any, $mdDialog: any, FeedTagService: any, field: any) {


        /**
         * Controls the Edit Field dialog.
         * @constructor
         */
        // var EditFieldDialogController = function () {

        /**
         * Provides a list of available tags.
         * @type {FeedTagService}
         */
        $scope.feedTagService = FeedTagService;

        /**
         * The field to edit.
         * @type {Object}
         */
        $scope.field = field;
        if (!angular.isArray(field.tags)) {
            field.tags = [];
        }

        /**
         * Metadata for the tag.
         * @type {{searchText: null, selectedItem: null}}
         */
        $scope.tagChips = {searchText: null, selectedItem: null};

        /**
         * Closes and rejects the dialog.
         */
        $scope.cancel = function () {
            $mdDialog.cancel();
        };

        /**
         * Closes and accepts the dialog.
         */
        $scope.hide = function () {
            $mdDialog.hide();
        };


        /**
         * Transforms the specified chip into a tag.
         * @param {string} chip the chip
         * @returns {Object} the tag
         */
        $scope.transformChip = function (chip:any) {
            return angular.isObject(chip) ? chip : {name: chip};
        };


    }
}

angular.module(moduleName)
    .controller('FeedDataPoliciesController',  Controller)
    .controller("EditFieldDialogController", ["$scope", "$mdDialog", "FeedTagService", "field", EditFieldDialogController])
    .directive('thinkbigFeedDataPolicies', directiveConfig);