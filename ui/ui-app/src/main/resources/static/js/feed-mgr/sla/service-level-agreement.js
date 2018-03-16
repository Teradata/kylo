define(["require", "exports", "angular", "./module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ServiceLevelAgreementController = /** @class */ (function () {
        function ServiceLevelAgreementController($scope, $mdDialog, $mdToast, $http, $rootScope, $q, StateService, FeedService, SlaService, PolicyInputFormService, PaginationDataService, TableOptionsService, AddButtonService, AccessControlService, EntityAccessControlService) {
            //if the newSLA flag is tripped then show the new SLA form and then reset it
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$http = $http;
            this.$rootScope = $rootScope;
            this.$q = $q;
            this.StateService = StateService;
            this.FeedService = FeedService;
            this.SlaService = SlaService;
            this.PolicyInputFormService = PolicyInputFormService;
            this.PaginationDataService = PaginationDataService;
            this.TableOptionsService = TableOptionsService;
            this.AddButtonService = AddButtonService;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            //Pagination DAta
            this.pageName = "service-level-agreements";
            this.paginationData = this.PaginationDataService.paginationData(this.pageName);
            this.paginationId = 'service-level-agreements';
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Name': 'name', 'Description': 'description' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'name', 'asc');
                this.TableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
            };
            /**
             * Indicates if editing SLAs is allowed.
             * @type {boolean}
             */
            this.allowEdit = false;
            /**
             * shows the (+) Add button when trying to assign an SLA that is not directly tied to a feed initially
             * @type {boolean}
             */
            this.allowCreate = false;
            this.cardTitle = "Service Level Agreements";
            this.cardLinksTitle = "Links";
            /**
             * show a progress bar indicating loading SLA list or individual SLA for edit
             * @type {boolean}
             */
            this.loading = true;
            this.loadingListMessage = "Loading Service Level Agreements";
            this.loadingSingleMessage = "Loading Service Level Agreement";
            // here 
            this.currentPage = this.PaginationDataService.currentPage(this.pageName) || 1;
            this.viewType = this.PaginationDataService.viewType(this.pageName);
            this.sortOptions = this.loadSortOptions();
            /**
             * Loading message
             */
            this.loadingMessage = this.loadingListMessage;
            this.showList = function (requery) {
                var _this = this;
                this.cardTitle = "Service Level Agreements";
                this.renderFilter = true;
                this.editSla = null;
                this.creatingNewSla = null;
                this.editSlaId = null;
                this.addingSlaCondition = false;
                this.editSlaIndex = null;
                if (requery && requery == true) {
                    this.loadSlas();
                }
                //Requery?
                if (this.feed == null && this.allowCreate) {
                    //display the add button if we are not in a given feed
                    this.AddButtonService.showAddButton();
                }
                /**
      * Load up the Metric Options for defining SLAs
      */
                this.SlaService.getPossibleSlaMetricOptions().then(function (response) {
                    var currentFeedValue = null;
                    if (_this.feed != null) {
                        currentFeedValue = _this.PolicyInputFormService.currentFeedValue(_this.feed);
                    }
                    _this.options = _this.PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
                    if (_this.allowCreate || _this.allowEdit) {
                        _this.PolicyInputFormService.stripNonEditableFeeds(_this.options);
                    }
                });
                /**
                 * Get all possible SLA Action Options
                 */
                this.SlaService.getPossibleSlaActionOptions().then(function (response) {
                    var currentFeedValue = null;
                    if (_this.feed != null) {
                        currentFeedValue = _this.PolicyInputFormService.currentFeedValue(_this.feed);
                    }
                    _this.slaActionOptions = _this.PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
                    if (_this.slaActionOptions.length > 0) {
                        _this.showActionOptions = true;
                        _.each(_this.slaActionOptions, function (action) {
                            //validate the rules
                            _this.SlaService.validateSlaActionRule(action);
                        });
                        if (_this.allowCreate || _this.allowEdit) {
                            _this.PolicyInputFormService.stripNonEditableFeeds(_this.slaActionOptions);
                        }
                    }
                    else {
                        _this.showActionOptions = false;
                    }
                });
                this.applyAccessPermissions();
            };
            //   this.feed = FeedService.editFeedModel;
            /**
             * SLA Options (aka. Metric Classes annotated with @ServiceLevelPolicy exposing the properties
             * @type {Array}
             */
            this.options = [];
            /**
             * The Form for validation
             * @type {{}}
             */
            this.slaForm = {};
            /**
             * Flag to indicated there are pending changes to be saved
             * @type {boolean}
             */
            this.pendingEdits = false;
            this.EMPTY_RULE_TYPE = { name: '' };
            /**
             * The current Rule type that is being edited (i.e. the @ServiceLevelAgreementPolicy#name of the current edit
             * @type {null}
             */
            this.ruleType = this.EMPTY_RULE_TYPE;
            /**
             * The Default Condition to be applied to the new Rule
             * REQUIRED = "AND"
             * SUFFICIENT = "OR"
             */
            this.ruleTypeCondition = 'REQUIRED';
            /**
             * flag to indicate if we should show the SLA Rule Type/Condition selection
             * @type {boolean}
             */
            this.addingSlaCondition = false;
            /**
             * The Current array of SLA objects found for this feed
             * This will be a copy of the agreements so they can be freely edited
             * [{name:'', rules:[{name:'',properties:[], condition:""}]}]
             * @type {Array}
             */
            this.serviceLevelAgreements = [];
            /**
             * The index of the ServiceLevelAgreement that is being edited  in reference o the this.serviceLevelAgreements array
             * @type {null}
             */
            this.editSlaIndex = null;
            /**
             * The ID of the SLA that is being modified/edited
             * @type {null}
             */
            this.editSlaId = null;
            /**
             * The SLA Object that is being created/edited
             * @type {null}
             */
            this.editSla = null;
            /**
             * Array of all the Action Configuration options available
             * @type {Array}
             */
            this.slaActionOptions = [];
            this.showActionOptions = false;
            /**
             * flag to indicated if we should show the Add Another Action button or not along with the Action dropdown
             * @type {boolean}
             */
            this.addingSlaAction = false;
            /**
             * The current SLA Action selected to edit/add
             * @type {{name: string}}
             */
            this.slaAction = this.EMPTY_RULE_TYPE;
            /**
             * flag to indicate we are creating a new sla to clear the message "no slas exist"
             * @type {boolean}
             */
            this.creatingNewSla = false;
            /**
             * Either NEW or EDIT
             * @type {string}
             */
            this.mode = 'NEW';
            this.userSuppliedName = false;
            this.userSuppliedDescription = false;
            this.filter = this.PaginationDataService.filter(this.pageName);
            this.onViewTypeChange = function (viewType) {
                this.PaginationDataService.viewType(this.pageName, this.viewType);
            };
            this.onOrderChange = function (order) {
                this.PaginationDataService.sort(this.pageName, order);
                this.TableOptionsService.setSortOption(this.pageName, order);
            };
            this.onPaginationChange = function (page, limit) {
                this.PaginationDataService.currentPage(this.pageName, null, page);
                this.currentPage = page;
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = this.TableOptionsService.toSortString(option);
                this.PaginationDataService.sort(this.pageName, sortString);
                var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
                this.TableOptionsService.setSortOption(this.pageName, sortString);
            };
            /**
             * Fetch the SLAs and populate the list.
             * If there is a Feed using this page it will only get the SLAs related for this feed
             * otherwise it will get all the SLas
             */
            this.loadSlas = function () {
                var _this = this;
                this.loadingMessage = this.loadingListMessage;
                this.loading = true;
                /**
                 * Load and copy the serviceLevelAgreements from the feed if available
                 * @type {Array|*}
                 */
                if (this.feed) {
                    var arr = this.feed.serviceLevelAgreements;
                    if (arr != null && arr != undefined) {
                        this.serviceLevelAgreements = angular.copy(arr);
                    }
                }
                if (this.feed != null) {
                    this.SlaService.getFeedSlas(this.feed.feedId).then(function (response) {
                        if (response.data && response.data != undefined && response.data.length > 0) {
                            _this.serviceLevelAgreements = response.data;
                        }
                        _this.loading = false;
                    });
                }
                else {
                    //get All Slas
                    this.SlaService.getAllSlas().then(function (response) {
                        _this.serviceLevelAgreements = response.data;
                        _this.loading = false;
                    });
                }
            };
            /**
             * Called when the user cancels a specific SLA
             */
            this.cancelEditSla = function () {
                this.showList();
                this.applyAccessPermissions();
                this.userSuppliedName = false;
                this.userSuppliedDescription = false;
            };
            this.addNewCondition = function () {
                this.ruleType = this.EMPTY_RULE_TYPE;
                //if editing one already validate, complete it and then add the new one
                var valid = true;
                if (this.editSla != null) {
                    valid = this.validateForm();
                }
                if (valid) {
                    //this will display the drop down to select the correct new rule/metric to assign to this SLA
                    this.addingSlaCondition = true;
                }
            };
            this.addNewActionCondition = function () {
                this.addingSlaAction = true;
            };
            this.deriveSlaName = function () {
                var feedNamesString = null;
                var feedNames = this.PolicyInputFormService.getFeedNames(this.editSla.rules);
                if (feedNames.length > 0) {
                    feedNamesString = feedNames.join(",");
                }
                var ruleNames = this.PolicyInputFormService.getRuleNames(this.editSla.rules);
                var slaName = ruleNames.join(",");
                if (feedNamesString != null) {
                    slaName = feedNamesString + " - " + slaName;
                }
                return slaName;
            };
            this.deriveDescription = function () {
                var feedNamesString = null;
                var feedNames = this.PolicyInputFormService.getFeedNames(this.editSla.rules);
                if (feedNames.length > 0) {
                    feedNamesString = feedNames.join(",");
                }
                var ruleNames = this.PolicyInputFormService.getRuleNames(this.editSla.rules);
                var desc = ruleNames.join(",");
                if (feedNamesString != null) {
                    desc += " for " + feedNamesString;
                }
                return desc;
            };
            this.onPropertyChange = function (property) {
                if (this.PolicyInputFormService.isFeedProperty(property)) {
                    if (this.editSla != null && (this.userSuppliedName == false || (this.editSla.name == '' || this.editSla.name == null))) {
                        this.editSla.name = this.deriveSlaName();
                    }
                    if (this.editSla != null && (this.userSuppliedDescription == false || (this.editSla.description == '' || this.editSla.description == null))) {
                        this.editSla.description = this.deriveDescription();
                    }
                }
            };
            this.onNameChange = function () {
                this.userSuppliedName = true;
            };
            this.onDescriptionChange = function () {
                this.userSuppliedDescription = true;
            };
            this.saveSla = function () {
                var _this = this;
                var valid = this.validateForm();
                if (valid) {
                    var success = function (response) {
                        if (response) {
                            _this.editSla.id = response.id;
                        }
                        if (_this.editSlaIndex != null) {
                            _this.serviceLevelAgreements[_this.editSlaIndex] = _this.editSla;
                        }
                        else {
                            _this.serviceLevelAgreements.push(_this.editSla);
                        }
                        _this.showList(true);
                        _this.$mdToast.show(_this.$mdToast.simple()
                            .textContent("Saved the SLA")
                            .hideDelay(1000));
                    };
                    this.saveSlaSuccess(success);
                }
            };
            this.saveSlaSuccess = function (successFn, failureFn) {
                var _this = this;
                this.$mdDialog.show(this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title('Saving SLA')
                    .textContent('Saving the Sla')
                    .ariaLabel('Saving Sla'));
                if (this.feed != null) {
                    this.SlaService.saveFeedSla(this.feed.feedId, this.editSla).then(function (response) {
                        _this.$mdDialog.hide();
                        if (successFn) {
                            successFn(response);
                        }
                    }, function () {
                        if (failureFn) {
                            failureFn();
                        }
                    });
                }
                else {
                    this.SlaService.saveSla(this.editSla).then(function () {
                        _this.$mdDialog.hide();
                        if (successFn) {
                            successFn();
                        }
                    }, function () {
                        if (failureFn) {
                            failureFn();
                        }
                    });
                }
            };
            this.onBackToList = function (ev) {
                var requery = false;
                if (this.serviceLevelAgreements == null || this.serviceLevelAgreements.length == 0) {
                    requery = true;
                }
                this.showList(requery);
                this.applyAccessPermissions();
            };
            this.onNewSla = function () {
                this.AddButtonService.hideAddButton();
                this.mode = 'NEW';
                this.creatingNewSla = true;
                this.editSlaIndex = null;
                this.editSlaId = null;
                this.editSla = { name: '', description: '', rules: [], actionConfigurations: [] };
                this.addingSlaCondition = true;
                this.renderFilter = false;
                this.userSuppliedName = false;
                this.userSuppliedDescription = false;
                this.cardTitle = "New Service Level Agreement";
            };
            this.onEditSla = function (sla) {
                if (this.allowEdit) {
                    this.AddButtonService.hideAddButton();
                    this.editSlaIndex = _.findIndex(this.serviceLevelAgreements, sla);
                    this.loadAndEditSla(sla.id);
                    this.renderFilter = false;
                }
                else {
                    this.$mdDialog.show(this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit SLAs.")
                        .ariaLabel("Access denied to edit SLAs.")
                        .ok("OK"));
                }
            };
            this.viewSlaAssessments = function () {
                if (this.editSla) {
                    this.StateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId==' + this.editSla.id);
                }
            };
            this.loadAndEditSla = function (slaId) {
                var _this = this;
                this.cardTitle = "Edit Service Level Agreement";
                this.mode = 'EDIT';
                this.creatingNewSla = false;
                this.editSlaId = slaId;
                this.ruleType = this.EMPTY_RULE_TYPE;
                this.addingSlaCondition = false;
                this.loadingMessage = this.loadingSingleMessage;
                this.loading = true;
                this.userSuppliedName = true;
                this.userSuppliedDescription = true;
                //fetch the SLA
                this.SlaService.getSlaForEditForm(slaId).then(function (response) {
                    var sla = response.data;
                    _this.applyEditPermissionsToSLA(sla);
                    _.each(sla.rules, function (rule) {
                        rule.editable = sla.canEdit;
                        rule.mode = 'EDIT';
                        rule.groups = this.PolicyInputFormService.groupProperties(rule);
                        this.PolicyInputFormService.updatePropertyIndex(rule);
                    });
                    _.each(sla.actionConfigurations, function (rule) {
                        rule.editable = sla.canEdit;
                        rule.mode = 'EDIT';
                        rule.groups = this.PolicyInputFormService.groupProperties(rule);
                        this.PolicyInputFormService.updatePropertyIndex(rule);
                        //validate the rules
                        this.SlaService.validateSlaActionRule(rule);
                    });
                    //sla.editable = sla.canEdit;
                    _this.editSla = sla;
                    _this.loading = false;
                    //  this.allowEdit = this.editSla.canEdit;
                }, function (err) {
                    var msg = err.data.message || 'Error loading the SLA';
                    this.loading = false;
                    this.$mdDialog.show(this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Error loading the SLA")
                        .textContent(msg)
                        .ariaLabel("Access denied to edit the SLA")
                        .ok("OK"));
                });
            };
            this.applyEditPermissionsToSLA = function (sla) {
                var _this = this;
                var entityAccessControlled = this.AccessControlService.isEntityAccessControlled();
                var functionalAccess = this.AccessControlService.getUserAllowedActions();
                this.$q.when(functionalAccess).then(function (response) {
                    if (entityAccessControlled) {
                        sla.editable = sla.canEdit;
                        _this.allowEdit = sla.canEdit;
                    }
                    else {
                        var allowFeedEdit = _this.feed != null ? _this.AccessControlService.hasAction(_this.AccessControlService.FEEDS_EDIT, response.actions) : true;
                        _this.allowEdit = allowFeedEdit && _this.AccessControlService.hasAction(_this.AccessControlService.SLA_EDIT, response.actions);
                        sla.editable = _this.allowEdit;
                    }
                });
            };
            this.onDeleteSla = function (ev) {
                var _this = this;
                //warn are you sure you want to delete?
                if (this.editSlaIndex != null || this.editSlaId != null) {
                    var confirm = this.$mdDialog.confirm()
                        .title('Delete SLA')
                        .textContent('Are you sure you want to Delete this SLA?')
                        .ariaLabel('Delete SLA')
                        .targetEvent(ev)
                        .ok('Please do it!')
                        .cancel('Nope');
                    this.$mdDialog.show(confirm).then(function () {
                        _this.SlaService.deleteSla(_this.editSla.id).then(function () {
                            _this.editSla = null;
                            if (_this.editSlaIndex != null) {
                                _this.serviceLevelAgreements.splice(_this.editSlaIndex, 1);
                            }
                            _this.$mdToast.show(_this.$mdToast.simple()
                                .textContent('SLA Deleted.')
                                .position('bottom left')
                                .hideDelay(3000));
                            _this.showList();
                        }, function () {
                            //alert delete error
                            this.$mdToast.show(this.$mdToast.simple()
                                .textContent('Error deleting SLA.')
                                .position('bottom left')
                                .hideDelay(3000));
                        });
                    }, function () {
                        //cancelled confirm box
                    });
                }
            };
            this.onDeleteSlaMetric = function (index) {
                //warn before delete
                this.editSla.rules.splice(index, 1);
                if (this.editSla.rules.length == 0) {
                    this.addingSlaCondition = true;
                }
            };
            this.onDeleteSlaAction = function (index) {
                //warn before delete
                this.editSla.actionConfigurations.splice(index, 1);
                if (this.editSla.actionConfigurations.length == 0) {
                    this.addingSlaCondition = true;
                }
            };
            this.onAddConditionRuleTypeChange = function () {
                if (this.ruleType != this.EMPTY_RULE_TYPE) {
                    //replace current sla rule if already editing
                    var newRule = angular.copy(this.ruleType);
                    newRule.mode = 'NEW';
                    //update property index
                    this.PolicyInputFormService.updatePropertyIndex(newRule);
                    newRule.condition = this.ruleTypeCondition;
                    newRule.editable = true;
                    this.editSla.rules.push(newRule);
                    this.addingSlaCondition = false;
                    this.ruleType = this.EMPTY_RULE_TYPE;
                    if ((this.userSuppliedName == false || (this.editSla.name == '' || this.editSla.name == null))) {
                        this.editSla.name = this.deriveSlaName();
                    }
                    if ((this.editSla.description == '' || this.editSla.description == null)) {
                        this.editSla.description = this.deriveDescription();
                    }
                }
            };
            this.onAddSlaActionChange = function () {
                if (this.slaAction != this.EMPTY_RULE_TYPE) {
                    //replace current sla rule if already editing
                    var newRule = angular.copy(this.slaAction);
                    newRule.mode = 'NEW';
                    //update property index
                    this.PolicyInputFormService.updatePropertyIndex(newRule);
                    newRule.editable = true;
                    this.editSla.actionConfigurations.push(newRule);
                    this.addingSlaAction = false;
                    this.slaAction = this.EMPTY_RULE_TYPE;
                }
            };
            /**
             * Validate the form before adding/editing a Rule for an SLA
             * @returns {boolean}
             */
            this.validateForm = function () {
                //loop through properties and determine if they are valid
                //the following _.some routine returns true if the items are invalid
                var ruleProperties = [];
                _.each(this.editSla.rules, function (rule) {
                    _.each(rule.properties, function (property) {
                        ruleProperties.push(property);
                    });
                });
                var validForm = this.PolicyInputFormService.validateForm(this.slaForm, ruleProperties);
                return validForm;
            };
            this.buildDisplayString = function () {
                if (this.editRule != null) {
                    var str = '';
                    _.each(this.editRule.properties, function (prop, idx) {
                        if (prop.type != 'currentFeed') {
                            //chain it to the display string
                            if (str != '') {
                                str += ';';
                            }
                            str += ' ' + prop.displayName;
                            var val = prop.value;
                            if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                                val = _.map(prop.values, function (labelValue) {
                                    return labelValue.value;
                                }).join(",");
                            }
                            str += ": " + val;
                        }
                    });
                    this.editRule.propertyValuesDisplayString = str;
                }
            };
            this.applyAccessPermissions = function () {
                var _this = this;
                //Apply the entity access permissions
                //if its an existing SLA, check to see if we can edit it
                if (this.editSla && !this.newSla) {
                    this.allowEdit = this.editSla.canEdit;
                }
                else {
                    //ensure the user has the EDIT_SLA and if editing for a feed ensure the user has access to edit that feed
                    var entityAccessControlled = this.feed != null && this.AccessControlService.isEntityAccessControlled();
                    //Apply the entity access permissions
                    var requests = {
                        entityEditAccess: entityAccessControlled == true ? this.FeedService.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, this.feed) : true,
                        functionalAccess: this.AccessControlService.getUserAllowedActions()
                    };
                    this.$q.all(requests).then(function (response) {
                        var allowEditAccess = _this.AccessControlService.hasAction(_this.AccessControlService.SLA_EDIT, response.functionalAccess.actions);
                        var slaAccess = _this.AccessControlService.hasAction(_this.AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                        var allowFeedEdit = _this.feed != null ? _this.AccessControlService.hasAction(_this.AccessControlService.FEEDS_EDIT, response.functionalAccess.actions) : true;
                        _this.allowEdit = response.entityEditAccess && allowEditAccess && slaAccess && allowFeedEdit;
                    });
                }
            };
            if (this.newSla == null && this.newSla == undefined) {
                this.newSla = false;
            }
            $scope.$watch(function () {
                return _this.newSla;
            }, function (newVal) {
                if (newVal == true) {
                    _this.onNewSla();
                    _this.newSla = false;
                    _this.loading = false;
                }
            });
            // Register Add button
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (AccessControlService.hasAction(AccessControlService.SLA_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton("service-level-agreements", function () {
                        _this.onNewSla();
                    });
                    _this.allowCreate = true;
                }
            });
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            if (this.feed != null) {
                this.paginationData.rowsPerPage = 100;
            }
            else {
                this.paginationData.rowsPerPage = 5;
            }
            if (this.slaId) {
                //fetch the sla
                SlaService.getSlaForEditForm(this.slaId).then(function (response) {
                    _this.applyEditPermissionsToSLA(response.data);
                    if (_this.allowEdit) {
                        _this.editSla = response.data;
                        _this.applyAccessPermissions();
                        _this.editSlaId = _this.slaId;
                        _this.onEditSla(_this.editSla);
                    }
                    else {
                        //
                    }
                });
            }
            else {
                /**
                 * Initiall load the SLA list
                 */
                this.loadSlas();
            }
            this.applyAccessPermissions();
        }
        return ServiceLevelAgreementController;
    }());
    exports.default = ServiceLevelAgreementController;
    angular.module(module_name_1.moduleName).controller('ServiceLevelAgreementController', ["$scope", "$mdDialog", "$mdToast", "$http", "$rootScope", "$q",
        "StateService", "FeedService", "SlaService", "PolicyInputFormService",
        "PaginationDataService", "TableOptionsService", "AddButtonService",
        "AccessControlService", "EntityAccessControlService",
        ServiceLevelAgreementController]);
    angular.module(module_name_1.moduleName)
        .directive('thinkbigServiceLevelAgreement', function () {
        return {
            restrict: "EA",
            bindToController: {
                feed: '=?',
                newSla: '=?',
                slaId: '=?'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: function (tElement, tAttrs) {
                if (tAttrs) {
                    if (tAttrs.view === 'all') {
                        return 'js/feed-mgr/sla/service-level-agreements.html';
                    }
                    if (tAttrs.view === 'feed') {
                        return 'js/feed-mgr/sla/feed-service-level-agreements.html';
                    }
                }
            },
            controller: "ServiceLevelAgreementController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    });
});
//# sourceMappingURL=service-level-agreement.js.map