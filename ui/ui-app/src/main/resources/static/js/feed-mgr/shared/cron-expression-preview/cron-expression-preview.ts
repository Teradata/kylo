
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

export class CronExpressionPreview {
    
    static readonly $inject = ['$scope','$http','RestUrlService'];
    nextDates : any[] = [];
    cronExpression : any;

    constructor(private $scope : IScope, private $http:angular.IHttpService, private RestUrlService:any){
        
        $scope.$watch('cronExpression',(newVal:any) =>{
            if(newVal != null && newVal != ''){
                this.getNextDates();
            }
            else {
                this.nextDates = [];
            }
        });
        this.getNextDates();
    }

    getNextDates() {
        this.$http.get(this.RestUrlService.PREVIEW_CRON_EXPRESSION_URL,{params:{cronExpression:this.cronExpression}})
            .then( (response:any) => {
            this.nextDates = response.data;
        });
    }
}

angular.module(moduleName)
    .component('cronExpressionPreview', {
        bindings : {
            cronExpression: '='
        },
        controller : CronExpressionPreview,
        templateUrl: 'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.html',
    });

