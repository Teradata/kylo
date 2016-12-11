(function () {

    angular.module(MODULE_FEED_MGR).directive('sticky', function ($document, $window) {
        var $win = angular.element($window); // wrap window object as jQuery object
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
                    elem.css('padding-left', '15px')
                }

                elem.bind("stickIt", function () {
                    console.log('stick it')
                    stickIt();
                });

                scope.$on('$destroy', function () {
                    elem.unbind("stickIt");
                    console.log('destroy stick it')

                });

            }
        }

    })
})();
