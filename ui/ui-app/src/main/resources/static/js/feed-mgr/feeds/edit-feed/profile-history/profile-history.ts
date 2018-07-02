import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from "underscore";

const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
const directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            selectedTabIndex: '='
        },
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history.html',
        controller: "FeedProfileHistoryController",
        link: function ($scope: any, element: any, attrs: any, controller: any) {

        }

    };
};

const overflowScrollDirective = function ($window: any, $timeout: any) {
    return {
        restrict: "A",
        scope: {},
        link: function ($scope: any, $element: any, attrs: any) {

            function onResize() {
                const $w = angular.element($window);
                let height = $w.height();
                if (attrs.parent) {
                    height = angular.element(attrs.parent).height();
                }
                if (attrs.offsetHeight) {
                    height -= parseInt(attrs.offsetHeight);
                }
                if (height <= 0) {
                    $timeout(function () {
                        onResize();
                    }, 10)
                }
                else {
                    $element.css('height', height);
                    $element.css('overflow', 'auto')
                }
            }

            angular.element($window).on('resize.profilehistory', function () {
                onResize();
            });
            onResize();

            $scope.$on('destroy', function () {
                angular.element($window).unbind('resize.profilehistory');
            })

        }

    };
};


export class FeedProfileItemController implements ng.IComponentController {

    constructor(private $scope: any, private $mdDialog: any, private $mdToast: any, private $http: any, private StateService: any
        , private FeedService: any, private BroadcastService: any, private feed: any, private profileRow: any, private currentTab: any) {
        $scope.feed = feed;
        $scope.profileRow = profileRow;
        $scope.processingdate = $scope.profileRow['DATE'];

        $scope.processingdttm = $scope.profileRow['PROCESSING_DTTM']
        if (currentTab == 'valid') {
            $scope.selectedTabIndex = 1;
        }
        else if (currentTab == 'invalid') {
            $scope.selectedTabIndex = 2;
        }
        if (currentTab == 'profile-stats') {
            $scope.selectedTabIndex = 0;
        }

        $scope.hide = function ($event: any) {
            $mdDialog.hide();
        };

        $scope.done = function ($event: any) {
            $mdDialog.hide();
        };

        $scope.cancel = function ($event: any) {
            $mdDialog.hide();
        };

        $scope.renderPagination = false;
        BroadcastService.subscribe($scope, 'PROFILE_TAB_DATA_LOADED', (tab: any) => {
            $scope.renderPagination = true;
        });


        //Pagination DAta
        $scope.paginationData = {
            rowsPerPage: 50,
            currentPage: 1,
            rowsPerPageOptions: ['5', '10', '20', '50', '100']
        }

        $scope.paginationId = 'profile_stats_0';
        $scope.$watch('selectedTabIndex', (newVal: any) => {
            $scope.renderPagination = false;
            $scope.paginationId = 'profile_stats_' + newVal;
            $scope.paginationData.currentPage = 1;
        });

        $scope.onPaginationChange = function (page: any, limit: any) {
            $scope.paginationData.currentPage = page;
        };

    }
}

export class FeedProfileHistoryController implements ng.IComponentController {

    model: any = this.FeedService.editFeedModel;
    showSummary: boolean = true;
    profileSummary: Array<any> = [];
    loading: boolean = false;
    showNoResults: boolean = false;

    constructor(private $scope: any, private $http: any, private $mdDialog: any, private $filter: any, private FeedService: any,
                private RestUrlService: any, private HiveService: any, private StateService: any, private Utils: any) {
    }

    private getProfileHistory() {
        // console.log('getProfileHistory');
        this.loading = true;
        this.showNoResults = false;
        const successFn = (response: any) => {
            if (response.data.length == 0) {
                this.showNoResults = true;
            }
            const dataMap: any = {};
            let dataArr: any = [];
            const columns: any = this.HiveService.getColumnNamesForQueryResult(response);
            if (columns != null) {
                //get the keys into the map for the different columns
                const dateColumn: any = _.find(columns, (column) => {
                    return this.Utils.strEndsWith(column, 'processing_dttm');
                });

                const metricTypeColumn: any = _.find(columns, (column) => {
                    return this.Utils.strEndsWith(column, 'metrictype');
                });

                const metricValueColumn: any = _.find(columns, (column) => {
                    return this.Utils.strEndsWith(column, 'metricvalue');
                });

                //group on date column
                angular.forEach(response.data, (row: any) => {
                    const date = row[dateColumn];
                    if (dataMap[date] == undefined) {
                        const timeInMillis = this.HiveService.getUTCTime(date);
                        const obj = {'PROCESSING_DTTM': date, 'DATE_TIME': timeInMillis, 'DATE': new Date(timeInMillis)};
                        dataMap[date] = obj;
                        dataArr.push(obj);
                    }
                    const newRow = dataMap[date];
                    const metricType = row[metricTypeColumn];
                    let value = row[metricValueColumn];
                    if (value && metricType == 'MIN_TIMESTAMP' || metricType == 'MAX_TIMESTAMP') {
                        //first check to see if it is millis
                        if (!isNaN(value)) {
                            const dateStr = this.$filter('date')(new Date(''), "yyyy-MM-dd");//tmp was passed as which is not declared anywhere. was returning 'Invalid Date'// replaced by '' here
                            value = dateStr;
                        }
                        else {
                            value = value.substr(0, 10); //string the time off the string
                        }

                    }

                    newRow[metricType] = value;
                });

                //sort it desc

                // dataArr = _.sortBy(dataArr,dateColumn).reverse();
                dataArr = _.sortBy(dataArr, 'DATE_TIME').reverse();

                this.profileSummary = dataArr;


            }
            this.loading = false;

        };
        const errorFn = (err: any) => {
            console.log('ERROR ', err)
            this.loading = false;
        };

        const promise = this.$http.get(this.RestUrlService.FEED_PROFILE_SUMMARY_URL(this.model.id));
        promise.then(successFn, errorFn);
        return promise;
    };

    $onInit() {
        this.getProfileHistory();
    }


    public onValidCountClick(row: any) {
        this.showProfileDialog('valid', row);
    }

    public onInvalidCountClick(row: any) {
        this.showProfileDialog('invalid', row);
    }

    public viewProfileStats(row: any) {
        this.showProfileDialog('profile-stats', row);
    }

    public showProfileDialog(currentTab: any, profileRow: any) {
        console.log("showProfileDialog currentTag,profileRow", currentTab, profileRow);
        this.$mdDialog.show({
            controller: 'FeedProfileItemController',
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: {
                feed: this.model,
                profileRow: profileRow,
                currentTab: currentTab
            }
        }).then((msg: any) => {
        }, () => {
        });
    };
}


angular.module(moduleName).controller('FeedProfileItemController',
    ["$scope", "$mdDialog", "$mdToast", "$http", "StateService", "FeedService", "BroadcastService", "feed", "profileRow", "currentTab", FeedProfileItemController]);

angular.module(moduleName)
    .directive('overflowScroll', ['$window', '$timeout', overflowScrollDirective]);

angular.module(moduleName).controller('FeedProfileHistoryController',
    ["$scope", "$http", "$mdDialog", "$filter", "FeedService", "RestUrlService", "HiveService", "StateService", "Utils", FeedProfileHistoryController]);

angular.module(moduleName)
    .directive('thinkbigFeedProfileHistory', directive);

