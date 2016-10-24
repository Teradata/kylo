(function() {

    var controller = function($scope, $http, $mdDialog, AccessControlService, RestUrlService, PaginationDataService, TableOptionsService, AddButtonService, StateService, RegisterTemplateService) {

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
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.TEMPLATES_IMPORT, actionSet.actions)) {
                        AddButtonService.registerAddButton("registered-templates", function() {
                            RegisterTemplateService.resetModel();
                            StateService.navigateToRegisterTemplate();
                        });
                    }
                });

        //Pagination DAta
        this.pageName = "registered-templates";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'registered-templates';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', 'All']);
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
            if (self.allowEdit) {
                RegisterTemplateService.resetModel();
                StateService.navigateToRegisteredTemplate(template.id, template.nifiTemplateId);
            } else {
                $mdDialog.show(
                        $mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title("Access Denied")
                                .textContent("You do not have access to edit templates.")
                                .ariaLabel("Access denied to edit templates")
                                .ok("OK")
                );
            }
        };

        function getRegisteredTemplates() {

            var successFn = function(response) {
                self.loading = false;
                if (response.data) {
                    angular.forEach(response.data, function(template) {
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
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                    self.allowExport = AccessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
                });
    };

    angular.module(MODULE_FEED_MGR).controller('RegisteredTemplatesController', controller);

}());
