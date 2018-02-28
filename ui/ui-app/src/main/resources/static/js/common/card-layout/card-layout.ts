import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("cardLayout",  ($compile: any)=>
     {
        return {
            scope: {headerCss: "@", bodyCss: "@", cardCss: '@',cardToolbar: "=?"},
            transclude: {
                'header1': '?headerSection',
                'body1': '?bodySection'
            },
            templateUrl: 'js/common/card-layout/card-layout.html',
            link: function (scope: any, iElem: any, iAttrs: any, ctrl: any, transcludeFn: any) {
                if(angular.isUndefined(scope.cardToolbar)){
                    scope.cardToolbar = true;
                }
            }
        };
    });