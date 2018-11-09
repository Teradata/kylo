import * as angular from 'angular';

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

// angular.module('cronExpressionPreview')
//     .component('cronExpressionPreview', {
//         bindings : {
//             cronExpression: '='
//         },
//         controller : CronExpressionPreview,
//         templateUrl: './cron-expression-preview.html',
//     });

