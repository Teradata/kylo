import * as angular from 'angular';
import * as _ from 'underscore';
import AccessControlService from '../../services/AccessControlService';
import StateService from '../../services/StateService';
import { FeedService } from '../services/FeedService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import AddButtonService from '../../services/AddButtonService';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import { SlaService } from '../services/SlaService';
import { Input, Component, SimpleChanges, Inject, ViewContainerRef } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { ITdDataTableColumn, TdDataTableSortingOrder, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
import { PolicyInputFormService } from '../shared/field-policies-angular2/policy-input-form.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdDialogService } from '@covalent/core/dialogs';
import {Observable} from 'rxjs/Observable';

@Component({
    selector: 'thinkbig-feed-service-level-agreements',
    templateUrl: 'js/feed-mgr/sla/feed-service-level-agreements.html'
})
export default class FeedServiceLevelAgreements {
   
    @Input() newSla: any;
    @Input() slaId: any;
    @Input() feed: any;
    editRule: any = null;

    private newSlaObserver = new Subject();

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

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;
    sortBy: string = 'name';

    pageSize: number = 2;
    fromRow: number = 1;
    searchTerm: string = '';

    filteredData: any[];
    filteredTotal: number = 0;

    currentPage: number = 1;

    columns: ITdDataTableColumn[] = [
        { name: 'name',  label: 'Name', sortable: true, filter: true },
        { name: 'description', label: 'Description', sortable: true, filter: true },
        { name: 'feedNames', label: 'RelatedFeeds', sortable: true, filter: true}
      ];

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

    loadingMessage: any;

    

    ngOnInit() {

        /**
        * Load up the Metric Options for defining SLAs
        */
        this.SlaService.getPossibleSlaMetricOptions().then((response: any) => {

            var currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feed);
            }
            this.options = this.policyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.allowCreate || this.allowEdit) {
                this.policyInputFormService.stripNonEditableFeeds(this.options);
            }

        });

        /**
         * Get all possible SLA Action Options
         */
        this.SlaService.getPossibleSlaActionOptions().then((response: any) => {
            var currentFeedValue = null;
            if (this.feed != null) {
                currentFeedValue = this.policyInputFormService.currentFeedValue(this.feed);
            }
            this.slaActionOptions = this.policyInputFormService.groupPolicyOptions(response.data, currentFeedValue);
            if (this.slaActionOptions.length > 0) {
                this.showActionOptions = true;

                _.each(this.slaActionOptions, (action: any) => {
                    //validate the rules
                    this.SlaService.validateSlaActionRule(action);
                });

                if (this.allowCreate || this.allowEdit) {
                    this.policyInputFormService.stripNonEditableFeeds(this.slaActionOptions);
                }

            }
            else {
                this.showActionOptions = false;
            }
        });

        /**
         * Loading message
         */
        this.loadingMessage = this.loadingListMessage;
    }
    
    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.newSla.currentValue) {
            this.newSlaObserver.next(changes.newSla.currentValue);
        }
    }

    constructor(private stateService: StateService,
                private SlaService: SlaService,
                private paginationDataService: DefaultPaginationDataService,
                private tableOptionsService: DefaultTableOptionsService,
                private addButtonService: AddButtonService,
                private accessControlService: AccessControlService,
                private entityAccessControlService: EntityAccessControlService,
                private policyInputFormService : PolicyInputFormService,
                private _dataTableService: TdDataTableService,
                private feedService: FeedService,
                private snackBar: MatSnackBar,
                private _tdDialogService : TdDialogService,
                private viewContainerRef : ViewContainerRef){

         //if the newSLA flag is tripped then show the new SLA form and then reset it

         if (this.newSla == null && this.newSla == undefined) {
            this.newSla = false;
        }

        this.newSlaObserver.subscribe(()=>{
            this.onNewSla();
            this.newSla = false;
            this.loading = false;
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
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption(option: any) {
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
    loadSlas() {
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

                this.filteredData = this.serviceLevelAgreements;
                this.filteredTotal = this.serviceLevelAgreements.length;
                this.filter();

            });
        }
    }

    /**
     * Called when the user cancels a specific SLA
     */
    cancelEditSla() {
        this.showList(false);
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

    addNewActionCondition() {
        this.addingSlaAction = true;
    }

    deriveSlaName() {

        var feedNamesString = null;
        var feedNames = this.policyInputFormService.getFeedNames(this.editSla.rules);
        if (feedNames.length > 0) {
            feedNamesString = feedNames.join(",");
        }
        var ruleNames = this.policyInputFormService.getRuleNames(this.editSla.rules);
        var slaName = ruleNames.join(",");
        if (feedNamesString != null) {
            slaName = feedNamesString + " - " + slaName;
        }
        return slaName;
    }

    deriveDescription() {
        var feedNamesString = null;
        var feedNames = this.policyInputFormService.getFeedNames(this.editSla.rules);
        if (feedNames.length > 0) {
            feedNamesString = feedNames.join(",");
        }
        var ruleNames = this.policyInputFormService.getRuleNames(this.editSla.rules);
        var desc = ruleNames.join(",");
        if (feedNamesString != null) {
            desc += " for " + feedNamesString;
        }
        return desc;
    }

    onPropertyChange(property: any) {
        if (this.policyInputFormService.isFeedProperty(property)) {
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
                this.snackBar.open('Saved the SLA','OK',{duration : 1000});
            }
            this.saveSlaSuccess(success, () => { });
        }
    }

    saveSlaSuccess(successFn: any, failureFn: any) {
        this._tdDialogService.openAlert({
            viewContainerRef : this.viewContainerRef,
            message : "Saving the Sla",
            ariaLabel : "Saving Sla",
            title : "Saving SLA",
            disableClose : true
        });
        if (this.feed != null) {
            this.SlaService.saveFeedSla(this.feed.feedId, this.editSla).then((response: any) => {
                this._tdDialogService.closeAll();
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
                this._tdDialogService.closeAll();
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

    onBackToList(ev: any) {
        var requery = false;
        if (this.serviceLevelAgreements == null || this.serviceLevelAgreements.length == 0) {
            requery = true;
        }
        this.showList(requery);
        this.applyAccessPermissions();

    }

    onNewSla() {
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

    onEditSla(sla: any) {
        if (this.allowEdit) {
            this.addButtonService.hideAddButton();
            this.editSlaIndex = _.findIndex(this.serviceLevelAgreements, sla);
            this.loadAndEditSla(sla.id);
            this.renderFilter = false;
        } else {
            this._tdDialogService.openAlert({
                message : "You do not have access to edit SLAs.",
                ariaLabel : "Access denied to edit SLAs.",
                title : "Access Denied",
                disableClose : false,
                closeButton : "OK"
            });
        }
    };

    viewSlaAssessments() {
        if (this.editSla) {
            this.stateService.OpsManager().Sla().navigateToServiceLevelAssessments('slaId==' + this.editSla.id);
        }
    }

    loadAndEditSla(slaId: any) {
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
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
            });

            _.each(sla.actionConfigurations, (rule: any) => {
                rule.editable = sla.canEdit;
                rule.mode = 'EDIT'
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
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
            this._tdDialogService.openAlert({
                message : msg,
                title: "Error loading the SLA",
                disableClose: false,
                ariaLabel: "Access denied to edit the SLA",
                closeButton: "OK"
            });
        });
    }

    applyEditPermissionsToSLA(sla: any) {

        var entityAccessControlled = this.accessControlService.isEntityAccessControlled();

        this.accessControlService.getUserAllowedActions().then((response: any) => {
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

    onDeleteSla(ev: any) {
        //warn are you sure you want to delete?
        if (this.editSlaIndex != null || this.editSlaId != null) {
            this._tdDialogService.openConfirm({
                message : 'Are you sure you want to Delete this SLA?',
                title : 'Delete SLA',
                ariaLabel : 'Delete SLA',
                cancelButton : 'Nope',
                acceptButton : 'Please do it!',
            }).afterClosed().subscribe((accept : boolean) => {
                if(accept){
                    this.SlaService.deleteSla(this.editSla.id).then(() => {
                        this.editSla = null;
                        if (this.editSlaIndex != null) {
                            this.serviceLevelAgreements.splice(this.editSlaIndex, 1);
                        }
                        this.snackBar.open('SLA Deleted.','OK',{duration : 3000});
                        this.showList(false);
                    }, () => {
                        //alert delete error
                        this.snackBar.open('Error deleting SLA.','OK',{duration : 3000});
                    });
                }
            });
        }
    }

    onDeleteSlaMetric(index: any) {
        //warn before delete
        this.editSla.rules.splice(index, 1);
        if (this.editSla.rules.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onDeleteSlaAction(index: any) {
        //warn before delete
        this.editSla.actionConfigurations.splice(index, 1);
        if (this.editSla.actionConfigurations.length == 0) {
            this.addingSlaCondition = true;
        }
    }

    onAddConditionRuleTypeChange(ruleType: any) {
        this.ruleType = ruleType;
        if (this.ruleType != this.EMPTY_RULE_TYPE) {
            //replace current sla rule if already editing
            var newRule = angular.copy(this.ruleType);
            newRule.mode = 'NEW'
            //update property index
            this.policyInputFormService.updatePropertyIndex(newRule);

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
            this.policyInputFormService.updatePropertyIndex(newRule);

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
    validateForm() {
        //loop through properties and determine if they are valid
        //the following _.some routine returns true if the items are invalid
        var ruleProperties: any[] = [];
        _.each(this.editSla.rules, (rule: any) => {
            _.each(rule.properties, (property: any) => {
                ruleProperties.push(property);
            });
        });

        var validForm = this.policyInputFormService.validateForm(this.slaForm, ruleProperties);
        return validForm;
    }

    buildDisplayString() {
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

    applyAccessPermissions() {
        //Apply the entity access permissions
        //if its an existing SLA, check to see if we can edit it
        if (this.editSla && !this.newSla) {
            this.allowEdit = this.editSla.canEdit;
        }
        else {
            //ensure the user has the EDIT_SLA and if editing for a feed ensure the user has access to edit that feed

            var entityAccessControlled = this.feed != null && this.accessControlService.isEntityAccessControlled();

            //Apply the entity access permissions
            this.accessControlService.getUserAllowedActions().then((functionalAccess : any) => {
                var allowEditAccess = this.accessControlService.hasAction(AccessControlService.SLA_EDIT, functionalAccess.actions);
                var slaAccess = this.accessControlService.hasAction(AccessControlService.SLA_ACCESS, functionalAccess.actions);
                var allowFeedEdit = this.feed != null ? this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, functionalAccess.actions) : true;
                var entityEditAccess = entityAccessControlled == true ? this.feedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, this.feed) : true;
                this.allowEdit = entityEditAccess && allowEditAccess && slaAccess && allowFeedEdit;
            })
        }
    }
    showList(requery: any) {
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
    
    filter(): void {
        let newData: any[] = this.serviceLevelAgreements;
        let excludedColumns: string[] = this.columns
            .filter((column: ITdDataTableColumn) => {
                return ((column.filter === undefined && column.hidden === true) ||
                    (column.filter !== undefined && column.filter === false));
            }).map((column: ITdDataTableColumn) => {
                return column.name;
            });
        newData = this._dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
        this.filteredTotal = newData.length;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredData = newData;
    }

    onPaginationChange(pagingEvent: IPageChangeEvent): void {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.filter();
    }

    onSortOrderChange(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }
}