import * as angular from 'angular';
import {moduleName} from '../module-name';
import * as _ from 'underscore';
import SlaEmailTemplateService from "./SlaEmailTemplateService";

export class controller implements ng.IComponentController{
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions = function() {
            var options = {'Template': 'name'};
            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'templateName', 'asc');
            this.TableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;
        }
   constructor(private $scope: any,
                private $http: any,
                private $mdDialog: any,
                private $q: any,
                private $transition$: any,
                private AccessControlService: any,
                private PaginationDataService: any,
                private TableOptionsService: any,
                private AddButtonService: any,
                private StateService: any,
                private SlaEmailTemplateService: any){
           
                    // Register Add button
                        AccessControlService.getUserAllowedActions()
                            .then((actionSet: any)=>{
                                if (AccessControlService.hasAction(AccessControlService.SLA_EMAIL_TEMPLATES_ACCESS, actionSet.actions)) {
                                    AddButtonService.registerAddButton("sla-email-templates", ()=>{
                                        SlaEmailTemplateService.newTemplate();
                                        StateService.FeedManager().Sla().navigateToNewEmailTemplate();
                                    });
                                }
                            });

                         PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        
                            $scope.$watch(()=> {
                                return this.viewType;
                                }, (newVal: any)=> {
                                    this.onViewTypeChange(newVal);
                                });

                            $scope.$watch(()=> {
                                return this.filter;
                                }, (newVal: any)=> {
                                    PaginationDataService.filter(this.pageName, newVal)
                                });
                    this.getExistingTemplates();

                        // Fetch the allowed actions
                        AccessControlService.getUserAllowedActions()
                            .then((actionSet: any)=>{
                                this.allowEdit = AccessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
                            });
                }


                        /**
         * Indicates if templates are allowed to be edited.
         * @type {boolean}
         */
        allowEdit: boolean= false;

        templates: any[] = [];
        loading: boolean = true;
        cardTitle: string = 'SLA Email Templates';

        //Pagination DAta
        pageName: string = "sla-email-templates";
        paginationData: any = this.PaginationDataService.paginationData(this.pageName);
        paginationId: string = 'sla-email-templates';
        currentPage: any = this.PaginationDataService.currentPage(this.pageName) || 1;
        viewType: any = this.PaginationDataService.viewType(this.pageName);
        sortOptions: any = this.loadSortOptions();

        filter: any = this.PaginationDataService.filter(this.pageName);

        

        onViewTypeChange = (viewType: any)=>{
           this.PaginationDataService.viewType(this.pageName, this.viewType);
        }

        onOrderChange = (order: any)=> {
            this.PaginationDataService.sort(this.pageName, order);
            this.TableOptionsService.setSortOption(this.pageName, order);
        };

        onPaginationChange = (page: any, limit: any)=> {
            this.PaginationDataService.currentPage(this.pageName, null, page);
            this.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any) =>{
            var sortString = this.TableOptionsService.toSortString(option);
            this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
        }



        /**
         * Displays the details of the specified template.
         *
         * @param event
         * @param template
         */
        editTemplate = (event: any, template: any)=> {
            if (this.allowEdit && template != undefined) {
                this.SlaEmailTemplateService.template = template;
                this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);

            } else {
                this.SlaEmailTemplateService.accessDeniedDialog();
            }
        };

        getExistingTemplates= ()=> {

            var successFn = (response: any) =>{
                this.loading = false;
                this.templates = response.data;
            }
            var errorFn = (err: any) =>{
                this.loading = false;
            }
            var promise = this.SlaEmailTemplateService.getExistingTemplates()
            promise.then(successFn, errorFn);
            return promise;
        }
}

 angular.module(moduleName)
 .controller('SlaEmailTemplatesController', 
                                        ["$scope","$http","$mdDialog","$q","$transition$",
                                        "AccessControlService","PaginationDataService",
                                        "TableOptionsService","AddButtonService","StateService",
                                        "SlaEmailTemplateService",controller]);
