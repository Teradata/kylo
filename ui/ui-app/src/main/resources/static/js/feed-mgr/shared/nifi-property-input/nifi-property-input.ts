import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


class controller {

    property:any;
    theForm: any;
    propertyDisabled: any;
    onPropertyChange: any;

    static readonly $inject = ["$scope", "element"];
    constructor(private $scope: IScope, private element: angular.IAugmentedJQuery) {
        element.addClass('nifi-property-input layout-padding-top-bottom')
        if (this.property.formKey == null) {
            var formKey = this.property.key.split(' ').join('_') + this.property.processorName.split(' ').join('_')
            this.property.formKey = formKey.toLowerCase();
        }
        if (this.propertyDisabled == undefined) {
            this.propertyDisabled = false;
        }

        

        if (this.property.renderType == 'select' && this.property.value != null) {
            if (this.onPropertyChange != undefined) {
                this.onPropertyChange(this.property);
            }
        }

        if (this.property.renderType == 'select') {
            if (this.property.renderOptions == null || this.property.renderOptions == undefined) {
                this.property.renderOptions = {};
            }
            if (this.property.renderOptions['selectCustom'] == 'true') {
                if (this.property.renderOptions['selectOptions']) {
                    this.property.selectOptions = angular.fromJson(this.property.renderOptions['selectOptions']);
                }
                else {
                    this.property.selectOptions = [];
                }
            }
        }


        if (this.property.renderType == 'checkbox-custom') {
            if (this.property.renderOptions == null || this.property.renderOptions == undefined) {
                this.property.renderOptions = {};
            }
            var trueValue = this.property.renderOptions['trueValue'];
            if (StringUtils.isBlank(trueValue)) {
                this.property.renderOptions['trueValue'] = 'true';
            }
            var falseValue = this.property.renderOptions['falseValue'];
            if (StringUtils.isBlank(falseValue)) {
                this.property.renderOptions['falseValue'] = 'false';
            }
        }
    }
    onPropertyChanged = (property:any) => {
        if (this.onPropertyChange != undefined) {
            this.onPropertyChange(this.property);
        }
    }
}

angular.module(moduleName).component('nifiPropertyInput', {
    bindings: {
        property: '=',
        theForm: '=',
        propertyDisabled: '=?',
        onPropertyChange: '&?'
    },
    controller : controller,
    controllerAs : "vm",
    templateUrl: 'js/feed-mgr/shared/nifi-property-input/nifi-property-input.html',
});

var emptyStringToNull = () => {
    // There is a distinction in Nifi between empty string and null, e.g. some
    // processors consider empty string invalid where null is valid. Without
    // this directive it would be impossible to set existing values to null
    return {
        restrict: 'EA',
        require: '?ngModel',
        link: ($scope: any, element: any, attr: any, ngModel: any) => {
            if (ngModel) {
                const replaceEmptyStringWithNull = function (value: any) {
                    return value === '' ? null : value;
                };
                ngModel.$parsers.push(replaceEmptyStringWithNull);
            }
        }
    };
};

angular.module(moduleName).directive('emptyStringToNull', emptyStringToNull);


