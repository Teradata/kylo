define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/module-name');
    var directive = function ($compile, $templateRequest) {
        return {
            restrict: "EA",
            scope: {
                mode: '@',
                processor: '=',
                templateUrl: '@',
                theForm: '='
            },
            template: '<div ng-include="getContentUrl()"></div>',
            link: function ($scope, element, attrs) {
                $scope.getContentUrl = function () {
                    return $scope.templateUrl;
                };
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
    };
    angular.module(moduleName)
        .directive('customProcessorRendering', ['$compile', '$templateRequest', directive]);
});
//# sourceMappingURL=custom-processor-rendering.js.map