define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("lazyLoadRetainOrder", ["$ocLazyLoad", "$compile", "$animate", "$parse", "$timeout",
        function ($ocLazyLoad, $compile, $animate, $parse, $timeout) {
            return {
                restrict: 'A',
                terminal: true,
                priority: 1000,
                compile: function compile(element, attrs) {
                    // we store the content and remove it before compilation
                    var content = element[0].innerHTML;
                    element.html('');
                    return function ($scope, $element, $attr) {
                        var model = $parse($attr.lazyLoadRetainOrder);
                        $scope.$watch(function () {
                            return model($scope) || $attr.lazyLoadRetainOrder; // it can be a module name (string), an object, an array, or a scope reference to any of this
                        }, function (moduleName) {
                            if (angular.isDefined(moduleName)) {
                                $ocLazyLoad.load(moduleName, { serie: true }).then(function () {
                                    // Attach element contents to DOM and then compile them.
                                    // This prevents an issue where IE invalidates saved element objects (HTMLCollections)
                                    // of the compiled contents when attaching to the parent DOM.
                                    $animate.enter(content, $element);
                                    // get the new content & compile it
                                    $compile($element.contents())($scope);
                                });
                            }
                        }, true);
                    };
                }
            };
        }
    ]);
});
//# sourceMappingURL=lazy-load-retain-order.js.map