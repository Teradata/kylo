define(['angular',"feed-mgr/sla/module-name"], function (angular,moduleName) {

    var controller = function($scope, $http, $mdDialog, $q,$transition$,AccessControlService, PaginationDataService, TableOptionsService, AddButtonService, StateService, SlaEmailTemplateService) {

        var self = this;

        /**
         * Indicates if templates are allowed to be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        self.templates = [];
        this.loading = true;
        this.cardTitle = 'SLA Email Templates';

        // Register Add button
        AccessControlService.getUserAllowedActions()
            .then(function(actionSet) {
                if (AccessControlService.hasAction(AccessControlService.SLA_EMAIL_TEMPLATES_ACCESS, actionSet.actions)) {
                    AddButtonService.registerAddButton("sla-email-templates", function() {
                        SlaEmailTemplateService.newTemplate();
                        StateService.FeedManager().Sla().navigateToNewEmailTemplate();
                    });
                }
            });

        //Pagination DAta
        this.pageName = "sla-email-templates";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'sla-email-templates';
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
            var options = {'Template': 'name'};
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
        this.editTemplate = function(event, template) {
            if (self.allowEdit && template != undefined) {
                SlaEmailTemplateService.template = template;
                StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);

            } else {
                SlaEmailTemplateService.accessDeniedDialog();
            }
        };

        function getExistingTemplates() {

            var successFn = function(response) {
                self.loading = false;
                self.templates = response.data;
            }
            var errorFn = function(err) {
                self.loading = false;
            }
            var promise = SlaEmailTemplateService.getExistingTemplates()
            promise.then(successFn, errorFn);
            return promise;

        }


        getExistingTemplates();

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
            .then(function(actionSet) {
                self.allowEdit = AccessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
    };

    angular.module(moduleName).controller('SlaEmailTemplatesController', ["$scope","$http","$mdDialog","$q","$transition$","AccessControlService","PaginationDataService","TableOptionsService","AddButtonService","StateService","SlaEmailTemplateService",controller]);

});
