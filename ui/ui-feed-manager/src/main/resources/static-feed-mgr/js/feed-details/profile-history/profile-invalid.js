(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/profile-history/profile-invalid-results.html',
            controller: "FeedProfileInvalidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    };

    var controller =  function($scope,$http,$stateParams, FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.data = [];
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[2];

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function() {
            getProfileValidation();
        };

        $scope.gridOptions = {
            columnDefs: [],
            rowHeight: 70,
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true,
            useExternalSorting: false
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

        var addCellTemplate = function(columns) {
            var cellTemplate =
                '<div layout="column" class="ui-grid-cell-contents">' +
                '        <div flex="100" ng-class="{ \'warn\': row.entity.invalidField(col.colDef.name) != undefined }">' +
                '            {{row.entity[col.colDef.name]}}' +
                '        </div>' +
                '        <div flex="100" class="violation hint">' +
                '                {{row.entity.invalidField(col.colDef.name).rule}}' +
                '                <br>' +
                '                {{row.entity.invalidField(col.colDef.name).reason}}' +
                '        </div>' +
                '</div>';

            angular.forEach(columns, function(column) {
                column.cellTemplate = cellTemplate;
            });

            return columns;
        };

        function getProfileValidation(){
            self.loading = true;

            var successFn = function (response) {
                var result = self.queryResults = HiveService.transformResultsToUiGridModel(response, [], transformFn);
                $scope.gridOptions.columnDefs = addCellTemplate(result.columns);
                $scope.gridOptions.columnDefs = _.reject($scope.gridOptions.columnDefs, function(col) {
                    return col.name == 'dlp_reject_reason'
                });
                $scope.gridOptions.data = result.rows;

                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','invalid');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            var promise = $http.get(
                RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(self.model.id),
                { params:
                    {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit
                    }
                });
            promise.then(successFn, errorFn);
            return promise;
        }



        getProfileValidation();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileInvalidResultsController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileInvalid', directive);

})();
