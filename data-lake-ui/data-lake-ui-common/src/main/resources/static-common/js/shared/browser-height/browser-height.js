(function () {

    var directive = function ($window, $compile) {
        return {

            link: function ($scope, element, attrs) {
                element.addClass('browser-height');
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



