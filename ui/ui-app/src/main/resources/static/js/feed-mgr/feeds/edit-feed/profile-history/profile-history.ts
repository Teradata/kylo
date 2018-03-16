import * as angular from 'angular';
import 'pascalprecht.translate';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            selectedTabIndex:'='
        },
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history.html',
        controller: "FeedProfileHistoryController",
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
}

var overflowScrollDirective = function ($window:any,$timeout:any) {
    return {
        restrict: "A",
        scope:{},
        link: function ($scope:any, $element:any, attrs:any) {

            function onResize() {
                var $w = angular.element($window);
                var height = $w.height();
                if (attrs.parent) {
                    height = angular.element(attrs.parent).height();
                }
                if (attrs.offsetHeight) {
                    height -= parseInt(attrs.offsetHeight);
                }
                if(height <=0){
                    $timeout(function(){
                        onResize();
                    },10)
                }
                else {
                    $element.css('height', height);
                    $element.css('overflow', 'auto')
                }
            }

            angular.element($window).on('resize.profilehistory', function() {
                onResize();
            });
            onResize();

            $scope.$on('destroy',function() {
                angular.element($window).unbind('resize.profilehistory');
            })

        }

    };
}


export class FeedProfileItemController implements ng.IComponentController {

    constructor(private $scope:any, private $mdDialog:any, private $mdToast:any, private $http:any, private StateService:any
        ,private FeedService:any, private BroadcastService:any , private feed:any ,private profileRow:any ,private currentTab:any) {
            $scope.feed = feed;
            $scope.profileRow = profileRow;
            $scope.processingdate = $scope.profileRow['DATE'];
        
            $scope.processingdttm = $scope.profileRow['PROCESSING_DTTM']
            if(currentTab == 'valid') {
                $scope.selectedTabIndex = 1;
            }
            else if(currentTab == 'invalid') {
                $scope.selectedTabIndex = 2;
            }
            if(currentTab == 'profile-stats') {
                $scope.selectedTabIndex = 0;
            }
        
            $scope.hide = function($event:any) {
                $mdDialog.hide();
            };
        
            $scope.done = function($event:any) {
                $mdDialog.hide();
            };
        
            $scope.cancel = function($event:any) {
                $mdDialog.hide();
            };
        
            $scope.renderPagination = false;
            BroadcastService.subscribe($scope,'PROFILE_TAB_DATA_LOADED',(tab:any) => {
                $scope.renderPagination = true;
            });
        
        
            //Pagination DAta
            $scope.paginationData = {rowsPerPage:50,
                currentPage:1,
                rowsPerPageOptions:['5','10','20','50','100']}
        
            $scope.paginationId = 'profile_stats_0';
            $scope.$watch('selectedTabIndex',(newVal:any) =>{
                $scope.renderPagination = false;
                $scope.paginationId = 'profile_stats_'+newVal;
                $scope.paginationData.currentPage = 1;
            });
        
            $scope.onPaginationChange = function (page:any, limit:any) {
                $scope.paginationData.currentPage = page;
            };
        
    }

}

// var FeedProfileItemController = function($scope, $mdDialog, $mdToast, $http, StateService,FeedService, BroadcastService, feed,profileRow,currentTab){

// };



export class FeedProfileHistoryController  implements ng.IComponentController{

// define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    model:any = this.FeedService.editFeedModel;
    showSummary:boolean = true;
    profileSummary:Array<any> = [];
    loading:boolean = false;

    onValidCountClick(row:any){
        this.showProfileDialog('valid',row);
      }
    onInvalidCountClick(row:any){
        this.showProfileDialog('invalid',row);
      }

    viewProfileStats(row:any) {
        this.showProfileDialog('profile-stats',row);
      }

    showProfileDialog(currentTab:any, profileRow:any) {
          this.$mdDialog.show({
              controller: 'FeedProfileItemController',
              templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history-dialog.html',
              parent: angular.element(document.body),
              clickOutsideToClose:false,
              fullscreen: true,
              locals : {
                  feed:this.model,
                  profileRow:profileRow,
                  currentTab:currentTab
              }
          }).then((msg:any) => {
          },  () => {
          });
      };




    constructor(private $scope:any, private $http:any, private $mdDialog:any, private $filter:any, private FeedService:any
        , private RestUrlService:any, private HiveService:any, private StateService:any, private Utils:any) {
            var getProfileHistory = (function (){
                this.loading = true;
                this.showNoResults = false;
                var successFn = (response:any) => {
                    if (response.data.length == 0) {
                        this.showNoResults = true;
                    }
                var dataMap:any = {};
                var dataArr:any = [];
                var columns:any = HiveService.getColumnNamesForQueryResult(response);
                     if(columns != null) {
                         //get the keys into the map for the different columns
                        var dateColumn:any = _.find(columns,(column)=>{
                            return Utils.strEndsWith(column,'processing_dttm');
                        });
    
                         var metricTypeColumn:any =  _.find(columns,(column)=>{
                             return Utils.strEndsWith(column,'metrictype');
                         });
    
                         var metricValueColumn:any = _.find(columns,(column)=>{
                             return Utils.strEndsWith(column,'metricvalue');
                         });
    
                         //group on date column
                     angular.forEach(response.data,(row:any)=>{
    
                         if(dataMap[row[dateColumn]] == undefined){
                             var timeInMillis = HiveService.getUTCTime(row[dateColumn]);
                             var obj = {'PROCESSING_DTTM':row[dateColumn],'DATE_TIME':timeInMillis,'DATE':new Date(timeInMillis)};
                             dataMap[row[dateColumn]] = obj;
                             dataArr.push(obj);
                         }
                         var newRow = dataMap[row[dateColumn]];
                         var metricType = row[metricTypeColumn];
                         var value = row[metricValueColumn];
                         if(value && metricType == 'MIN_TIMESTAMP' || metricType == 'MAX_TIMESTAMP'){
                             //first check to see if it is millis
                             if(!isNaN(value)){
                                 var dateStr = this.$filter('date')(new Date(''),"yyyy-MM-dd");//tmp was passed as which is not declared anywhere. was returning 'Invalid Date'// replaced by '' here 
                                 value = dateStr;
                             }
                             else {
                                 value = value.substr(0,10); //string the time off the string
                             }
    
                          }
    
                         newRow[metricType] = value;
                     }) ;
    
                         //sort it desc
    
                        // dataArr = _.sortBy(dataArr,dateColumn).reverse();
                         dataArr = _.sortBy(dataArr,'DATE_TIME').reverse();
    
                         this.profileSummary = dataArr;
    
    
                }
                this.loading = false;
    
                }
                var errorFn = (err:any)=> {
                    console.log('ERROR ',err)
                    this.loading = false;
                }
                var promise = $http.get(RestUrlService.FEED_PROFILE_SUMMARY_URL(this.model.id));
                promise.then(successFn, errorFn);
                return promise;
            }).bind(this);
            getProfileHistory();
    
    }









        
        
    }
    
    

    angular.module(moduleName).controller('FeedProfileItemController',["$scope","$mdDialog","$mdToast","$http","StateService","FeedService","BroadcastService","feed","profileRow","currentTab",FeedProfileItemController]);
    
    
    angular.module(moduleName)
        .directive('overflowScroll', ['$window','$timeout',overflowScrollDirective]);
    
    

    angular.module(moduleName).controller('FeedProfileHistoryController', ["$scope","$http","$mdDialog","$filter","FeedService","RestUrlService","HiveService","StateService","Utils",FeedProfileHistoryController]);

    angular.module(moduleName)
        .directive('thinkbigFeedProfileHistory', directive);

