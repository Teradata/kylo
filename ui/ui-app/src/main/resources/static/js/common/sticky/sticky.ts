import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("sticky",
  ["$document", "$window", ($document, $window) => {
           var $win = angular.element($window); // wrap window object as jQuery object
        return {
            restrict: 'A',
            link: function (scope: any, elem: any, attrs: any) {

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
                    elem.css('padding-left', '15px')
                }

                elem.bind("stickIt", function () {
                    stickIt();
                });

                scope.$on('$destroy', function () {
                    elem.unbind("stickIt");
                });

            }
        }
  }
  ]);
