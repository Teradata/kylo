import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/module-name');

var directive = function ($compile:any, $templateRequest:any) {
    return {
        restrict: "EA",
        scope: {
            mode: '@',
            processor: '=',
            templateUrl: '@',
            theForm: '='
        },
        template: '<div ng-include="getContentUrl()"></div>',
        link: ($scope:any, element:any, attrs:any) => {
            $scope.getContentUrl = () => {
                return $scope.templateUrl;
            }
        }
        /*
         compile:function(element,attrs) {
         return {
         pre: function preLink($scope, iElement, iAttrs, controller) {


         },
         post: function postLink($scope, $element, iAttrs, controller) {
         console.log('POST COMPILE!!!! ', $scope.templateUrl)
         $templateRequest($scope.templateUrl).then(function(html){
         // Convert the html to an actual DOM node
         var template = angular.element(html);
         // Append it to the directive element
         $element.append(template);
         // And let Angular $compile it
         $compile(template)($scope);
         });

         }
         }

         }
         */

    };
}


    angular.module(moduleName)
        .directive('customProcessorRendering', ['$compile', '$templateRequest', directive]);
