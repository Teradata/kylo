/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
                    stickIt();
                });

                scope.$on('$destroy', function () {
                    elem.unbind("stickIt");
                });

            }
        }

    })
})();
