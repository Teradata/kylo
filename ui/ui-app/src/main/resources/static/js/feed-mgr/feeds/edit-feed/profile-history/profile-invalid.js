define(['angular','feed-mgr/feeds/edit-feed/module-name', 'fattable'], function (angular,moduleName) {


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

    var controller =  function($scope,$http,FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

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
            var MIN_COLUMN_WIDTH = 50;
            var MAX_COLUMN_WIDTH = 300;
            var ROW_HEIGHT = 53;
            var HEADER_HEIGHT = 40;
            var PADDING = 40;
            var HEADER_FONT = "bold 12px Roboto, \"Helvetica Neue\", sans-serif";
            var ROW_FONT = "14px Roboto, \"Helvetica Neue\", sans-serif";
            var TABLE_CONTAINER_ID = "#invalidProfile";

            var tableData = new fattable.SyncTableModel();
            var painter = new fattable.Painter();

            function get2dContext(font) {
                var canvas = document.createElement("canvas");
                document.createDocumentFragment().appendChild(canvas);
                var context = canvas.getContext("2d");
                context.font = font;
                return context;
            }

            var headerContext = get2dContext(HEADER_FONT);
            var rowContext = get2dContext(ROW_FONT);

            tableData.columnHeaders = [];
            var columnWidths = [];
            _.each(self.headers, function(column) {
                var headerTextWidth = headerContext.measureText(column.displayName).width;
                var rowTextWidth = _.reduce(self.rows, function (previousMax, row) {
                    var textWidth = rowContext.measureText(row[column.displayName]).width;
                    var validationError = row.invalidFieldMap[column.displayName];
                    var ruleTextWidth = 0;
                    var reasonTextWidth = 0;
                    if (validationError !== undefined) {
                        ruleTextWidth = rowContext.measureText(validationError.rule).width;
                        reasonTextWidth = rowContext.measureText(validationError.reason).width;
                    }
                    return Math.max(previousMax, textWidth, ruleTextWidth, reasonTextWidth);
                }, MIN_COLUMN_WIDTH);

                columnWidths.push(Math.min(MAX_COLUMN_WIDTH, Math.max(headerTextWidth, rowTextWidth)) + PADDING);
                tableData.columnHeaders.push(column.displayName);
            });

            painter.fillCell = function (cellDiv, data) {
                if (data === undefined) {
                    return;
                }
                var classname = "";
                if (data.rowId % 2 === 0) {
                    classname = "even";
                }
                else {
                    classname = "odd";
                }

                var html = data.value;
                if (data.isInvalid) {
                    html += '<br><span class="violation hint">' + data.rule + '</span>';
                    html += '<br><span class="violation hint">' + data.reason + '</span>';
                    classname += " warn";
                }
                cellDiv.innerHTML = html;
                cellDiv.className = classname;
            };

            painter.fillHeader = function(headerDiv, header) {
                headerDiv.innerHTML = '<div>' + header + '</div>';
            };

            tableData.getCellSync = function (i, j) {
                var displayName = self.headers[j].displayName;
                var row = self.rows[i];
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
                    "reason": reason,
                    "rowId": i
                };
            };

            tableData.getHeaderSync = function(j) {
                return tableData.columnHeaders[j];
            };

            var table = fattable({
                "container": TABLE_CONTAINER_ID,
                "model": tableData,
                "nbRows": self.rows.length,
                "rowHeight": ROW_HEIGHT,
                "headerHeight": HEADER_HEIGHT,
                "painter": painter,
                "columnWidths": columnWidths
            });

            window.onresize = function () {
                table.setup();
            };

            table.setup();
        }

        getFilterOptions();
        getProfileValidation().then(setupTable);
    };

    angular.module(moduleName).controller('FeedProfileInvalidResultsController', ["$scope","$http","FeedService","RestUrlService","HiveService","Utils","BroadcastService",controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedProfileInvalid', directive);

});
