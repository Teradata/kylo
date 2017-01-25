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
/**
 * Directive to auto size the container to fill the rest of the screen based upon the height of the browser window
 *
 * attrs:
 *  - browser-height-selector=  some css selector (i.e. #content) this will be used to determine the height of the element instead of the current element
 *  - browser-height-scroll-y=true/false   show the scroll bar on the content
 *  - browser-height-wait-and-calc= true/false  if true it will wait before calculating the height to get the items in the page default=false
 *  - browser-height-scroll-left or browser-height-scroll-x=##
 *  - browser-height-resize-event=binding to on resize of the window
 *  - browser-height-offset=offset to apply to the height of the element after getting the window size
 *
 */
(function () {

    var directive = function ($window, $compile) {
        return {

            link: function ($scope, element, attrs) {
                element.addClass('browser-height');
                /**
                 *
                 */

                var eleSelector = attrs.browserHeightSelector;

                var scrollY = attrs.browserHeightScrollY;

                var browserHeightWaitAndCalc = attrs.browserHeightWaitAndCalc;

                if(browserHeightWaitAndCalc != undefined && browserHeightWaitAndCalc == "true"){
                    browserHeightWaitAndCalc = true;
                }
                else {
                    browserHeightWaitAndCalc = false;
                }

                if(scrollY == undefined){
                    scrollY = true;
                }
                else {
                    if(scrollY == "false"){
                        scrollY = false;
                    }
                    else {
                        scrollY = true;
                    }
                }

                var scrollX= attrs.browserHeightScrollLeft;
                if(scrollX == undefined) {
                    scrollX = attrs.browserHeightScrollX;
                }

                if(scrollX == "true"){
                    scrollX = true;
                }
                else {
                    scrollX = false;
                }


                var bindResize = attrs.browserHeightResizeEvent;
                if(bindResize == undefined){
                    bindResize = true;
                }
                else {
                    if(bindResize == "true"){
                        bindResize = true;
                    }
                    else {
                        bindResize = false;
                    }
                }


                var ele = element
                if(eleSelector!= undefined){
                    ele = element.find(eleSelector);
                }
                var offsetHeight = attrs.browserHeightOffset;
                if (offsetHeight) {
                    offsetHeight = parseInt(offsetHeight);
                }
                else {
                    offsetHeight = 0;
                }
                function calcHeight() {
                    var windowHeight = angular.element($window).height();
                    var newHeight = windowHeight - offsetHeight;

                    ele.css('height',newHeight+'px');
                    if(scrollY) {
                        ele.css('overflow-y', 'scroll');
                    }
                    if(scrollX) {
                        ele.css('overflow-x','scroll');
                    }
                    else {
                        ele.css('overflow-x','hidden');
                    }
                }

                if(browserHeightWaitAndCalc){
                    setTimeout(function() {
                        calcHeight();
                    },1300)
                }

                if(bindResize) {
                    angular.element($window).bind("resize.browserheight", function () {
                        // if(element.is(':visible')) {
                        calcHeight();
                        //  }
                    });
                }
                    $scope.$on('$destroy', function () {
                        //tabsWrapper.css('top', '0px')
                        angular.element($window).unbind("resize.browserheight");
                        //angular.element('#content').unbind("scroll");
                    });
                setTimeout(function(){
                    calcHeight();
                },10);



                //set margin-top = top

            }
        }
    }




    angular.module(COMMON_APP_MODULE_NAME)
        .directive('browserHeight', directive);

})();



