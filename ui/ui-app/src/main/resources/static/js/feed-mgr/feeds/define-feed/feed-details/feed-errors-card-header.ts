
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

export class FeedErrorCardHeader{
    static readonly $inject = ["$scope","FeedCreationErrorService"];
    
    $onInit(){
        this.ngOnInit();
    }

    ngOnInit() {
        this.$scope.hasFeedCreationErrors = () => {
            return this.FeedCreationErrorService.hasErrors();
        };
        this.$scope.showFeedErrorsDialog = this.FeedCreationErrorService.showErrorDialog;
    }
    constructor(private $scope:any,private FeedCreationErrorService:any){
        
        
    }
}
angular.module(moduleName).
    component("thinkbigDefineFeedGeneralInfao", {
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/feed-errors-card-header.html',
    });
