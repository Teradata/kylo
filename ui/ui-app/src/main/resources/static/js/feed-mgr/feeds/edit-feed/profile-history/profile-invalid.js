define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm: '=',
                rowsPerPage: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-invalid-results.html',
            controller: "FeedProfileInvalidResultsController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var FeedProfileInvalidResultsController = /** @class */ (function () {
        function FeedProfileInvalidResultsController($scope, $http, $window, FeedService, RestUrlService, HiveService, Utils, BroadcastService, FattableService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$window = $window;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.Utils = Utils;
            this.BroadcastService = BroadcastService;
            this.FattableService = FattableService;
            this.model = this.FeedService.editFeedModel;
            this.data = [];
            this.loadingFilterOptions = false;
            this.loadingData = false;
            this.limitOptions = [10, 50, 100, 500, 1000];
            this.limit = this.limitOptions[2];
            this.queryResults = [];
            this.filterOptions = [
                { name: 'None', objectShortClassType: '' },
                { name: 'Type Conversion', objectShortClassType: 'Not convertible to' }
            ];
            this.filter = this.filterOptions[0];
            var self = this;
            var errorFn = function (err) {
                self.loadingData = false;
            };
            function getProfileValidation() {
                self.loadingData = true;
                var successFn = function (response) {
                    var result = self.queryResults = HiveService.transformResultsToUiGridModel(response, [], transformFn.bind(self));
                    self.headers = result.columns;
                    self.headers = _.reject(self.headers, function (col) {
                        return col.name == 'dlp_reject_reason';
                    });
                    self.rows = result.rows;
                    self.loadingData = false;
                    BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'invalid');
                };
                var promise = $http.get(RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(self.model.id), { params: {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit,
                        'filter': _.isUndefined(self.filter) ? '' : self.filter.objectShortClassType
                    }
                });
                promise.then(successFn, errorFn);
                return promise;
            }
            //noinspection JSUnusedGlobalSymbols
            function onLimitChange() {
                getProfileValidation().then(this.setupTable.bind(self));
            }
            ;
            //noinspection JSUnusedGlobalSymbols
            function onFilterChange() {
                getProfileValidation().then(this.setupTable.bind(self));
            }
            ;
            function getFilterOptions() {
                self.loadingFilterOptions = true;
                var filterOptionsOk = function (response) {
                    self.filterOptions = _.union(self.filterOptions, response.data);
                    self.loadingFilterOptions = false;
                };
                var promise = $http.get(RestUrlService.AVAILABLE_VALIDATION_POLICIES, { cache: true });
                promise.then(filterOptionsOk, errorFn);
                return promise;
            }
            function setupTable() {
                var _this = this;
                FattableService.setupTable({
                    tableContainerId: "invalidProfile",
                    headers: self.headers,
                    rows: self.rows,
                    cellText: function (row, column) {
                        //return the longest text out of cell value and its validation errors
                        var textArray = [];
                        textArray.push(row[column.displayName]);
                        var validationError = row.invalidFieldMap[column.displayName];
                        if (validationError !== undefined) {
                            textArray.push(validationError.rule);
                            textArray.push(validationError.reason);
                        }
                        return textArray.sort(function (a, b) { return b.length - a.length; })[0];
                    },
                    fillCell: function (cellDiv, data) {
                        var html = data.value;
                        if (data.isInvalid) {
                            html += '<span class="violation hint">' + data.rule + '</span>';
                            html += '<span class="violation hint">' + data.reason + '</span>';
                            cellDiv.className += " warn";
                        }
                        cellDiv.innerHTML = html;
                    },
                    getCellSync: function (i, j) {
                        var displayName = _this.headers[j].displayName;
                        var row = _this.rows[i];
                        if (row === undefined) {
                            //occurs when filtering table
                            return undefined;
                        }
                        var invalidFieldMap = row.invalidFieldMap[displayName];
                        var isInvalid = invalidFieldMap !== undefined;
                        var rule = isInvalid ? invalidFieldMap.rule : "";
                        var reason = isInvalid ? invalidFieldMap.reason : "";
                        return {
                            "value": row[displayName],
                            "isInvalid": isInvalid,
                            "rule": rule,
                            "reason": reason
                        };
                    }
                });
            }
            function transformFn(row, columns, displayColumns) {
                var _this = this;
                var invalidFields = [];
                var invalidFieldMap = {};
                row.invalidFields = invalidFields;
                row.invalidFieldMap = invalidFieldMap;
                row.invalidField = function (column) {
                    return _this.invalidFieldMap[column];
                };
                var _index = _.indexOf(displayColumns, 'dlp_reject_reason');
                var rejectReasons = row[columns[_index]];
                if (rejectReasons != null) {
                    rejectReasons = angular.fromJson(rejectReasons);
                }
                if (rejectReasons != null) {
                    angular.forEach(rejectReasons, function (rejectReason) {
                        if (rejectReason.scope == 'field') {
                            var field = rejectReason.field;
                            var copy = angular.copy(rejectReason);
                            _index = _.indexOf(displayColumns, field);
                            copy.fieldValue = row[columns[_index]];
                            invalidFields.push(copy);
                            invalidFieldMap[columns[_index]] = copy;
                        }
                    });
                }
            }
            ;
            getFilterOptions();
            getProfileValidation().then(setupTable);
        }
        return FeedProfileInvalidResultsController;
    }());
    exports.FeedProfileInvalidResultsController = FeedProfileInvalidResultsController;
    angular.module(moduleName).controller('FeedProfileInvalidResultsController', ["$scope", "$http", "$window", "FeedService", "RestUrlService", "HiveService", "Utils", "BroadcastService", "FattableService", FeedProfileInvalidResultsController]);
    angular.module(moduleName).directive('thinkbigFeedProfileInvalid', directive);
});
//# sourceMappingURL=profile-invalid.js.map