define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $mdDialog, $q, $transition$, AccessControlService, PaginationDataService, TableOptionsService, AddButtonService, StateService, SlaEmailTemplateService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$q = $q;
            this.$transition$ = $transition$;
            this.AccessControlService = AccessControlService;
            this.PaginationDataService = PaginationDataService;
            this.TableOptionsService = TableOptionsService;
            this.AddButtonService = AddButtonService;
            this.StateService = StateService;
            this.SlaEmailTemplateService = SlaEmailTemplateService;
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Template': 'name' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'templateName', 'asc');
                this.TableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
            };
            /**
* Indicates if templates are allowed to be edited.
* @type {boolean}
*/
            this.allowEdit = false;
            this.templates = [];
            this.loading = true;
            this.cardTitle = 'SLA Email Templates';
            //Pagination DAta
            this.pageName = "sla-email-templates";
            this.paginationData = this.PaginationDataService.paginationData(this.pageName);
            this.paginationId = 'sla-email-templates';
            this.currentPage = this.PaginationDataService.currentPage(this.pageName) || 1;
            this.viewType = this.PaginationDataService.viewType(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.filter = this.PaginationDataService.filter(this.pageName);
            this.onViewTypeChange = function (viewType) {
                this.PaginationDataService.viewType(this.pageName, this.viewType);
            };
            this.onOrderChange = function (order) {
                this.PaginationDataService.sort(this.pageName, order);
                this.TableOptionsService.setSortOption(this.pageName, order);
            };
            this.onPaginationChange = function (page, limit) {
                this.PaginationDataService.currentPage(this.pageName, null, page);
                this.currentPage = page;
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = this.TableOptionsService.toSortString(option);
                this.PaginationDataService.sort(this.pageName, sortString);
                var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
                this.TableOptionsService.setSortOption(this.pageName, sortString);
            };
            /**
             * Displays the details of the specified template.
             *
             * @param event
             * @param template
             */
            this.editTemplate = function (event, template) {
                if (this.allowEdit && template != undefined) {
                    this.SlaEmailTemplateService.template = template;
                    this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);
                }
                else {
                    this.SlaEmailTemplateService.accessDeniedDialog();
                }
            };
            this.getExistingTemplates = function () {
                var successFn = function (response) {
                    this.loading = false;
                    this.templates = response.data;
                };
                var errorFn = function (err) {
                    this.loading = false;
                };
                var promise = this.SlaEmailTemplateService.getExistingTemplates();
                promise.then(successFn, errorFn);
                return promise;
            };
            // Register Add button
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (AccessControlService.hasAction(AccessControlService.SLA_EMAIL_TEMPLATES_ACCESS, actionSet.actions)) {
                    AddButtonService.registerAddButton("sla-email-templates", function () {
                        SlaEmailTemplateService.newTemplate();
                        StateService.FeedManager().Sla().navigateToNewEmailTemplate();
                    });
                }
            });
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal) {
                PaginationDataService.filter(_this.pageName, newVal);
            });
            this.getExistingTemplates();
            // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                this.allowEdit = AccessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('SlaEmailTemplatesController', ["$scope", "$http", "$mdDialog", "$q", "$transition$",
        "AccessControlService", "PaginationDataService",
        "TableOptionsService", "AddButtonService", "StateService",
        "SlaEmailTemplateService", controller]);
});
//# sourceMappingURL=SlaEmailTemplatesController.js.map