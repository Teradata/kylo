/*-
 * #%L
 * thinkbig-ui-common
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

    var directive = function ($window, $compile, BroadcastService) {
        return {

            link: function ($scope, element, attrs) {
                element.addClass('sticky');
                element.prepend('<div class="sticky-anchor"></div>');
                var tabsWrapper = element.find('md-tabs-wrapper');
                tabsWrapper.css('z-index', 49);
                var header = angular.element('.page-header');
                header.css('z-index', 60);

                var side = angular.element('md-sidenav');
                side.css('z-index', 61);
                tabsWrapper.css('position','fixed');
                //tabsWrapper.css('width','100%');
                var width = angular.element('#content').width();
                if(width == 0){
                    width = angular.element($window).width() - side.width();
                }
                tabsWrapper.css('width',width+'px');
                var tabsContentWrapper  = element.find('md-tabs-content-wrapper');
                tabsContentWrapper.css('margin-top','80px');


                var headerHeight = header.height();
                angular.element($window).on("resize.stickytab", function () {
                    resize();
                });
                BroadcastService.subscribe($scope, BroadcastConstants.CONTENT_WINDOW_RESIZED, onContentWindowResized);

                function onContentWindowResized() {
                    resize();
                }

                function resize() {
                    var width = angular.element('#content').width();
                    tabsWrapper.css('width', width + 'px');
                }
/*
                angular.element('#content').bind("scroll", function () {
                    var header = angular.element('.page-header');
                    var headerHeight = header.height();

                    var window_top = 0;
                    var div_top = element.find('.sticky-anchor').offset().top;

                    if (div_top < headerHeight) {
                        if (div_top < 0) {
                            tabsWrapper.css('top', Math.abs(div_top) + headerHeight + 'px')
                        }
                        else {
                            tabsWrapper.css('top', Math.abs(headerHeight - Math.abs(div_top)) + 'px')
                        }
                      //  tabsWrapper.addClass('stick');
                    } else {
                       // tabsWrapper.removeClass('stick');
                        tabsWrapper.css('top', '0px')
                    }
                });
                */
                $scope.$on('$destroy',function() {
                    //tabsWrapper.css('top', '0px')
                    angular.element($window).off("resize.stickytab");
                    //angular.element('#content').unbind("scroll");
                })

            }

            //set margin-top = top

        }
    }




    angular.module(COMMON_APP_MODULE_NAME)
        .directive('stickyTabs', directive);

})();



