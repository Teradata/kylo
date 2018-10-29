import * as _ from 'underscore';
import AccessControlService from "../../services/AccessControlService";
import { ListTableView } from "../../services/ListTableViewTypes";
import StateService from '../../services/StateService';
import { RegisterTemplateServiceFactory } from '../services/RegisterTemplateServiceFactory';
import { Component, Inject } from '@angular/core';
import { RestUrlService } from '../services/RestUrlService';
import AddButtonService from '../../services/AddButtonService';
import { TdDataTableSortingOrder, ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'registered-templates-controller',
    templateUrl: 'js/feed-mgr/templates/registered-templates.html'
})
export class RegisteredTemplatesController extends BaseFilteredPaginatedTableView{

    /**
     * Indicates if templates are allowed to be edited.
     * @type {boolean}
     */
    allowEdit: boolean = false;
    /**
     * Indicates if templates are allowed to be exported.
     * @type {boolean}
     */
    allowExport: boolean = false;
    /**
     * Array of templates
     */
    registeredTemplates: any[] = [];
    /**
     * boolean indicating loading
     */
    loading: boolean = true;
    /**
     * the title of the card
     */
    cardTitle: string = "Templates";
    /**
     * The unique page name for the PaginationDataService
     */
    pageName: string = "registered-templates";
    
    columns: ITdDataTableColumn[] = [
        { name: 'templateName',  label: 'Template', sortable: true, filter: true },
        { name: 'properties', label: '# of Properties', sortable: true, filter: true },
        { name: 'updateDate', label: 'Last Updated', sortable: true, filter: true},
        { name: 'export', label: '', sortable: false, filter: false}
      ];

    constructor(private accessControlService: AccessControlService, 
                private RestUrlService: RestUrlService, 
                private AddButtonService: AddButtonService, 
                private StateService: StateService, 
                private RegisterTemplateService: RegisterTemplateServiceFactory,
                private http: HttpClient,
                public _dataTableService: TdDataTableService) {
                    super(_dataTableService);
                }

    /**
     * Initialize the controller and properties
     */
    ngOnInit() {

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.AddButtonService.registerAddButton("registered-templates", () => {
                        this.RegisterTemplateService.resetModel();
                        this.StateService.FeedManager().Template().navigateToRegisterNewTemplate();
                    });
                }
            });

        this.getRegisteredTemplates();

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowExport = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });

    }

    /**
     * Displays the details of the specified template.
     *
     * @param event
     * @param template
     */
    templateDetails (template: any) {
        if (this.allowEdit && template != undefined) {
            this.RegisterTemplateService.resetModel();

            // this.$injector.get("$q").when(this.RegisterTemplateService.hasEntityAccess([AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE], template)).then((hasAccess: boolean) => {
                if (this.RegisterTemplateService.hasEntityAccess([AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE], template)) {
                    this.StateService.FeedManager().Template().navigateToRegisteredTemplate(template.id, template.nifiTemplateId);
                }
                else {
                    this.RegisterTemplateService.accessDeniedDialog();
                }
            // });

        } else {
            this.RegisterTemplateService.accessDeniedDialog();
        }
    };

    getRegisteredTemplates (): Promise<any> {

        let successFn = (response :any) => {
            this.loading = false;
            if (response) {
                var entityAccessControlled = this.accessControlService.isEntityAccessControlled();
                _.forEach(response, (template: any) => {
                    template.allowExport = !entityAccessControlled || this.RegisterTemplateService.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.EXPORT, template);
                    template.exportUrl = this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id;
                });
            }
            this.registeredTemplates = response;
            super.setSortBy('templateName');
            super.setDataAndColumnSchema(this.registeredTemplates,this.columns);
            super.filter();
        };
        let errorFn = (err: any) => {
            super.setSortBy('templateName');
            super.setDataAndColumnSchema([],this.columns);
            super.filter();
            this.loading = false;

        };
        return this.http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL).toPromise().then(successFn, errorFn);

    }

    exportTemplate (event: any, template: any) {
        var promise = this.http.get(this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id);
    }
}
