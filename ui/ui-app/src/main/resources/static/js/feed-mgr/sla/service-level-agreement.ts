import * as angular from 'angular';
import {moduleName} from './module-name';
import * as _ from 'underscore';

import {ListTableView} from "../../services/ListTableViewTypes";
import PaginationData = ListTableView.PaginationData;
import SortOption = ListTableView.SortOption;

export default class ServiceLevelAgreementController implements ng.IComponentController {
    /**
     * Flag to determine if we are dealing with a new SLA or not
     * @type {boolean}
     */
    newSla: boolean = false;

    /**
     * The sla id passed in from the directive
     */
    slaId: string;

    /**
     * The selected feed.
     * This can be undefined or null if we are creating an SLA without selecting a feed first
     */
    feed: any;

    /**
     * Indicates if editing SLAs is allowed.
     * @type {boolean}
     */
    allowEdit: boolean = false;

    /**
     * shows the (+) Add button when trying to assign an SLA that is not directly tied to a feed initially
     * @type {boolean}
     */
    allowCreate: boolean = false;

    /**
     * The title for the card
     * @type {string}
     */
    cardTitle: string = "Service Level Agreements";

    /**
     * the title for the right card
     * @type {string}
     */
    cardLinksTitle: string = "Links";

    /**
     * show a progress bar indicating loading SLA list or individual SLA for edit
     * @type {boolean}
     */
    loading: boolean = true;

    loadingListMessage: string = "Loading Service Level Agreements";

    loadingSingleMessage: string = "Loading Service Level Agreement";


    /**
     * Loading message
     */
    loadingMessage: any = this.loadingListMessage;


    //Pagination DAta
    /**
     * The unique page name for the PaginationDataService
     */
    pageName: string = "service-level-agreements";
    /**
     * The pagination Data
     */
    paginationData: PaginationData;
    /**
     * The unique id for the PaginationData
     */
    paginationId: string = 'service-level-agreements';
    /**
     * The current page
     */
    currentPage: number;
    /**
     * the view Type( table|list)
     */
    viewType: any;
    /**
     * Array of SortOptions
     */
    sortOptions: SortOption[];
    /**
     * The current filter
     */
    filter: string;

    renderFilter: boolean = false;


    /**
     * SLA Options (aka. Metric Classes annotated with @ServiceLevelPolicy exposing the properties
     * @type {Array}
     */
    options: any[] = [];

    /**
     * The Form for validation
     * @type {{}}
     */
    slaForm: any = {};

    /**
     * Flag to indicated there are pending changes to be saved
     * @type {boolean}
     */
    pendingEdits: boolean = false;

    EMPTY_RULE_TYPE: any = {name: ''};

    /**
     * The current Rule type that is being edited (i.e. the @ServiceLevelAgreementPolicy#name of the current edit
     * @type {null}
     */
    ruleType: any = this.EMPTY_RULE_TYPE;

    /**
     * The Default Condition to be applied to the new Rule
     * REQUIRED = "AND"
     * SUFFICIENT = "OR"
     */
    ruleTypeCondition: any = 'REQUIRED';

    /**
     * flag to indicate if we should show the SLA Rule Type/Condition selection
     * @type {boolean}
     */
    addingSlaCondition: boolean = false;

    /**
     * The Current array of SLA objects found for this feed
     * This will be a copy of the agreements so they can be freely edited
     * [{name:'', rules:[{name:'',properties:[], condition:""}]}]
     * @type {Array}
     */
    serviceLevelAgreements: any[] = [];

    /**
     * The index of the ServiceLevelAgreement that is being edited  in reference o the this.serviceLevelAgreements array
     * @type {null}
     */
    editSlaIndex: any = null;

    /**
     * The ID of the SLA that is being modified/edited
     * @type {null}
     */
    editSlaId: any = null;

    /**
     * The SLA Object that is being created/edited
     * @type {null}
     */
    editSla: any = null;

    /**
     * Array of all the Action Configuration options available
     * @type {Array}
     */
    slaActionOptions: any[] = []

    showActionOptions: boolean = false;

    /**
     * flag to indicated if we should show the Add Another Action button or not along with the Action dropdown
     * @type {boolean}
     */
    addingSlaAction: boolean = false;

    /**
     * The current SLA Action selected to edit/add
     * @type {{name: string}}
     */
    slaAction: any = this.EMPTY_RULE_TYPE;

    /**
     * flag to indicate we are creating a new sla to clear the message "no slas exist"
     * @type {boolean}
     */
    creatingNewSla: boolean = false;

    /**
     * Either NEW or EDIT
     * @type {string}
     */
    mode: string = 'NEW';

    /**
     * did the user modify the name of the sla
     * @type {boolean}
     */
    userSuppliedName: boolean = false;

    /**
     * did the user modify the desc of the sla
     * @type {boolean}
     */
    userSuppliedDescription: boolean = false;


    static $inject = ["$scope", "$mdDialog", "$mdToast", "$http",
        "$rootScope", "$q", "StateService", "FeedService", "SlaService",
        "PolicyInputFormService", "PaginationDataService", "TableOptionsService",
        "AddButtonService", "AccessControlService", "EntityAccessControlService"]

    constructor(private $scope: any, private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService, private $http: angular.IHttpService,
                private $rootScope: angular.IRootScopeService, private $q: angular.IQService, private StateService: any, private FeedService: any, private SlaService: any,
                private PolicyInputFormService: any, private PaginationDataService: ListTableView.PaginationDataService, private TableOptionsService: ListTableView.TableOptionService,
                private AddButtonService: any, private AccessControlService: any, private EntityAccessControlService: any) {
        // Register Add button
        AccessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (AccessControlService.hasAction(AccessControlService.SLA_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton("service-level-agreements", () => {
                        this.onNewSla();
                    });
                    this.allowCreate = true;
                }
            });


        this.paginationData = this.PaginationDataService.paginationData(this.pageName);

        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = PaginationDataService.currentPage(this.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();
        this.filter = PaginationDataService.filter(this.pageName);

        //rebind the controller back to this when a property changes
        this.onPropertyChange = this.onPropertyChange.bind(this);

        this.onOrderChange = this.onOrderChange.bind(this);
        this.onPaginationChange = this.onPaginationChange.bind(this)
    }

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit(): void {

        this.loading = true;
        if (this.newSla == null && this.newSla == undefined) {
            this.newSla = false;
        }

        this.$scope.$watch(() => {
            return this.newSla;
        }, (newVal: any) => {
            if (newVal == true) {
                this.onNewSla();
                this.newSla = false;
                this.loading = false;
            }
        });


        this.$scope.$watch(() => {
            return this.viewType;
        }, (newVal: any) => {
            this.onViewTypeChange(newVal);
        });


        if (this.feed != null) {
            this.paginationData.rowsPerPage = 100;
        } else {
            this.paginationData.rowsPerPage = 5;
        }

        if (this.slaId) {
            //fetch the sla
            this.SlaService.getSlaForEditForm(this.slaId).then((response: any) => {
                this.applyEditPermissionsToSLA(response.data);
                if (this.allowEdit) {
                    this.editSla = response.data;
                    this.applyAccessPermissions()
                    this.editSlaId = this.slaId;
                    this.onEditSla(this.editSla);
                }
                else {
                    //
                }
            })

        } else {
            /**
             * Initiall load the SLA list
             */
            this.loadSlas();
        }


        /**
         * Load up the Metric Options for defining SLAs
         */
        this.SlaService.getPossibleSlaMetricOptions().then((response: any) => {

            var currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.PolicyInputFormService.currentFeedValue(this.feed);
            }
            this.options = this.PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.allowCreate || this.allowEdit) {
                this.PolicyInputFormService.stripNonEditableFeeds(this.options);
            }

        });

        /**
         * Get all possible SLA Action Options
         */
        this.SlaService.getPossibleSlaActionOptions().then((response: any) => {
            var currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.PolicyInputFormService.currentFeedValue(this.feed);
            }
            this.slaActionOptions = this.PolicyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.slaActionOptions.length > 0) {
                this.showActionOptions = true;

                _.each(this.slaActionOptions, (action: any) => {
                    //validate the rules
                    this.SlaService.validateSlaActionRule(action);
                });

                if (this.allowCreate || this.allowEdit) {
                    this.PolicyInputFormService.stripNonEditableFeeds(this.slaActionOptions);
                }

            }
            else {
                this.showActionOptions = false;
            }
        });


        this.applyAccessPermissions();


    }


    onViewTypeChange(viewType: any) {
        this.PaginationDataService.viewType(this.pageName, this.viewType);
    }

    onOrderChange(order: any) {
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange(page: any, limit: any) {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption(option: any) {
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    }


    /**
     * Called when the user cancels a specific SLA
     */
    cancelEditSla() {
        this.showList();
        this.applyAccessPermissions();
        this.userSuppliedName = false;
        this.userSuppliedDescription = false;
    }

    addNewCondition() {
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

    }


    onPropertyChange(property: any) {
        if (this.PolicyInputFormService.isFeedProperty(property)) {
            if (this.editSla != null && (this.userSuppliedName == false || (this.editSla.name == '' || this.editSla.name == null))) {
                this.editSla.name = this.deriveSlaName();
            }
            if (this.editSla != null && (this.userSuppliedDescription == false || (this.editSla.description == '' || this.editSla.description == null))) {
                this.editSla.description = this.deriveDescription();
            }
        }
    }

    onNameChange() {
        this.userSuppliedName = true;
    }

    onDescriptionChange() {
        this.userSuppliedDescription = true;
    }

    saveSla() {
        var valid = this.validateForm();
        if (valid) {
            let success = (response: any) => { //success
                if (response) {
                    this.editSla.id = response.id;
                }
                if (this.editSlaIndex != null) {
                    this.serviceLevelAgreements[this.editSlaIndex] = this.editSla;
                }
                else {
                    this.serviceLevelAgreements.push(this.editSla);
                }
                this.showList(true);
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent("Saved the SLA")
                        .hideDelay(1000)
                );
            }
            this._saveSla(success)
        }
    }


    /**
     * When a user goes back to the SLA list from a single SLA
     * @param ev
     */
    onBackToList(ev: any) {
        var requery = false;
        if (this.serviceLevelAgreements == null || this.serviceLevelAgreements.length == 0) {
            requery = true;
        }
        this.showList(requery);
        this.applyAccessPermissions();

    }

    /**
     * When a user clicks to start a new SLA
     */
    onNewSla() {
        this.AddButtonService.hideAddButton();
        this.mode = 'NEW';
        this.creatingNewSla = true;
        this.editSlaIndex = null;
        this.editSlaId = null;
        this.editSla = {name: '', description: '', rules: [], actionConfigurations: []};
        this.addingSlaCondition = true;
        this.renderFilter = false;
        this.userSuppliedName = false;
        this.userSuppliedDescription = false;
        this.cardTitle = "New Service Level Agreement"

    }

    /**
     * When a user clicks on an SLA to edit
     * @param sla
     */
    onEditSla(sla: any) {
        if (this.allowEdit) {
            this.AddButtonService.hideAddButton();
            this.editSlaIndex = _.findIndex(this.serviceLevelAgreements, sla);
            this.loadAndEditSla(sla.id);
            this.renderFilter = false;
        } else {
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Access Denied")
                    .textContent("You do not have access to edit SLAs.")
                    .ariaLabel("Access denied to edit SLAs.")
                    .ok("OK")
            );
        }
    };


    onDeleteSla(ev: any) {
        //warn are you sure you want to delete?
        if (this.editSlaIndex != null || this.editSlaId != null) {
            var confirm = this.$mdDialog.confirm()
                .title('Delete SLA')
                .textContent('Are you sure you want to Delete this SLA?')
                .ariaLabel('Delete SLA')
                .targetEvent(ev)
                .ok('Please do it!')
                .cancel('Nope');
            this.$mdDialog.show(confirm).then(() => {
                this.SlaService.deleteSla(this.editSla.id).then(() => {
                    this.editSla = null;
                    if (this.editSlaIndex != null) {
                        this.serviceLevelAgreements.splice(this.editSlaIndex, 1);
                    }
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('SLA Deleted.')
                            .position('bottom left')
                            .hideDelay(3000)
                    );
                    this.showList();
                }, () => {
                    //alert delete error
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Error deleting SLA.')
                            .position('bottom left')
                            .hideDelay(3000)
                    );
                });

            }, function () {
                //cancelled confirm box
            });

        }
    }

    onDeleteSlaMetric(index: number) {
        //warn before delete
        this.editSla.rules.splice(index, 1);
        if (this.editSla.rules.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onDeleteSlaAction(index: number) {
        //warn before delete
        this.editSla.actionConfigurations.splice(index, 1);
        if (this.editSla.actionConfigurations.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onAddConditionRuleTypeChange() {
        if (this.ruleType != this.EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            var newRule = angular.copy(this.ruleType);
            newRule.mode = 'NEW'
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
    }

    onAddSlaActionChange() {
        if (this.slaAction != this.EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            var newRule = angular.copy(this.slaAction);
            newRule.mode = 'NEW'
            //update property index
            this.PolicyInputFormService.updatePropertyIndex(newRule);

            newRule.editable = true;
            this.editSla.actionConfigurations.push(newRule);
            this.addingSlaAction = false;
            this.slaAction = this.EMPTY_RULE_TYPE;
        }
    }

    viewSlaAssessments() {
        if (this.editSla) {
            this.StateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId==' + this.editSla.id);
        }
    }


    private loadAndEditSla(slaId: string) {
        this.cardTitle = "Edit Service Level Agreement"
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
        this.SlaService.getSlaForEditForm(slaId).then((response: any) => {
            var sla = response.data;
            this.applyEditPermissionsToSLA(sla);
            _.each(sla.rules, (rule: any) => {
                rule.editable = sla.canEdit;
                rule.mode = 'EDIT'
                rule.groups = this.PolicyInputFormService.groupProperties(rule);
                this.PolicyInputFormService.updatePropertyIndex(rule);
            });

            _.each(sla.actionConfigurations, (rule: any) => {
                rule.editable = sla.canEdit;
                rule.mode = 'EDIT'
                rule.groups = this.PolicyInputFormService.groupProperties(rule);
                this.PolicyInputFormService.updatePropertyIndex(rule);
                //validate the rules
                this.SlaService.validateSlaActionRule(rule)

            });
            //sla.editable = sla.canEdit;
            this.editSla = sla;
            this.loading = false;
            //  this.allowEdit = this.editSla.canEdit;

        }, (err: any) => {
            var msg = err.data.message || 'Error loading the SLA';
            this.loading = false;
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Error loading the SLA")
                    .textContent(msg)
                    .ariaLabel("Access denied to edit the SLA")
                    .ok("OK")
            );

        });
    }

    private applyEditPermissionsToSLA(sla: any) {

        var entityAccessControlled = this.AccessControlService.isEntityAccessControlled();

        var functionalAccess = this.AccessControlService.getUserAllowedActions()
        this.$q.when(functionalAccess).then((response: any) => {
            if (entityAccessControlled) {
                sla.editable = sla.canEdit;
                this.allowEdit = sla.canEdit;
            }
            else {
                var allowFeedEdit = this.feed != null ? this.AccessControlService.hasAction(this.AccessControlService.FEEDS_EDIT, response.actions) : true;
                this.allowEdit = allowFeedEdit && this.AccessControlService.hasAction(this.AccessControlService.SLA_EDIT, response.actions);
                sla.editable = this.allowEdit;
            }
        });
    }

    /**
     * Validate the form before adding/editing a Rule for an SLA
     * @returns {boolean}
     */
    private validateForm() {
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var ruleProperties: any[] = [];
        _.each(this.editSla.rules, function (rule: any) {
            _.each(rule.properties, function (property: any) {
                ruleProperties.push(property);
            });
        });

        var validForm = this.PolicyInputFormService.validateForm(this.slaForm, ruleProperties);
        return validForm;
    }


    private _saveSla(successFn: any, failureFn?: any) {
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title('Saving SLA')
                .textContent('Saving the Sla')
                .ariaLabel('Saving Sla')
        );
        if (this.feed != null) {
            this.SlaService.saveFeedSla(this.feed.feedId, this.editSla).then((response: any) => {
                this.$mdDialog.hide();
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
            this.SlaService.saveSla(this.editSla).then(() => {
                this.$mdDialog.hide();
                if (successFn) {
                    successFn();
                }
            }, function () {
                if (failureFn) {
                    failureFn();
                }
            });
        }
    }


    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    private loadSortOptions() {
        var options = {'Name': 'name', 'Description': 'description'};
        var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'name', 'asc');
        this.TableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    private showList(requery?: boolean) {
        this.cardTitle = "Service Level Agreements"
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
    }

    /**
     * Fetch the SLAs and populate the list.
     * If there is a Feed using this page it will only get the SLAs related for this feed
     * otherwise it will get all the SLas
     */
    private loadSlas() {
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
            this.SlaService.getFeedSlas(this.feed.feedId).then((response: any) => {
                if (response.data && response.data != undefined && response.data.length > 0) {
                    this.serviceLevelAgreements = response.data;
                }
                this.loading = false;
            });
        }
        else {
            //get All Slas
            this.SlaService.getAllSlas().then((response: any) => {
                this.serviceLevelAgreements = response.data;
                this.loading = false;
            });
        }
    }

    private deriveSlaName() {

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
    }

    private deriveDescription() {
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
    }

    /**
     * May not be used.. possible delete
     * @deprecated

     private buildDisplayString() {
        if (this.editRule != null) {
            var str = '';
            _.each(this.editRule.properties, function (prop: any, idx: any) {
                if (prop.type != 'currentFeed') {
                    //chain it to the display string
                    if (str != '') {
                        str += ';';
                    }
                    str += ' ' + prop.displayName;
                    var val = prop.value;
                    if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                        val = _.map(prop.values, function (labelValue: any) {
                            return labelValue.value;
                        }).join(",");
                    }
                    str += ": " + val;
                }
            });
            this.editRule.propertyValuesDisplayString = str;
        }
    }
     */

    private applyAccessPermissions() {
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
            }
            this.$q.all(requests).then((response: any) => {
                var allowEditAccess = this.AccessControlService.hasAction(this.AccessControlService.SLA_EDIT, response.functionalAccess.actions);
                var slaAccess = this.AccessControlService.hasAction(this.AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                var allowFeedEdit = this.feed != null ? this.AccessControlService.hasAction(this.AccessControlService.FEEDS_EDIT, response.functionalAccess.actions) : true;
                this.allowEdit = response.entityEditAccess && allowEditAccess && slaAccess && allowFeedEdit;
            });

        }
    }
}

angular.module(moduleName).controller('ServiceLevelAgreementController', ServiceLevelAgreementController);
angular.module(moduleName)
    .directive('thinkbigServiceLevelAgreement',
        () => {
            return {
                restrict: "EA",
                bindToController: {
                    feed: '=?',
                    newSla: '=?',
                    slaId: '=?'
                },
                controllerAs: 'vm',
                scope: {},
                templateUrl: function (tElement: any, tAttrs: any) {
                    if (tAttrs) {
                        if (tAttrs.view === 'all') {
                            return 'js/feed-mgr/sla/service-level-agreements.html'
                        }
                        if (tAttrs.view === 'feed') {
                            return 'js/feed-mgr/sla/feed-service-level-agreements.html'
                        }
                    }
                },
                controller: "ServiceLevelAgreementController",
                link: function ($scope: any, element: any, attrs: any, controller: any) {
                }
            };
        });
