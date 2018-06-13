import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/module-name');

export default class controller  implements ng.IComponentController{
    model: any;
    current: any;
    leftVersion: any;
    rightVersion: any;
    versions: any;
    loading: any;
    loadVersions: any;
    changeRightVersion:any;
    rightFeed: any;
    
constructor(private $scope: any,
            private $http: any,
            private $q: any,
            private $filter: any,
            private RestUrlService: any,
            private FeedService: any) {
        this.model = FeedService.editFeedModel;
        FeedService.versionFeedModel = {};
        FeedService.versionFeedModelDiff = [];
        this.current = $filter('translate')('views.feed-versions-compare.Current');
        this.leftVersion = this.current;
        this.rightVersion = {};
        this.versions = [];
        this.loading = false;

        this.loadVersions = ()=> {
            FeedService.getFeedVersions(this.model.feedId).then((result: any)=> {
                this.versions = result.versions;
                this.leftVersion = this.current + " (" + this.getCurrentVersion().name + ")";
            }, (err: any)=> {

            });
        };

        this.changeRightVersion =()=> {
            var version = _.find(this.versions, (v: any)=>{
                return v.id === this.rightVersion;
            });
            this.loading = true;
            var diff = FeedService.diffFeedVersions(this.model.feedId, this.rightVersion, this.getCurrentVersion().id).then((result: any)=> {
                // console.log('diff', result.difference);
                FeedService.versionFeedModelDiff = [];
                _.each(result.difference.patch, (patch: any)=> {
                    FeedService.versionFeedModelDiff[patch.path] = patch;
                });
            }, (err: any)=> {

            });

            var versionedFeed = FeedService.getFeedVersion(this.model.feedId, this.rightVersion).then((result: any)=> {
                this.rightFeed = result.entity;
                FeedService.versionFeedModel = this.rightFeed;
                FeedService.versionFeedModel.version = version;
            }, (err: any)=> {

            });

            Promise.all([diff, versionedFeed]).then((result: any)=>{
                this.loading = false;
            }).catch((err: any)=> {
                this.loading = false;
            });

        };

        this.loadVersions();
    }

    getCurrentVersion=() =>{
            return this.versions[0];
        }
}

angular.module(moduleName).controller('FeedVersionsCompareController', ["$scope", "$http", "$q", "$filter", "RestUrlService", "FeedService", controller]);
angular.module(moduleName).directive('thinkbigFeedVersionsCompare', [
    () =>{
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/versions/feed-versions-compare.html',
            controller: "FeedVersionsCompareController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }
    ]);