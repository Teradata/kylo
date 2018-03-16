import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "./module-name";

 export class RegisteredTemplatesController {

    allowEdit:any;
    allowExport:any;
    registeredTemplates:any;
    loading:any;
    cardTitle:any;
    pageName:any;
    paginationData:any;
    paginationId:any;
    currentPage:any;
    viewType:any;
    sortOptions:any;
    filter:any;
    onViewTypeChange:any;
    onOrderChange:any;
    onPaginationChange:any;
    selectedTableOption:any;
    templateDetails:any;
    exportTemplate:any;
    constructor(private $scope:any, private $http:any, private $mdDialog:any, private $q:any
        ,private AccessControlService:any, private RestUrlService:any, private PaginationDataService:any
        , private TableOptionsService:any, private AddButtonService:any, private StateService:any, private RegisterTemplateService:any) {

        var self = this;

        /**
         * Indicates if templates are allowed to be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Indicates if templates are allowed to be exported.
         * @type {boolean}
         */
        self.allowExport = false;

        self.registeredTemplates = [];
        this.loading = true;
        this.cardTitle = 'Templates';

        // Register Add button
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet:any) {
                    if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                        AddButtonService.registerAddButton("registered-templates", function() {
                            RegisterTemplateService.resetModel();
                            StateService.FeedManager().Template().navigateToRegisterNewTemplate();
                        });
                    }
                });

        //Pagination DAta
        this.pageName = "registered-templates";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'registered-templates';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);

        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal:any) {
            self.onViewTypeChange(newVal);
        });

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal:any) {
            PaginationDataService.filter(self.pageName, newVal)
        })

        this.onViewTypeChange = function(viewType:any) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order:any) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        this.onPaginationChange = function(page:any, limit:any) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option:any) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Template': 'templateName', 'Last Modified': 'updateDate'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'templateName', 'asc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        /**
         * Displays the details of the specified template.
         *
         * @param event
         * @param template
         */
        this.templateDetails = function(event:any, template:any) {
            if (self.allowEdit && template != undefined) {
                RegisterTemplateService.resetModel();

                $q.when(RegisterTemplateService.hasEntityAccess([AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE],template)).then(function(hasAccess:any){
                    if(hasAccess){
                        StateService.FeedManager().Template().navigateToRegisteredTemplate(template.id, template.nifiTemplateId);
                    }
                    else {
                        RegisterTemplateService.accessDeniedDialog();
                    }
                });

            } else {
                RegisterTemplateService.accessDeniedDialog();
            }
        };

        function getRegisteredTemplates() {

            var successFn = function(response:any) {
                self.loading = false;
                if (response.data) {
                    var entityAccessControlled = AccessControlService.isEntityAccessControlled();
                    angular.forEach(response.data, function(template) {
                        template.allowExport = !entityAccessControlled || RegisterTemplateService.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.EXPORT, template);
                        template.exportUrl = RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id;
                    });
                }
                self.registeredTemplates = response.data;
            }
            var errorFn = function(err:any) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;

        }

        this.exportTemplate = function(event:any, template:any) {
            var promise = $http.get(RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id);
        }

        getRegisteredTemplates();

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet:any) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                    self.allowExport = AccessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
                });
    };

    
}
angular.module(moduleName).controller('RegisteredTemplatesController', ["$scope","$http","$mdDialog","$q","AccessControlService","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","StateService","RegisterTemplateService",RegisteredTemplatesController]);
