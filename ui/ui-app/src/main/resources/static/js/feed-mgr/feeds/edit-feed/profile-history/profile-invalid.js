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
        }
        FeedProfileInvalidResultsController.prototype.$onInit = function () {
            this.getFilterOptions();
            this.getProfileValidation().then(this.setupTable.bind(this));
        };
        FeedProfileInvalidResultsController.prototype.errorFn = function (err) {
            this.loadingData = false;
        };
        ;
        FeedProfileInvalidResultsController.prototype.getProfileValidation = function () {
            var _this = this;
            // console.log('get profile validation');
            this.loadingData = true;
            var successFn = function (response) {
                // console.log('successFn');
                var result = _this.queryResults = _this.HiveService.transformResultsToUiGridModel(response, [], _this.transformFn.bind(_this));
                _this.headers = result.columns;
                _this.headers = _.reject(_this.headers, function (col) {
                    return col.name == 'dlp_reject_reason';
                });
                _this.rows = result.rows;
                _this.loadingData = false;
                _this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'invalid');
            };
            var promise = this.$http.get(this.RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(this.model.id), {
                params: {
                    'processingdttm': this.processingdttm,
                    'limit': this.limit,
                    'filter': _.isUndefined(this.filter) ? '' : this.filter.objectShortClassType
                }
            });
            promise.then(successFn, this.errorFn);
            return promise;
        };
        FeedProfileInvalidResultsController.prototype.onLimitChange = function () {
            this.getProfileValidation().then(this.setupTable.bind(this));
        };
        ;
        FeedProfileInvalidResultsController.prototype.onFilterChange = function () {
            this.getProfileValidation().then(this.setupTable.bind(this));
        };
        ;
        FeedProfileInvalidResultsController.prototype.getFilterOptions = function () {
            var _this = this;
            this.loadingFilterOptions = true;
            var filterOptionsOk = function (response) {
                _this.filterOptions = _.union(_this.filterOptions, response.data);
                _this.loadingFilterOptions = false;
            };
            var promise = this.$http.get(this.RestUrlService.AVAILABLE_VALIDATION_POLICIES, { cache: true });
            promise.then(filterOptionsOk, this.errorFn);
            return promise;
        };
        FeedProfileInvalidResultsController.prototype.setupTable = function () {
            var _this = this;
            // console.log('setupTable');
            var parameters = {
                tableContainerId: "invalidProfile",
                headers: this.headers,
                rows: this.rows,
                rowHeight: 45,
                cellText: function (row, column) {
                    // console.log('cellText');
                    //return the longest text out of cell value and its validation errors
                    var textArray = [];
                    textArray.push(row[column.displayName]);
                    var validationError = row.invalidFieldMap[column.displayName];
                    if (validationError !== undefined) {
                        textArray.push(validationError.rule);
                        textArray.push(validationError.reason);
                    }
                    return textArray.sort(function (a, b) {
                        return b.length - a.length;
                    })[0];
                },
                fillCell: function (cellDiv, data) {
                    // console.log('fillCell');
                    var html = _.escape(data.value);
                    if (data.isInvalid) {
                        html += '<span class="violation hint">' + data.rule + '</span>';
                        html += '<span class="violation hint">' + data.reason + '</span>';
                        cellDiv.className += " warn";
                    }
                    cellDiv.innerHTML = html;
                },
                getCellSync: function (i, j) {
                    // console.log('getCellSync');
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
            };
            this.FattableService.setupTable(parameters);
        };
        FeedProfileInvalidResultsController.prototype.transformFn = function (row, columns, displayColumns) {
            // console.log('transformFn');
            var invalidFields = [];
            var invalidFieldMap = {};
            row.invalidFields = invalidFields;
            row.invalidFieldMap = invalidFieldMap;
            row.invalidField = function (column) {
                return invalidFieldMap[column];
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
        };
        ;
        return FeedProfileInvalidResultsController;
    }());
    exports.FeedProfileInvalidResultsController = FeedProfileInvalidResultsController;
    angular.module(moduleName).controller('FeedProfileInvalidResultsController', ["$scope", "$http", "$window", "FeedService", "RestUrlService", "HiveService", "Utils", "BroadcastService", "FattableService", FeedProfileInvalidResultsController]);
    angular.module(moduleName).directive('thinkbigFeedProfileInvalid', directive);
});
//# sourceMappingURL=profile-invalid.js.map