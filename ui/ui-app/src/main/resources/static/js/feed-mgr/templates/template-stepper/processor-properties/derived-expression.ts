import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";
import { IAugmentedJQuery } from '@angular/upgrade/static/src/common/angular1';
import {Input} from "@angular/core";

export class DerivedExpressionController {
    
    @Input()
    public ngModel: string;
    
    static readonly $inject = ["$scope","$element","RegisterTemplateService"];

    constructor(private $scope: IScope,private $element: angular.IAugmentedJQuery,private RegisterTemplateService:any){
       
        $scope.$watch(
            'ngModel',
            (newValue:any) => {
                if(newValue != null) {
                    var derivedValue = this.RegisterTemplateService.deriveExpression(newValue,true)
                    this.$element.html(derivedValue);
                }
                else {
                    this.$element.html('');
                }
            });

    }
}

angular.module(moduleName).component('thinkbigDerivedExpression',{
    bindings : {
        ngModel : "="
    },
    controller : DerivedExpressionController
});