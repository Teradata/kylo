define(["require", "exports", "angular", "./module-name", "underscore", "../services/OpsManagerRestUrlService", "../services/IconStatusService", "../services/TabService"], function (require, exports, angular, module_name_1, _, OpsManagerRestUrlService_1, IconStatusService_1, TabService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $timeout, $q, $mdToast, $mdPanel, OpsManagerRestUrlService, TableOptionsService, PaginationDataService, StateService, IconService, TabService, AccessControlService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$timeout = $timeout;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.$mdPanel = $mdPanel;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.IconService = IconService;
            this.TabService = TabService;
            this.AccessControlService = AccessControlService;
            this.BroadcastService = BroadcastService;
            this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'service-level-assessments';
            this.loaded = false;
            //Track active requests and be able to cancel them if needed
            this.activeRequests = [];
            this.tabNames = ['All', 'Failure', 'Warning', 'Success'];
            this.onViewTypeChange = function (viewType) {
                _this.PaginationDataService.viewType(_this.pageName, viewType);
            };
            //Tab Functions
            this.onTabSelected = function (tab) {
                _this.TabService.selectedTab(_this.pageName, tab);
                return _this.loadAssessments(true).promise;
            };
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
                return _this.loadAssessments(true).promise;
                //return self.deferred.promise;
            };
            this.onPaginate = function (page, limit) {
                var activeTab = _this.TabService.getActiveTab(_this.pageName);
                //only trigger the reload if the initial page has been loaded.
                //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
                //on load the query will be triggered via onTabSelected() method
                if (_this.loaded) {
                    activeTab.currentPage = page;
                    _this.PaginationDataService.currentPage(_this.pageName, activeTab.title, page);
                    return _this.loadAssessments(true).promise;
                }
            };
            this.onPaginationChange = function (page, limit) {
                if (_this.viewType == 'list') {
                    _this.onPaginate(page, limit);
                }
            };
            this.onDataTablePaginationChange = function (page, limit) {
                if (_this.viewType == 'table') {
                    _this.onPaginate(page, limit);
                }
            };
            this.assessmentDetails = function (event, assessment) {
                _this.StateService.OpsManager().Sla().navigateToServiceLevelAssessment(assessment.id);
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
                _this.loadAssessments(true);
            };
            //Load Jobs
            this.loadAssessments = function (force) {
                if (force || !_this.refreshing) {
                    if (force) {
                        angular.forEach(_this.activeRequests, function (canceler, i) {
                            canceler.resolve();
                        });
                        _this.activeRequests = [];
                    }
                    var activeTab = _this.TabService.getActiveTab(_this.pageName);
                    _this.refreshing = true;
                    var sortOptions = '';
                    var tabTitle = activeTab.title;
                    var filters = { tabTitle: tabTitle };
                    var limit = _this.paginationData.rowsPerPage;
                    var start = (limit * activeTab.currentPage) - limit; //self.query.page(self.selectedTab));
                    var sort = _this.PaginationDataService.sort(_this.pageName);
                    var canceler = _this.$q.defer();
                    var successFn = function (response) {
                        if (response.data) {
                            _this.transformAssessments(tabTitle, response.data.data);
                            _this.TabService.setTotal(_this.pageName, tabTitle, response.data.recordsFiltered);
                            if (_this.loading) {
                                _this.loading = false;
                            }
                        }
                        _this.finishedRequest(canceler);
                    };
                    var errorFn = function (err) {
                        _this.finishedRequest(canceler);
                    };
                    var finallyFn = function () {
                    };
                    _this.activeRequests.push(canceler);
                    _this.deferred = canceler;
                    _this.promise = _this.deferred.promise;
                    var filter = _this.filter;
                    if (tabTitle.toUpperCase() != 'ALL') {
                        if (filter != null && angular.isDefined(filter) && filter != '') {
                            filter += ',';
                        }
                        filter += 'result==' + tabTitle.toUpperCase();
                    }
                    var params = { start: start, limit: limit, sort: sort, filter: filter };
                    _this.$http.get(_this.OpsManagerRestUrlService.LIST_SLA_ASSESSMENTS_URL, { timeout: canceler.promise, params: params })
                        .then(successFn, errorFn);
                }
                _this.showProgress = true;
                return _this.deferred;
            };
            this.clearRefreshTimeout = function (instanceId) {
                var timeoutInstance = this.timeoutMap[instanceId];
                if (timeoutInstance) {
                    this.$timeout.cancel(timeoutInstance);
                    delete this.timeoutMap[instanceId];
                }
            };
            this.filter = angular.isUndefined(this.filter) ? '' : this.filter;
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.paginationId = function (tab) {
                return PaginationDataService.paginationId(_this.pageName, tab.title);
            };
            this.currentPage = function (tab) {
                return PaginationDataService.currentPage(_this.pageName, tab.title);
            };
            $scope.$watch(function () { return _this.viewType; }, function (newVal) { _this.onViewTypeChange(newVal); });
            $scope.$watch(function () { return _this.filter; }, function (newVal, oldVal) {
                if (newVal != oldVal) {
                    return _this.loadAssessments(true).promise;
                }
            });
            //Pagination and view Type (list or table)
            this.paginationData = this.PaginationDataService.paginationData(this.pageName);
            this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
            //Setup the Tabs
            // let tabNames = ['All', 'Failure', 'Warning','Success'];
            this.tabs = this.TabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
            this.tabMetadata = this.TabService.metadata(this.pageName);
            this.sortOptions = this.loadSortOptions();
            /**
             * Indicates that admin operations are allowed.
             * @type {boolean}
            */
            this.allowAdmin = false;
            //Page State
            this.loading = true;
            this.showProgress = true;
            // Fetch allowed permissions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
        } // end of constructor
        /**
        * Build the possible Sorting Options
        * @returns {*[]}
        */
        controller.prototype.loadSortOptions = function () {
            var options = { 'Name': 'serviceLevelAgreementDescription.name', 'Time': 'createdTime', 'Status': 'result' };
            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'createdTime', 'desc');
            var currentOption = this.TableOptionsService.getCurrentSort(this.pageName);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(this.pageName, currentOption);
            }
            return sortOptions;
        };
        controller.prototype.transformAssessments = function (tabTitle, assessments) {
            //first clear out the arrays
            this.TabService.clearTabs(this.pageName);
            angular.forEach(assessments, function (assessment, i) {
                this.TabService.addContent(this.pageName, tabTitle, assessment);
            });
            return assessments;
        };
        controller.prototype.finishedRequest = function (canceler) {
            var index = _.indexOf(this.activeRequests, canceler);
            if (index >= 0) {
                this.activeRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            this.refreshing = false;
            this.showProgress = false;
            this.loaded = true;
        };
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service("IconService", [IconStatusService_1.default])
        .service("OpsManagerRestUrlService", [OpsManagerRestUrlService_1.default])
        .service("TabService", TabService_1.default)
        .controller("ServiceLevelAssessmentsController", ["$scope", "$http", "$timeout", "$q", "$mdToast", "$mdPanel", "OpsManagerRestUrlService", "TableOptionsService", "PaginationDataService", "StateService", "IconService", "TabService", "AccessControlService", "BroadcastService", controller]);
    angular.module(module_name_1.moduleName).directive("kyloServiceLevelAssessments", //[this.thinkbigPermissionsTable]);
    [function () {
            return {
                restrict: "EA",
                bindToController: {
                    cardTitle: "@",
                    pageName: '@',
                    filter: '@'
                },
                controllerAs: 'vm',
                scope: true,
                templateUrl: 'js/ops-mgr/sla/service-level-assessments-template.html',
                controller: "ServiceLevelAssessmentsController",
                link: function ($scope, element, attrs, controller) {
                }
            };
        }]);
});
//# sourceMappingURL=service-level-assessments.js.map