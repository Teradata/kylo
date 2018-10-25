
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('../module-name');

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
        templateUrl: './feed-errors-card-header.html',
    });
