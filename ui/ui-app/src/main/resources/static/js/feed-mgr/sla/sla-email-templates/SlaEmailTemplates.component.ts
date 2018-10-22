import SlaEmailTemplateService from "./SlaEmailTemplateService";
import AccessControlService from '../../../services/AccessControlService';
import AddButtonService from '../../../services/AddButtonService';
import { Component } from '@angular/core';
import StateService from '../../../services/StateService';
import { ITdDataTableColumn, TdDataTableService } from '@covalent/core/data-table';
import { BaseFilteredPaginatedTableView } from "../../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";

@Component({
    selector: 'sla-email-templates-controller',
    templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-templates.html'
})
export class SlaEmailTemplates extends BaseFilteredPaginatedTableView{

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
                private addButtonService: AddButtonService,
                private StateService: StateService,
                private slaEmailTemplateService: SlaEmailTemplateService,
                _dataTableService: TdDataTableService) {
                    super(_dataTableService);
                }

    /**
     * Displays the details of the specified template.
     *
     * @param event
     * @param template
     */
    editTemplate (template: any) {
        if (this.allowEdit && template != undefined) {
            this.slaEmailTemplateService.template = template;
            this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);

        } else {
            this.slaEmailTemplateService.accessDeniedDialog();
        }
    };

    getExistingTemplates () {

        var successFn = (response: any) => {
            this.loading = false;
            this.templates = response;

            super.setSortBy('name');
            super.setDataAndColumnSchema(this.templates,this.columns);
            super.filter();
        }
        var errorFn = (err: any) => {
            this.loading = false;
        }
        var promise = this.slaEmailTemplateService.getExistingTemplates();
        promise.then(successFn, errorFn);
        return promise;
    }
}