import * as angular from 'angular';
import {AccessControlService} from "../../services/AccessControlService";
import { moduleName } from "./module-name";
import { ListTableView } from "../../services/ListTableViewTypes";
import PaginationData = ListTableView.PaginationData;
import SortOption = ListTableView.SortOption;
import {StateService} from '../../services/StateService';
import { RegisterTemplateServiceFactory } from '../services/RegisterTemplateServiceFactory';
import './module-require';

export class RegisteredTemplatesController {

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
    /**
     * The pagination Data
     */
    paginationData: PaginationData;
    /**
     * The unique id for the PaginationData
     */
    paginationId: string = 'registered-templates';
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


    static $inject = ["$scope", "$http", "$mdDialog", "$q", "AccessControlService", "RestUrlService", "PaginationDataService", "TableOptionsService", "AddButtonService", "StateService", "RegisterTemplateService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private $mdDialog: angular.material.IDialogService, private $q: angular.IQService
        , private accessControlService: AccessControlService, private RestUrlService: any, private PaginationDataService: ListTableView.PaginationDataService
        , private TableOptionsService: ListTableView.TableOptionService, private AddButtonService: any, private StateService: StateService, private RegisterTemplateService: RegisterTemplateServiceFactory) {

        this.$scope.$watch(() => {
            return this.viewType;
        }, (newVal: any) => {
            this.onViewTypeChange(newVal);
        });

        this.$scope.$watch(() => {
            return this.filter;
        }, (newVal: any) => {
            this.PaginationDataService.filter(this.pageName, newVal)
        });
    }
    /**
     * When the controller is ready, initialize
     */
    $onInit() {
        this.ngOnInit();
    }
    /**
     * Initialize the controller and properties
     */
    ngOnInit() {

        this.selectedTableOption = this.selectedTableOption.bind(this);

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


        this.paginationData = this.PaginationDataService.paginationData(this.pageName);

        this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = this.PaginationDataService.currentPage(this.pageName) || 1;
        this.viewType = this.PaginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();
        this.filter = this.PaginationDataService.filter(this.pageName);


        this.getRegisteredTemplates();

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowExport = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });

    }

    onViewTypeChange = (viewType: string) => {
        this.PaginationDataService.viewType(this.pageName, this.viewType);
    }


    onOrderChange = (order: string) => {
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange = (page: number, limit: number) => {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption = (option: SortOption)  => {
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    };

    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    loadSortOptions() {
        var options = {'Name': 'templateName', 'Last modified': 'updateDate'};
        var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'templateName', 'asc');
        this.TableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    /**
     * Displays the details of the specified template.
     *
     * @param event
     * @param template
     */
    templateDetails = (event: angular.IAngularEvent, template: any) =>{
        if (this.allowEdit && template != undefined) {
            this.RegisterTemplateService.resetModel();

            this.$q.when(this.RegisterTemplateService.hasEntityAccess([AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE], template)).then((hasAccess: boolean) => {
                if (hasAccess) {
                    this.StateService.FeedManager().Template().navigateToRegisteredTemplate(template.id, template.nifiTemplateId);
                }
                else {
                    this.RegisterTemplateService.accessDeniedDialog();
                }
            });

        } else {
            this.RegisterTemplateService.accessDeniedDialog();
        }
    };

    templateInfo = (event: angular.IAngularEvent, template: any) =>{
        this.StateService.FeedManager().Template().navigateToTemplateInfo(template.id, template.nifiTemplateId);
    }

    getRegisteredTemplates = (): angular.IPromise<any> =>{

        let successFn = (response: angular.IHttpResponse<any>) => {
            this.loading = false;
            if (response.data) {
                var entityAccessControlled = this.accessControlService.isEntityAccessControlled();
                angular.forEach(response.data, (template) => {
                    template.allowExport = !entityAccessControlled || this.RegisterTemplateService.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.EXPORT, template);
                    template.exportUrl = this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id;
                });
            }
            this.registeredTemplates = response.data;
        };
        let errorFn = (err: any) => {
            this.loading = false;

        };
        var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
        promise.then(successFn, errorFn);
        return promise;

    }

    exportTemplate = (event: angular.IAngularEvent, template: any) => {
        var promise = this.$http.get(this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id);
    }
}

const module = angular.module(moduleName).component('registeredTemplatesController', {

    templateUrl: './registered-templates.html',
    controller: RegisteredTemplatesController,
    controllerAs: 'vm'

});
export default module;