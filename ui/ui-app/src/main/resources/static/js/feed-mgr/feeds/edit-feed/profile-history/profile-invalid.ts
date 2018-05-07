import * as angular from 'angular';
import * as _ from "underscore";

const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

const directive = function () {
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
        link: function ($scope: any, element: any, attrs: any, controller: any) {

        }

    };
};


export class FeedProfileInvalidResultsController implements ng.IComponentController {

    model: any = this.FeedService.editFeedModel;
    data: any = [];
    loadingFilterOptions: boolean = false;
    loadingData: boolean = false;
    limitOptions: Array<any> = [10, 50, 100, 500, 1000];
    limit: any = this.limitOptions[2];

    headers: any;
    queryResults: any = [];
    processingdttm: any;
    rows: any;

    filterOptions: Array<any> = [
        {name: 'None', objectShortClassType: ''},
        {name: 'Type Conversion', objectShortClassType: 'Not convertible to'}
    ];
    filter: any = this.filterOptions[0];

    constructor(private $scope: any, private $http: any, private $window: any, private FeedService: any,
                private RestUrlService: any, private HiveService: any, private Utils: any, private BroadcastService: any, private FattableService: any) {
    }

    $onInit() {
        this.getFilterOptions();
        this.getProfileValidation().then(this.setupTable.bind(this));
    }

    private errorFn(err: any) {
        this.loadingData = false;
    };

    private getProfileValidation() {
        // console.log('get profile validation');
        this.loadingData = true;

        const successFn = (response: any) => {
            // console.log('successFn');
            const result = this.queryResults = this.HiveService.transformResultsToUiGridModel(response, [], this.transformFn.bind(this));
            this.headers = result.columns;
            this.headers = _.reject(this.headers, (col: any) => {
                return col.name == 'dlp_reject_reason'
            });
            this.rows = result.rows;

            this.loadingData = false;
            this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'invalid');
        };

        const promise = this.$http.get(
            this.RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(this.model.id),
            {
                params:
                    {
                        'processingdttm': this.processingdttm,
                        'limit': this.limit,
                        'filter': _.isUndefined(this.filter) ? '' : this.filter.objectShortClassType
                    }
            });
        promise.then(successFn, this.errorFn);
        return promise;
    }

    private onLimitChange() {
        this.getProfileValidation().then(this.setupTable.bind(this));
    };

    private onFilterChange() {
        this.getProfileValidation().then(this.setupTable.bind(this));
    };

    private getFilterOptions() {
        this.loadingFilterOptions = true;
        const filterOptionsOk = (response: any) => {
            this.filterOptions = _.union(this.filterOptions, response.data);
            this.loadingFilterOptions = false;
        };
        const promise = this.$http.get(this.RestUrlService.AVAILABLE_VALIDATION_POLICIES, {cache: true});
        promise.then(filterOptionsOk, this.errorFn);
        return promise;
    }

    private setupTable() {
        // console.log('setupTable');
        const parameters = {
            tableContainerId: "invalidProfile",
            headers: this.headers,
            rows: this.rows,
            rowHeight: 45,
            cellText: (row: any, column: any) => {
                // console.log('cellText');
                //return the longest text out of cell value and its validation errors
                const textArray = [];
                textArray.push(row[column.displayName]);
                const validationError = row.invalidFieldMap[column.displayName];
                if (validationError !== undefined) {
                    textArray.push(validationError.rule);
                    textArray.push(validationError.reason);
                }
                return textArray.sort(function (a, b) {
                    return b.length - a.length
                })[0];
            },
            fillCell: (cellDiv: any, data: any) => {
                // console.log('fillCell');
                let html = _.escape(data.value);
                if (data.isInvalid) {
                    html += '<span class="violation hint">' + data.rule + '</span>';
                    html += '<span class="violation hint">' + data.reason + '</span>';
                    cellDiv.className += " warn";
                }
                cellDiv.innerHTML = html;
            },
            getCellSync: (i: any, j: any) => {
                // console.log('getCellSync');
                const displayName = this.headers[j].displayName;
                const row = this.rows[i];
                if (row === undefined) {
                    //occurs when filtering table
                    return undefined;
                }
                const invalidFieldMap = row.invalidFieldMap[displayName];
                const isInvalid = invalidFieldMap !== undefined;
                const rule = isInvalid ? invalidFieldMap.rule : "";
                const reason = isInvalid ? invalidFieldMap.reason : "";
                return {
                    "value": row[displayName],
                    "isInvalid": isInvalid,
                    "rule": rule,
                    "reason": reason
                };

            }
        };
        this.FattableService.setupTable(parameters);
    }
    
    private transformFn(row: any, columns: any, displayColumns: any) {
        // console.log('transformFn');
        const invalidFields: Array<any> = [];
        const invalidFieldMap: Object = {};
        row.invalidFields = invalidFields;
        row.invalidFieldMap = invalidFieldMap;
        row.invalidField = (column: any) => {
            return invalidFieldMap[column];
        };
        let _index = _.indexOf(displayColumns, 'dlp_reject_reason');
        let rejectReasons = row[columns[_index]];
        if (rejectReasons != null) {
            rejectReasons = angular.fromJson(rejectReasons);
        }
        if (rejectReasons != null) {
            angular.forEach(rejectReasons, function (rejectReason) {
                if (rejectReason.scope == 'field') {
                    const field = rejectReason.field;
                    const copy = angular.copy(rejectReason);
                    _index = _.indexOf(displayColumns, field);
                    copy.fieldValue = row[columns[_index]];
                    invalidFields.push(copy);
                    invalidFieldMap[columns[_index]] = copy;
                }
            });
        }

    };

}

angular.module(moduleName).controller('FeedProfileInvalidResultsController',
    ["$scope", "$http", "$window", "FeedService", "RestUrlService", "HiveService", "Utils", "BroadcastService", "FattableService", FeedProfileInvalidResultsController]);

angular.module(moduleName).directive('thinkbigFeedProfileInvalid', directive);
