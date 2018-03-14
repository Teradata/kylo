define(["require", "exports", "angular", "../module-name", "./SlaEmailTemplateService"], function (require, exports, angular, module_name_1, SlaEmailTemplateService_1) {
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
                _this.PaginationDataService.viewType(_this.pageName, _this.viewType);
            };
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
            };
            this.onPaginationChange = function (page, limit) {
                _this.PaginationDataService.currentPage(_this.pageName, null, page);
                _this.currentPage = page;
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.TableOptionsService.toSortString(option);
                _this.PaginationDataService.sort(_this.pageName, sortString);
                var updatedOption = _this.TableOptionsService.toggleSort(_this.pageName, option);
                _this.TableOptionsService.setSortOption(_this.pageName, sortString);
            };
            /**
             * Displays the details of the specified template.
             *
             * @param event
             * @param template
             */
            this.editTemplate = function (event, template) {
                if (_this.allowEdit && template != undefined) {
                    _this.SlaEmailTemplateService.template = template;
                    _this.StateService.FeedManager().Sla().navigateToNewEmailTemplate(template.id);
                }
                else {
                    _this.SlaEmailTemplateService.accessDeniedDialog();
                }
            };
            this.getExistingTemplates = function () {
                var successFn = function (response) {
                    _this.loading = false;
                    _this.templates = response.data;
                };
                var errorFn = function (err) {
                    _this.loading = false;
                };
                var promise = _this.SlaEmailTemplateService.getExistingTemplates();
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
                _this.allowEdit = AccessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
        }
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service('SlaEmailTemplateService', ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", SlaEmailTemplateService_1.default])
        .controller('SlaEmailTemplatesController', ["$scope", "$http", "$mdDialog", "$q", "$transition$",
        "AccessControlService", "PaginationDataService",
        "TableOptionsService", "AddButtonService", "StateService",
        "SlaEmailTemplateService", controller]);
});
//# sourceMappingURL=SlaEmailTemplatesController.js.map