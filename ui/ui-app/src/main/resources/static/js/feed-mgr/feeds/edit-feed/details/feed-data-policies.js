define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directiveConfig = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-data-policies.html',
            controller: "FeedDataPoliciesController",
            link: function ($scope, element, attrs, controller) {
                if (angular.isUndefined($scope.versions)) {
                    $scope.versions = false;
                }
            }
        };
    };
    var CheckAll = /** @class */ (function () {
        function CheckAll(fieldName, isChecked) {
            this.fieldName = fieldName;
            this.isChecked = isChecked;
            this.isIndeterminate = false;
            this.totalChecked = 0;
        }
        CheckAll.prototype.setup = function (editModel) {
            var _this = this;
            this.editModel = editModel;
            this.totalChecked = 0;
            _.each(this.editModel.fieldPolicies, function (field) {
                if (field["profile"]) {
                    _this.totalChecked++;
                }
            });
            this.markChecked();
        };
        CheckAll.prototype.clicked = function (checked) {
            if (checked) {
                this.totalChecked++;
            }
            else {
                this.totalChecked--;
            }
        };
        CheckAll.prototype.markChecked = function () {
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
        };
        CheckAll.prototype.toggleAll = function () {
            var _this = this;
            var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
            if (angular.isDefined(this.editModel)) {
                _.each(this.editModel.fieldPolicies, function (field) {
                    field[_this.fieldName] = checked;
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
        };
        return CheckAll;
    }());
    exports.CheckAll = CheckAll;
    var Controller = /** @class */ (function () {
        function Controller($scope, $mdDialog, $timeout, $q, $compile, $sce, AccessControlService, EntityAccessControlService, FeedService, StateService, FeedFieldPolicyRuleService, DomainTypesService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$timeout = $timeout;
            this.$q = $q;
            this.$compile = $compile;
            this.$sce = $sce;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.FeedService = FeedService;
            this.StateService = StateService;
            this.FeedFieldPolicyRuleService = FeedFieldPolicyRuleService;
            this.$filter = $filter;
            /**
             * Flag to indicate if we are on the versions page or not
             * @type {boolean}
             */
            this.versions = false;
            /**
             * Indicates if the feed data policies may be edited.
             * @type {boolean}
             */
            this.allowEdit = !this.versions;
            /**
             * The Feed Model
             * @type {{} | any}
             */
            this.model = this.FeedService.editFeedModel;
            /**
             * The Model for versioning the feed
             */
            this.versionFeedModel = this.FeedService.versionFeedModel;
            /**
             * The differences between the model and version Feed Model
             * @type {any | Array}
             */
            this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
            /**
             * The angular form
             * @type {{}}
             */
            this.editFeedDataPoliciesForm = {};
            /**
             * Flag to indicate the section is editable
             * @type {boolean}
             */
            this.editableSection = false;
            /**
             * List of available domain types.
             * @type {DomainType[]}
             */
            this.availableDomainTypes = [];
            /**
             * The Feed Model in its Edit state.  When a user clicks the onEdit
             * @type {null}
             */
            this.editModel = null;
            this.versions = $scope.versions;
            this.profileCheckAll = new CheckAll('profile', true);
            this.indexCheckAll = new CheckAll('index', false);
            DomainTypesService.findAll().then(function (domainTypes) {
                _this.availableDomainTypes = domainTypes;
                // KYLO-251 Remove data type until schema evolution is supported
                domainTypes.forEach(function (domainType) {
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
        Controller.prototype.$onInit = function () {
            this.onInit();
        };
        Controller.prototype.onInit = function () {
            var _this = this;
            this.$scope.$watch(function () {
                return _this.FeedService.editFeedModel;
            }, function (newVal) {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = _this.FeedService.editFeedModel;
                    _this.populateFieldNameMap();
                    _this.applyDefaults();
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
            //Apply the entity access permissions
            this.$q.when(this.AccessControlService.hasPermission(this.EntityAccessControlService.FEEDS_EDIT, this.model, this.EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                _this.allowEdit = !_this.versions && access && !_this.model.view.dataPolicies.disabled;
            });
        };
        /**
         * apply default values to the read only model
         */
        Controller.prototype.applyDefaults = function () {
            if (this.model.table.targetFormat === undefined || this.model.table.targetFormat === '' || this.model.table.targetFormat === null) {
                //default to ORC
                this.model.table.targetFormat = 'STORED AS ORC';
            }
            if (this.model.table.options.compressionFormat === undefined || this.model.table.options.compressionFormat === '' || this.model.table.options.compressionFormat === null) {
                this.model.table.options.compressionFormat = 'NONE';
            }
        };
        Controller.prototype.populateFieldNameMap = function () {
            var _this = this;
            this.fieldNameMap = {};
            _.each(this.model.table.tableSchema.fields, function (field) {
                _this.fieldNameMap[field['name']] = field;
            });
        };
        /**
         * Returns the readable display name for the mergeStrategy on the edited feed model
         * @returns {*}
         */
        Controller.prototype.mergeStrategyDisplayName = function (model) {
            if (model !== undefined && model.table !== undefined) {
                var mergeStrategyObject = _.find(this.FeedService.mergeStrategies, function (strategy) {
                    return strategy.type === model.table.targetMergeStrategy;
                });
                return mergeStrategyObject !== undefined ? mergeStrategyObject.name : model.table.targetMergeStrategy;
            }
            return '';
        };
        /**
         * Enable/Disable the PK Merge Strategy
         */
        Controller.prototype.onChangePrimaryKey = function () {
            this.validateMergeStrategies();
        };
        ;
        Controller.prototype.onChangeMergeStrategy = function () {
            this.validateMergeStrategies();
        };
        Controller.prototype.shouldIndexingOptionsBeDisabled = function () {
            return ((this.model.historyReindexingStatus === 'IN_PROGRESS') || (this.model.historyReindexingStatus === 'DIRTY'));
        };
        Controller.prototype.shouldIndexingOptionsBeEnabled = function () {
            return !this.shouldIndexingOptionsBeDisabled();
        };
        Controller.prototype.onEdit = function () {
            var _this = this;
            //copy the model
            var fieldPolicies = angular.copy(this.FeedService.editFeedModel.table.fieldPolicies);
            var fields = angular.copy(this.FeedService.editFeedModel.table.tableSchema.fields);
            //assign the field to the policy
            var fieldMap = _.groupBy(fields, function (field) {
                return field.name;
            });
            _.each(fieldPolicies, function (policy) {
                var columnDef = fieldMap[policy.name][0];
                policy.columnDef = columnDef;
                if (angular.isString(policy.domainTypeId) && policy.domainTypeId !== "") {
                    policy.$currentDomainType = _.find(_this.availableDomainTypes, function (domainType) {
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
                this.editModel.table.targetFormat = 'ORC';
            }
            this.editModel.table.targetMergeStrategy = this.FeedService.editFeedModel.table.targetMergeStrategy;
            this.editModel.table.options = angular.copy(this.FeedService.editFeedModel.table.options);
            if (this.editModel.table.options.compressionFormat === undefined) {
                this.editModel.table.options.compressionFormat = 'NONE';
            }
            this.editModel.options = angular.copy(this.FeedService.editFeedModel.options);
            this.indexCheckAll.setup(this.editModel);
            this.profileCheckAll.setup(this.editModel);
            this.editModel.historyReindexingStatus = this.FeedService.editFeedModel.historyReindexingStatus;
            this.$timeout(this.validateMergeStrategies.bind(this), 400);
        };
        Controller.prototype.onCancel = function () {
        };
        Controller.prototype.getAllFieldPolicies = function (field) {
            return this.FeedFieldPolicyRuleService.getAllPolicyRules(field);
        };
        Controller.prototype.getAllVersionedFieldPolicies = function (policyIndex) {
            return this.getAllFieldPolicies(this.findVersionedPolicy(policyIndex));
        };
        Controller.prototype.findVersionedRuleName = function (policyIndex, ruleIndex) {
            if (this.versionFeedModel && this.versionFeedModel.table && this.versionFeedModel.table.fieldPolicies) {
                var field = this.versionFeedModel.table.fieldPolicies[policyIndex];
                var rules = this.FeedFieldPolicyRuleService.getAllPolicyRules(field);
                if (ruleIndex < rules.length) {
                    return rules[ruleIndex].name;
                }
            }
            return '';
        };
        ;
        Controller.prototype.onSave = function (ev) {
            var _this = this;
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
                    .then(function (response) {
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
                            }
                            else {
                                displayIndexChangedStatus = "<font color='red'>disabled</font>";
                            }
                            displayIndexChanges += "<td>" + displayIndexChangedStatusIndicator + "&nbsp;&nbsp;&nbsp;</td>";
                            displayIndexChanges += "<td>" + key + "</td><td>&nbsp;&nbsp;&nbsp;</td><td><td'>" + displayIndexChangedStatus + "</td>";
                            displayIndexChanges += "</tr>";
                        }
                        displayIndexChanges += "</table></div>";
                        var confirm = _this.$mdDialog.confirm()
                            .title("Apply indexing changes to history data?")
                            .htmlContent(displayIndexChanges)
                            .ariaLabel("Apply indexing changes to history data?")
                            .ok("Yes")
                            .cancel("No");
                        _this.$mdDialog.show(confirm).then(function () {
                            _this.editModel.historyReindexingStatus = 'DIRTY';
                            _this.goAheadWithSave(ev, true); //indexing changed, kylo configured, user opt-in for history reindexing
                        }, function () {
                            _this.goAheadWithSave(ev, false); //indexing changed, kylo configured, user opt-out for history reindexing
                        });
                    }
                    else {
                        _this.goAheadWithSave(ev, false); //indexing changed, kylo not configured
                    }
                }, function (response) {
                    console.log("Unable to determine if Kylo is configured to support data history reindexing. Please check Kylo services. Moving ahead assuming it is not configured.");
                    _this.goAheadWithSave(ev, false);
                });
            }
            else {
                this.goAheadWithSave(ev, false); //indexing not changed
            }
        };
        Controller.prototype.showFieldRuleDialog = function (field) {
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
        /**
         * Gets the domain type with the specified id.
         *
         * @param {string} domainTypeId the domain type id
         * @returns {(DomainType|null)} the domain type
         */
        Controller.prototype.getDomainType = function (domainTypeId) {
            return _.find(this.availableDomainTypes, function (domainType) {
                return (domainType.id === domainTypeId);
            });
        };
        /**
         * Gets the placeholder HTML for the specified domain type option.
         *
         * @param {string} domainTypeId the domain type id
         * @returns {string} the placeholder HTML
         */
        Controller.prototype.getDomainTypePlaceholder = function (domainTypeId) {
            // Find domain type from id
            var domainType = null;
            if (angular.isString(domainTypeId) && domainTypeId !== "") {
                domainType = _.find(this.availableDomainTypes, function (domainType) {
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
            }
            else {
                return "";
            }
        };
        /**
         * Display a confirmation when the domain type of a field is changed and there are existing standardizers and validators.
         *
         * @param {FieldPolicy} policy the field policy
         */
        Controller.prototype.onDomainTypeChange = function (policy) {
            var _this = this;
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
                    .then(function () {
                    _this.FeedService.setDomainTypeForField(policy.columnDef, policy, domainType);
                }, function () {
                    policy.domainTypeId = angular.isDefined(policy.$currentDomainType) ? policy.$currentDomainType.id : null;
                });
            }
            else {
                this.FeedService.setDomainTypeForField(policy.columnDef, policy, domainType);
            }
        };
        /**
         * Shows the Edit Field dialog for the specified field.
         *
         * @param {Object} field the field to edit
         */
        Controller.prototype.showEditFieldDialog = function (field) {
            this.$mdDialog.show({
                controller: "EditFieldDialogController",
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                templateUrl: "js/feed-mgr/feeds/edit-feed/feed-details-edit-field-dialog.html",
                locals: {
                    field: field
                }
            }).then(function () {
                field.$edited = true;
            });
        };
        Controller.prototype.diff = function (path) {
            return this.FeedService.diffOperation(path);
        };
        Controller.prototype.diffCollection = function (path) {
            return this.FeedService.diffCollectionOperation(path);
        };
        Controller.prototype.diffPolicies = function (policyIdx) {
            return this.FeedService.joinVersionOperations(this.FeedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/standardization'), this.FeedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/validation'));
        };
        Controller.prototype.validateMergeStrategies = function () {
            var valid = this.FeedService.enableDisablePkMergeStrategy(this.editModel, this.mergeStrategies);
            this.editFeedDataPoliciesForm['targetMergeStrategy'].$setValidity('invalidPKOption', valid);
            valid = this.FeedService.enableDisableRollingSyncMergeStrategy(this.model, this.mergeStrategies);
            this.editFeedDataPoliciesForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', valid);
        };
        Controller.prototype.findVersionedPolicy = function (policyIndex) {
            if (this.versionFeedModel && this.versionFeedModel.table && this.versionFeedModel.table.fieldPolicies) {
                return this.versionFeedModel.table.fieldPolicies[policyIndex];
            }
            return '';
        };
        Controller.prototype.goAheadWithSave = function (ev, applyHistoryReindexing) {
            var _this = this;
            //save changes to the model
            this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-data-policies.Saving'), this.model.feedName);
            var copy = angular.copy(this.FeedService.editFeedModel);
            if (applyHistoryReindexing === true) {
                copy.historyReindexingStatus = this.editModel.historyReindexingStatus;
            }
            else {
                //Server may have updated value. Don't send via UI.
                copy.historyReindexingStatus = undefined;
            }
            copy.table.targetFormat = this.editModel.table.targetFormat;
            copy.table.fieldPolicies = this.editModel.fieldPolicies;
            //add back in the changes to the pk, nullable, created, updated tracker columns
            var policyMap = _.groupBy(copy.table.fieldPolicies, function (policy) {
                return policy.name;
            });
            _.each(copy.table.tableSchema.fields, function (field) {
                //find the respective changes in the ui object for this field
                var updatedColumnDef = policyMap[field.name] != undefined ? policyMap[field.name][0] : undefined;
                if (updatedColumnDef) {
                    var def = updatedColumnDef.columnDef;
                    angular.extend(field, def);
                }
            });
            //strip off the added 'columnDef' property
            _.each(this.editModel.fieldPolicies, function (policy) {
                policy.columnDef = undefined;
            });
            copy.table.targetMergeStrategy = this.editModel.table.targetMergeStrategy;
            copy.table.options = this.editModel.table.options;
            copy.userProperties = null;
            copy.options = this.editModel.options;
            this.FeedService.saveFeedModel(copy).then(function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.editableSection = false;
                //save the changes back to the model
                _this.model.table.tableSchema.fields = copy.table.tableSchema.fields;
                _this.model.table.targetFormat = _this.editModel.table.targetFormat;
                _this.model.table.fieldPolicies = _this.editModel.fieldPolicies;
                _this.model.table.targetMergeStrategy = _this.editModel.table.targetMergeStrategy;
                _this.model.table.options = _this.editModel.table.options;
                _this.model.options = _this.editModel.options;
                //Get the updated value from the server.
                _this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
                _this.populateFieldNameMap();
            }, function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.FeedService.buildErrorData(_this.model.feedName, response);
                _this.FeedService.showFeedErrorsDialog();
                //make it editable
                _this.editableSection = true;
            });
        };
        Controller.$inject = ["$scope", "$mdDialog", "$timeout", "$q", "$compile", "$sce", "AccessControlService", "EntityAccessControlService", "FeedService", "StateService",
            "FeedFieldPolicyRuleService", "DomainTypesService", "$filter"];
        return Controller;
    }());
    exports.Controller = Controller;
    var EditFieldDialogController = /** @class */ (function () {
        // define(['angular', 'feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular, moduleName) {
        function EditFieldDialogController($scope, $mdDialog, FeedTagService, field) {
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
            $scope.tagChips = { searchText: null, selectedItem: null };
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
            $scope.transformChip = function (chip) {
                return angular.isObject(chip) ? chip : { name: chip };
            };
        }
        return EditFieldDialogController;
    }());
    exports.EditFieldDialogController = EditFieldDialogController;
    angular.module(moduleName)
        .controller('FeedDataPoliciesController', Controller)
        .controller("EditFieldDialogController", ["$scope", "$mdDialog", "FeedTagService", "field", EditFieldDialogController])
        .directive('thinkbigFeedDataPolicies', directiveConfig);
});
//# sourceMappingURL=feed-data-policies.js.map