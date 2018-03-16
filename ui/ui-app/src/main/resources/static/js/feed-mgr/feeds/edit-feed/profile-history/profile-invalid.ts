import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

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
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
};



export class FeedProfileInvalidResultsController implements ng.IComponentController{


    model:any = this.FeedService.editFeedModel;
    data:any = [];
    loadingFilterOptions:boolean = false;
    loadingData:boolean = false;
    limitOptions:Array<any> = [10, 50, 100, 500, 1000];
    limit:any = this.limitOptions[2];

    headers:any;
    queryResults:any = [];
    processingdttm:any; 
    rows:any;

    filterOptions:Array<any> = [
        {name: 'None', objectShortClassType: ''},
        {name: 'Type Conversion', objectShortClassType: 'Not convertible to'}
    ];
    filter:any = this.filterOptions[0];





    constructor(private $scope:any, private $http:any, private $window:any, private FeedService:any, 
        private RestUrlService:any, private HiveService:any, private Utils:any, private BroadcastService:any, private FattableService:any) {
            var self = this;
            
            var errorFn = function (err:any) {
                self.loadingData = false;
            };
            function getProfileValidation(){
                self.loadingData = true;
    
                var successFn = (response:any) => {
                    var result = self.queryResults = HiveService.transformResultsToUiGridModel(response, [], transformFn.bind(self));
                    self.headers = result.columns;
                    self.headers = _.reject(self.headers, (col :any) => {
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

        //noinspection JSUnusedGlobalSymbols
        function onLimitChange() {
            getProfileValidation().then(this.setupTable.bind(self));
        };

        //noinspection JSUnusedGlobalSymbols
        function onFilterChange() {
            getProfileValidation().then(this.setupTable.bind(self));
        };

            
        function getFilterOptions() {
            self.loadingFilterOptions = true;
            var filterOptionsOk = (response:any) => {
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
                cellText: (row:any, column:any) => {
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
                fillCell: (cellDiv:any, data:any) => {
                    var html = data.value;
                    if (data.isInvalid) {
                        html += '<span class="violation hint">' + data.rule + '</span>';
                        html += '<span class="violation hint">' + data.reason + '</span>';
                        cellDiv.className += " warn";
                    }
                    cellDiv.innerHTML = html;
                },
                getCellSync: (i:any, j:any) => {
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

        function transformFn(row:any, columns:any ,displayColumns:any){
            var invalidFields:Array<any> = [];
            var invalidFieldMap:Object = {};
            row.invalidFields = invalidFields;
            row.invalidFieldMap = invalidFieldMap;
            row.invalidField = (column:any) => {
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
    

        getFilterOptions();
        getProfileValidation().then(setupTable);
    }

}
angular.module(moduleName).controller('FeedProfileInvalidResultsController', ["$scope","$http","$window","FeedService","RestUrlService","HiveService","Utils","BroadcastService","FattableService",FeedProfileInvalidResultsController]);

angular.module(moduleName).directive('thinkbigFeedProfileInvalid', directive);
