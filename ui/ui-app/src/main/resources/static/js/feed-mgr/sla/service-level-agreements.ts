import * as angular from 'angular';
import { moduleName } from './module-name';
import * as _ from 'underscore';
import AccessControlService from '../../services/AccessControlService';
import StateService from '../../services/StateService';
import { FeedService } from '../services/FeedService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import AddButtonService from '../../services/AddButtonService';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';


export default class ServiceLevelAgreements {
   
    newSla: any;
    slaId: any;
    feed: any;
    editRule: any = null;
    //Pagination DAta
    pageName: string = "service-level-agreements";
    paginationData: any = this.paginationDataService.paginationData(this.pageName);
    paginationId: any = 'service-level-agreements';

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

    cardTitle: string = "Service Level Agreements";

    cardLinksTitle: string = "Links";

    /**
     * show a progress bar indicating loading SLA list or individual SLA for edit
     * @type {boolean}
     */
    loading: boolean = true;

    renderFilter: boolean = true;

    loadingListMessage: string = "Loading Service Level Agreements";
    loadingSingleMessage: string = "Loading Service Level Agreement";


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

    EMPTY_RULE_TYPE: any = { name: '' };

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
    mode: any = 'NEW';

    userSuppliedName: boolean = false;

    userSuppliedDescription: boolean = false;

    currentPage: any;
    viewType: any;
    sortOptions: any;
    loadingMessage: any;

    

    ngOnInit() {

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

        // here 
        this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
        this.viewType = this.paginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();
        /**
         * Loading message
         */
        this.loadingMessage = this.loadingListMessage;
    }
    $onInit() {
        this.ngOnInit();
    }
   
    static readonly $inject = ["$scope", "$mdDialog", "$mdToast", "$http", "$rootScope", "$q",
    "StateService", "FeedService", "SlaService", "PolicyInputFormService",
    "PaginationDataService", "TableOptionsService", "AddButtonService",
    "AccessControlService", "EntityAccessControlService"];
    constructor(private $scope: IScope,
        private $mdDialog: angular.material.IDialogService,
        private $mdToast: angular.material.IToastService,
        private $http: angular.IHttpService,
        private $rootScope: IScope,
        private $q: angular.IQService,
        private stateService: StateService,
        private feedService: FeedService,
        private SlaService: any,
        private PolicyInputFormService: any,
        private paginationDataService: DefaultPaginationDataService,
        private tableOptionsService: DefaultTableOptionsService,
        private addButtonService: AddButtonService,
        private accessControlService: AccessControlService,
        private entityAccessControlService: EntityAccessControlService){

         //if the newSLA flag is tripped then show the new SLA form and then reset it

         if (this.newSla == null && this.newSla == undefined) {
            this.newSla = false;
        }

        $scope.$watch(() => {
            return this.newSla;
        }, (newVal: any) => {
            if (newVal == true) {
                this.onNewSla();
                this.newSla = false;
                this.loading = false;
            }
        });

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (accessControlService.hasAction(AccessControlService.SLA_EDIT, actionSet.actions)) {
                    this.addButtonService.registerAddButton("service-level-agreements", () => {
                        this.onNewSla();
                    });
                    this.allowCreate = true;
                }
            });

        $scope.$watch(() => {
            return this.viewType;
        }, (newVal: any) => {
            this.onViewTypeChange(newVal);
        });

        this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        if (this.feed != null) {
            this.paginationData.rowsPerPage = 100;
        }
        else {
            this.paginationData.rowsPerPage = 5;
        }
        if (this.slaId) {
            //fetch the sla
            SlaService.getSlaForEditForm(this.slaId).then((response: any) => {
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

        }
        else {
            /**
             * Initiall load the SLA list
             */
            this.loadSlas();
        }
        this.applyAccessPermissions();
    }

    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    loadSortOptions = () => {
        var options = { 'Name': 'name', 'Description': 'description' };
        var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'name', 'asc');
        this.tableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    onViewTypeChange = (viewType: any) => {
        this.paginationDataService.viewType(this.pageName, this.viewType);
    }

    onOrderChange = (order: any) => {
        this.paginationDataService.sort(this.pageName, order);
        this.tableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange = (page: any, limit: any) => {
        this.paginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption = (option: any) => {
        var sortString = this.tableOptionsService.toSortString(option);
        this.paginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
        this.tableOptionsService.setSortOption(this.pageName, sortString);
    }

    /**
     * Fetch the SLAs and populate the list.
     * If there is a Feed using this page it will only get the SLAs related for this feed
     * otherwise it will get all the SLas
     */
    loadSlas= () => {
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

    /**
     * Called when the user cancels a specific SLA
     */
    cancelEditSla = () => {
        this.showList(false);
        this.applyAccessPermissions();
        this.userSuppliedName = false;
        this.userSuppliedDescription = false;
    }

    addNewCondition = () => {
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

    addNewActionCondition = () => {
        this.addingSlaAction = true;
    }

    deriveSlaName =() => {

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

    deriveDescription = () => {
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

    onPropertyChange = (property: any) => {
        if (this.PolicyInputFormService.isFeedProperty(property)) {
            if (this.editSla != null && (this.userSuppliedName == false || (this.editSla.name == '' || this.editSla.name == null))) {
                this.editSla.name = this.deriveSlaName();
            }
            if (this.editSla != null && (this.userSuppliedDescription == false || (this.editSla.description == '' || this.editSla.description == null))) {
                this.editSla.description = this.deriveDescription();
            }
        }
    }

    onNameChange = () => {
        this.userSuppliedName = true;
    }

    onDescriptionChange = () => {
        this.userSuppliedDescription = true;
    }

    saveSla = () => {
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
            this.saveSlaSuccess(success, () => { });
        }
    }

    saveSlaSuccess = (successFn: any, failureFn: any) => {
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
            }, () => {
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
            }, () => {
                if (failureFn) {
                    failureFn();
                }
            });
        }
    }

    onBackToList = (ev: any) => {
        var requery = false;
        if (this.serviceLevelAgreements == null || this.serviceLevelAgreements.length == 0) {
            requery = true;
        }
        this.showList(requery);
        this.applyAccessPermissions();

    }

    onNewSla = () => {
        this.addButtonService.hideAddButton();
        this.mode = 'NEW';
        this.creatingNewSla = true;
        this.editSlaIndex = null;
        this.editSlaId = null;
        this.editSla = { name: '', description: '', rules: [], actionConfigurations: [] };
        this.addingSlaCondition = true;
        this.renderFilter = false;
        this.userSuppliedName = false;
        this.userSuppliedDescription = false;
        this.cardTitle = "New Service Level Agreement"

    }

    onEditSla = (sla: any) => {
        if (this.allowEdit) {
            this.addButtonService.hideAddButton();
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

    viewSlaAssessments = () => {
        if (this.editSla) {
            this.stateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId==' + this.editSla.id);
        }
    }

    loadAndEditSla = (slaId: any) => {
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

    applyEditPermissionsToSLA = (sla: any) => {

        var entityAccessControlled = this.accessControlService.isEntityAccessControlled();

        var functionalAccess = this.accessControlService.getUserAllowedActions()
        this.$q.when(functionalAccess).then((response: any) => {
            if (entityAccessControlled) {
                sla.editable = sla.canEdit;
                this.allowEdit = sla.canEdit;
            }
            else {
                var allowFeedEdit = this.feed != null ? this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.actions) : true;
                this.allowEdit = allowFeedEdit && this.accessControlService.hasAction(AccessControlService.SLA_EDIT, response.actions);
                sla.editable = this.allowEdit;
            }
        });
    }

    onDeleteSla = (ev: any) => {
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
                    this.showList(false);
                }, () => {
                    //alert delete error
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Error deleting SLA.')
                            .position('bottom left')
                            .hideDelay(3000)
                    );
                });

            }, () => {
                //cancelled confirm box
            });

        }
    }

    onDeleteSlaMetric = (index: any) => {
        //warn before delete
        this.editSla.rules.splice(index, 1);
        if (this.editSla.rules.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onDeleteSlaAction = (index: any) => {
        //warn before delete
        this.editSla.actionConfigurations.splice(index, 1);
        if (this.editSla.actionConfigurations.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onAddConditionRuleTypeChange = () => {
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

    onAddSlaActionChange = () => {
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

    /**
     * Validate the form before adding/editing a Rule for an SLA
     * @returns {boolean}
     */
    validateForm = () => {
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var ruleProperties: any[] = [];
        _.each(this.editSla.rules, (rule: any) => {
            _.each(rule.properties, (property: any) => {
                ruleProperties.push(property);
            });
        });

        var validForm = this.PolicyInputFormService.validateForm(this.slaForm, ruleProperties);
        return validForm;
    }

    buildDisplayString = () => {
        if (this.editRule != null) {
            var str = '';
            _.each(this.editRule.properties, (prop: any, idx: any) => {
                if (prop.type != 'currentFeed') {
                    //chain it to the display string
                    if (str != '') {
                        str += ';';
                    }
                    str += ' ' + prop.displayName;
                    var val = prop.value;
                    if ((val == null || val == undefined || val == '') && (prop.values != null && prop.values.length > 0)) {
                        val = _.map(prop.values, (labelValue: any) => {
                            return labelValue.value;
                        }).join(",");
                    }
                    str += ": " + val;
                }
            });
            this.editRule.propertyValuesDisplayString = str;
        }
    }

    applyAccessPermissions = () => {
        //Apply the entity access permissions
        //if its an existing SLA, check to see if we can edit it
        if (this.editSla && !this.newSla) {
            this.allowEdit = this.editSla.canEdit;
        }
        else {
            //ensure the user has the EDIT_SLA and if editing for a feed ensure the user has access to edit that feed

            var entityAccessControlled = this.feed != null && this.accessControlService.isEntityAccessControlled();

            //Apply the entity access permissions
            var requests = {
                entityEditAccess: entityAccessControlled == true ? this.feedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, this.feed) : true,
                functionalAccess: this.accessControlService.getUserAllowedActions()
            }
            this.$q.all(requests).then((response: any) => {
                var allowEditAccess = this.accessControlService.hasAction(AccessControlService.SLA_EDIT, response.functionalAccess.actions);
                var slaAccess = this.accessControlService.hasAction(AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                var allowFeedEdit = this.feed != null ? this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions) : true;
                this.allowEdit = response.entityEditAccess && allowEditAccess && slaAccess && allowFeedEdit;
            });

        }
    }
    showList = (requery: any) => {
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
            this.addButtonService.showAddButton();
        }

        this.applyAccessPermissions();

    }
    //   this.feed = FeedService.editFeedModel;
    filter = this.paginationDataService.filter(this.pageName);
}

angular.module(moduleName).component('thinkbigServiceLevelAgreements', {
    bindings: {
        feed: '=?',
        newSla: '=?',
        slaId: '=?',
        view: '@'
    },
    controller : ServiceLevelAgreements,
    templateUrl : 'js/feed-mgr/sla/service-level-agreements.html',
    controllerAs : 'vm'
})