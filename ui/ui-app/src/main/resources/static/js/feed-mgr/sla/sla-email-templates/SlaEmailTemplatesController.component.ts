import * as angular from 'angular';
import * as _ from 'underscore';
import SlaEmailTemplateService from "./SlaEmailTemplateService";
import AccessControlService from '../../../services/AccessControlService';
import { DefaultPaginationDataService } from '../../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../../services/TableOptionsService';
import AddButtonService from '../../../services/AddButtonService';
import { Component } from '@angular/core';
import StateService from '../../../services/StateService';
import { TdDataTableSortingOrder, ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';

@Component({
    selector: 'sla-email-templates-controller',
    templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-templates.html'
})
export class SlaEmailTemplatesController {

    /**
    * Indicates if templates are allowed to be edited.
    * @type {boolean}
    */
    allowEdit: boolean = false;

    templates: any[] = [];
    loading: boolean = true;
    cardTitle: string = 'SLA Email Templates';

    //Pagination DAta
    pageName: string = "sla-email-templates";
    paginationData: any;
    paginationId: string = 'sla-email-templates';
    
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;
    sortBy: string = 'name';

    pageSize: number = 2;
    fromRow: number = 1;
    searchTerm: string = '';

    filteredData: any[];
    filteredTotal: number = 0;

    currentPage: number = 1;

    columns: ITdDataTableColumn[] = [
        { name: 'name',  label: 'Template', sortable: true, filter: true },
        { name: 'enabled', label: 'Active', sortable: true, filter: true },
        { name: 'default', label: 'Default', sortable: true, filter: true}
      ];
      
    ngOnInit() {

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions)) {
                    this.addButtonService.registerAddButton("sla-email-templates", () => {
                        this.slaEmailTemplateService.newTemplate();
                        this.StateService.FeedManager().Sla().navigateToNewEmailTemplate();
                    });
                }
            });

        this.getExistingTemplates();

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
    }

    constructor(private accessControlService: AccessControlService,
                private paginationDataService: DefaultPaginationDataService,
                private tableOptionsService: DefaultTableOptionsService,
                private addButtonService: AddButtonService,
                private StateService: StateService,
                private slaEmailTemplateService: SlaEmailTemplateService,
                private _dataTableService: TdDataTableService) {}

    /**
     * Displays the details of the specified template.
     *
     * @param event
     * @param template
     */
    editTemplate = (template: any) => {
        if (this.allowEdit && template != undefined) {
            this.slaEmailTemplateService.template = template;
            this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);

        } else {
            this.slaEmailTemplateService.accessDeniedDialog();
        }
    };

    getExistingTemplates = () => {

        var successFn = (response: any) => {
            this.loading = false;
            this.templates = response.data;

            this.filteredData = this.templates;
            this.filteredTotal = this.templates.length;
            this.filter();
        }
        var errorFn = (err: any) => {
            this.loading = false;
        }
        var promise = this.slaEmailTemplateService.getExistingTemplates()
        promise.then(successFn, errorFn);
        return promise;
    }

    search(searchTerm: string): void {
        this.searchTerm = searchTerm;
        this.filter();
    }

    filter(): void {
        let newData: any[] = this.templates;
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