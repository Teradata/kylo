import * as angular from "angular";
import {moduleName} from "../module-name";

export default class CardLayout implements ng.IComponentController {

    static readonly $inject = ["$scope"];

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        if(angular.isUndefined(this.$scope.cardToolbar)){
            this.$scope.cardToolbar = true;
        }
    }

    constructor(private $scope: IScope) {}

}

angular.module(moduleName).component('cardLayout', {
    controller: CardLayout,
    transclude: {
        'header1': '?headerSection',
        'body1': '?bodySection'
    },
    controllerAs: "vm",
    templateUrl: 'js/common/card-layout/card-layout.html'
});