import * as angular from 'angular';
import {moduleName} from "./module-name";
import {ListTableView} from "../../services/ListTableViewTypes";
import PaginationData = ListTableView.PaginationData;
import SortOption = ListTableView.SortOption;

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
    loading: boolean;
    /**
     * the title of the card
     */
    cardTitle: string;
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

    constructor(private $scope: any, private $http: any, private $mdDialog: any, private $q: any
        , private AccessControlService: any, private RestUrlService: any, private PaginationDataService: ListTableView.PaginationDataService
        , private TableOptionsService: ListTableView.TableOptionService, private AddButtonService: any, private StateService: any, private RegisterTemplateService: any) {

        this.loading = true;
        this.cardTitle = 'Templates';

        this.selectedTableOption = this.selectedTableOption.bind(this);

        // Register Add button
        this.AccessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                    this.AddButtonService.registerAddButton("registered-templates", () =>{
                        this.RegisterTemplateService.resetModel();
                        this.StateService.FeedManager().Template().navigateToRegisterNewTemplate();
                    });
                }
            });


        this.paginationData = this.PaginationDataService.paginationData(this.pageName);

        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = PaginationDataService.currentPage(this.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = this.loadSortOptions();
        this.filter = PaginationDataService.filter(this.pageName);
    }

    /**
     * Initialize the controller and properties
     */
    ngOnInit() {


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


        this.getRegisteredTemplates();

        // Fetch the allowed actions
        this.AccessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowExport = this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });

    }

    onViewTypeChange(viewType: string) {
        this.PaginationDataService.viewType(this.pageName, this.viewType);
    }


    onOrderChange(order: string) {
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };

    onPaginationChange(page: number, limit: number) {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption(option: SortOption) {
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    }

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
    templateDetails(event: angular.IAngularEvent, template: any) {
        if (this.allowEdit && template != undefined) {
            this.RegisterTemplateService.resetModel();

            this.$q.when(this.RegisterTemplateService.hasEntityAccess([this.AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE], template)).then((hasAccess: boolean) => {
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

    getRegisteredTemplates(): angular.IPromise<any> {

        let successFn = (response: angular.IHttpResponse<any>) => {
            this.loading = false;
            if (response.data) {
                var entityAccessControlled = this.AccessControlService.isEntityAccessControlled();
                angular.forEach(response.data, (template) => {
                    template.allowExport = !entityAccessControlled || this.RegisterTemplateService.hasEntityAccess(this.AccessControlService.ENTITY_ACCESS.TEMPLATE.EXPORT, template);
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

    exportTemplate(event: angular.IAngularEvent, template: any) {
        var promise = this.$http.get(this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id);
    }

    /**
     * When the controller is ready, initialize
     */
    $onInit(): void {
        this.ngOnInit();
    }

}

angular.module(moduleName).controller('RegisteredTemplatesController', RegisteredTemplatesController);
