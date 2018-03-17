import * as angular from "angular";
import {moduleName} from "../module-name";

export default class controller implements ng.IComponentController{
constructor(private $scope: any,
            private $mdDialog: any,
            private $http: any){
            $http({
                    method: "GET",
                    url: "/proxy/v1/about/version"
                }).then(function callSuccess(response: any){
                    $scope.version = response.data;
                },function callFailure(response: any){
                    $scope.version = "Not Available"
                });
                $scope.hide = ()=> {
                    $mdDialog.hide();
                };
                $scope.cancel = ()=> {
                    $mdDialog.cancel();
                };
            }
}

export class AboutKyloService{
    constructor(private $mdDialog: any){}
       showAboutDialog = ()=> {
            this.$mdDialog.show({
                controller: 'AboutKyloDialogController',
                templateUrl: 'js/common/about-kylo/about.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                escapeToClose: true,
                fullscreen: false,
                locals: {}
            }).then((msg: any)=>{
                //callback (success)
            }, function () {
                //callback (failure)
            });
        }
}

angular.module(moduleName).service('AboutKyloService',AboutKyloService);
angular.module(moduleName).controller('AboutKyloDialogController', controller);
