import * as angular from 'angular';
import {moduleName} from "./module-name";


export class controller implements ng.IComponentController{
feedName: any;
constructor(private $scope: any,
            private $transition$: any){
                this.feedName = $transition$.params().feedName;
            }
}
angular.module(moduleName).controller('FeedStatsController',["$scope", "$transition$", controller]);
