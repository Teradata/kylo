define(['angular',"feed-mgr/templates/module-name"], function (angular,moduleName) {

    var controller = function($scope, $http, $mdDialog, $q,AccessControlService, RestUrlService, PaginationDataService, TableOptionsService, AddButtonService, StateService, RegisterTemplateService) {

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
                .then(function(actionSet) {
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
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        });

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            PaginationDataService.filter(self.pageName, newVal)
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
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
        this.templateDetails = function(event, template) {
            if (self.allowEdit && template != undefined) {
                RegisterTemplateService.resetModel();

                $q.when(RegisterTemplateService.hasEntityAccess([AccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE],template)).then(function(hasAccess){
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

            var successFn = function(response) {
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
            var errorFn = function(err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;

        }

        this.exportTemplate = function(event, template) {
            var promise = $http.get(RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + template.id);
        }

        getRegisteredTemplates();

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                    self.allowExport = AccessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
                });
    };

    angular.module(moduleName).controller('RegisteredTemplatesController', ["$scope","$http","$mdDialog","$q","AccessControlService","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","StateService","RegisterTemplateService",controller]);

});
