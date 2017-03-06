define(['angular','common/module-name'], function (angular,moduleName) {
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
    var directive = function ($window) {
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




    angular.module(moduleName)
        .directive('browserHeight', ["$window", directive]);

});



