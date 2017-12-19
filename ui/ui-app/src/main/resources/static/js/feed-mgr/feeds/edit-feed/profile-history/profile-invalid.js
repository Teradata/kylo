define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {


    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-invalid-results.html',
            controller: "FeedProfileInvalidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    };

    var controller =  function($scope,$http,$window,FeedService, RestUrlService, HiveService, Utils,BroadcastService,FattableService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.data = [];
        this.loadingFilterOptions = false;
        this.loadingData = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[2];

        this.filterOptions = [
            {name: 'None', objectShortClassType: ''},
            {name: 'Type Conversion', objectShortClassType: 'Not convertible to'}
        ];
        this.filter = self.filterOptions[0];

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function() {
            getProfileValidation().then(setupTable);
        };

        //noinspection JSUnusedGlobalSymbols
        this.onFilterChange = function() {
            getProfileValidation().then(setupTable);
        };

        var transformFn = function(row,columns,displayColumns){
            var invalidFields = [];
            var invalidFieldMap = {};
            row.invalidFields = invalidFields;
            row.invalidFieldMap = invalidFieldMap;
            row.invalidField = function(column){
                return this.invalidFieldMap[column];
            };
            var _index = _.indexOf(displayColumns,'dlp_reject_reason');
            var rejectReasons = row[columns[_index]];
            if(rejectReasons != null){
                rejectReasons = angular.fromJson(rejectReasons);
            }
            if(rejectReasons != null){
                angular.forEach(rejectReasons,function(rejectReason){
                    if(rejectReason.scope =='field'){
                        var field = rejectReason.field;
                        var copy = angular.copy(rejectReason);
                        _index = _.indexOf(displayColumns,field);
                        copy.fieldValue = row[columns[_index]];
                        invalidFields.push(copy);
                        invalidFieldMap[columns[_index]] = copy;
                    }
                });
            }

        };

        var errorFn = function (err) {
            self.loadingData = false;
        };
        function getProfileValidation(){
            self.loadingData = true;

            var successFn = function (response) {
                var result = self.queryResults = HiveService.transformResultsToUiGridModel(response, [], transformFn);
                self.headers = result.columns;
                self.headers = _.reject(self.headers, function(col) {
                    return col.name == 'dlp_reject_reason'
                });
                self.rows = result.rows;

                self.loadingData = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','invalid');
            };

            var promise = $http.get(
                RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(self.model.id),
                { params:
                    {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit,
                        'filter': _.isUndefined(self.filter) ? '' : self.filter.objectShortClassType
                    }
                });
            promise.then(successFn, errorFn);
            return promise;
        }

        function getFilterOptions() {
            self.loadingFilterOptions = true;
            var filterOptionsOk = function(response) {
                self.filterOptions = _.union(self.filterOptions, response.data);
                self.loadingFilterOptions = false;
            };
            var promise = $http.get(RestUrlService.AVAILABLE_VALIDATION_POLICIES, {cache:true});
            promise.then(filterOptionsOk, errorFn);
            return promise;
        }

        function setupTable() {
            FattableService.setupTable({
                tableContainerId: "invalidProfile",
                headers: self.headers,
                rows: self.rows,
                cellText: function(row, column) {
                    //return the longest text out of cell value and its validation errors
                    var textArray = [];
                    textArray.push(row[column.displayName]);
                    var validationError = row.invalidFieldMap[column.displayName];
                    if (validationError !== undefined) {
                        textArray.push(validationError.rule);
                        textArray.push(validationError.reason);
                    }
                    return textArray.sort(function(a,b) { return b.length - a.length })[0];
                },
                fillCell: function(cellDiv, data) {
                    var html = data.value;
                    if (data.isInvalid) {
                        html += '<span class="violation hint">' + data.rule + '</span>';
                        html += '<span class="violation hint">' + data.reason + '</span>';
                        cellDiv.className += " warn";
                    }
                    cellDiv.innerHTML = html;
                },
                getCellSync: function(i, j) {
                    var displayName = this.headers[j].displayName;
                    var row = this.rows[i];
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


        getFilterOptions();
        getProfileValidation().then(setupTable);
    };

    angular.module(moduleName).controller('FeedProfileInvalidResultsController', ["$scope","$http","$window","FeedService","RestUrlService","HiveService","Utils","BroadcastService","FattableService",controller]);

    angular.module(moduleName).directive('thinkbigFeedProfileInvalid', directive);

});
