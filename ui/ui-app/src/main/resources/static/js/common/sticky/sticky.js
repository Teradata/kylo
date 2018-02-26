define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    var _this = this;
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("sticky", ["$document", "$window", function () {
            var $win = angular.element(_this.$window); // wrap window object as jQuery object
            return {
                restrict: 'A',
                link: function (scope, elem, attrs) {
                    var scrollSelector = attrs.scrollSelector;
                    var offset = angular.isDefined(attrs.offset) ? parseInt(attrs.offset) : 0;
                    var scrollContainerElem = angular.isDefined(scrollSelector) ? angular.element(scrollSelector) : $win;
                    var currLeftPos = elem[0].offsetLeft;
                    scrollContainerElem.on('scroll', function (e) {
                        stickIt();
                    });
                    function stickIt() {
                        var scrollAmount = scrollContainerElem.scrollTop();
                        elem.css('top', (scrollAmount + offset) + 'px');
                        elem.css('position', 'absolute');
                        elem.css('padding-left', '15px');
                    }
                    elem.bind("stickIt", function () {
                        stickIt();
                    });
                    scope.$on('$destroy', function () {
                        elem.unbind("stickIt");
                    });
                }
            };
        }
    ]);
});
//# sourceMappingURL=sticky.js.map