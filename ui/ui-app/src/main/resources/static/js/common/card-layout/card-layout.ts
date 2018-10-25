import * as angular from "angular";
import {moduleName} from "../module-name";

export default class CardLayout implements ng.IComponentController {

    headerCss: any;
    bodyCss: any; 
    cardCss: any;
    cardToolbar: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        if(angular.isUndefined(this.cardToolbar)){
            this.cardToolbar = true;
        }
    }
    
}

angular.module(moduleName).component('cardLayout', {
    controller: CardLayout,
    bindings: {
        headerCss: "@", 
        bodyCss: "@", 
        cardCss: '@',
        cardToolbar: "=?"
    },
    transclude: {
        'header1': '?headerSection',
        'body1': '?bodySection'
    },
    controllerAs: "vm",
    templateUrl: './card-layout.html'
});