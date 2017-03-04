define(['angular','common/module-name'], function (angular,moduleName) {

    var directive = function ($window) {
        return {
            require:'^thinkbigStepper',
            link: function ($scope, element, attrs,stepperController) {


                var    offset     = element.offset(),
                    topPadding = parseInt(attrs.top)||0,
                    $content = angular.element('#content');

                if(attrs.overflowselector) {
                    $overflowElement = element.find(attrs.overflowselector);
                }
                else {
                    $overflowElement = element;
                }

/*
               $content.bind("scroll", function () {
                    if ($content.scrollTop() > offset.top) {
                        element.css('margin-top',($content.scrollTop())+topPadding)
                      /*  element.stop().animate({
                            marginTop: ($content.scrollTop())+topPadding //- offset.top + topPadding
                        });
                    } else {
                        element.css('margin-top', 0 + topPadding);
                        /*
                        element.stop().animate({
                            marginTop: 0 + topPadding
                        });

                    }
                });
*/

                function getLeftPos(){
                    var ele = null;
                    if(attrs.offsetleftfrom !== undefined) {

                         ele = angular.element(attrs.offsetleftfrom);

                    }
                    else if(attrs.offsetleftfromprevious !== undefined){
                         ele =element.prev();

                    }
                    if(ele != null) {
                        if(ele.length >0){
                            var leftOffset = ele[0].offsetLeft;
                            var width = ele[0].offsetWidth;
                            return leftOffset + width;
                        }
                        return null;
                    }
                }

                function adjustLeft(){
                    var left = getLeftPos();
                    if(left != null) {
                        element.css('left', (left + 216) + 'px');
                    }
                }

                function adjustOverflow(){
                    var windowHeight = angular.element($window).height() - 150;
                    if(element.height() > windowHeight){
                        $overflowElement.css('overflow-y','scroll');
                        $overflowElement.css('height', (windowHeight -50));
                    }
                    else {
                        $overflowElement.css('overflow-y','inherit')
                        $overflowElement.css('height','inherit')
                    }
                }

                element.bind("adjustoverflow", function () {
                     adjustLeft();
                    adjustOverflow();
                });



                angular.element($window).bind("resize.fixedtopright", function () {
                   // if(element.is(':visible')) {
                        adjustLeft();
                    adjustOverflow();
                  //  }
                });

                $scope.$watch(function(){
                    return stepperController.selectedStepIndex;
                }, function(current, old){
                    adjustLeft();
                    adjustOverflow();
                });


                $scope.$on('$destroy',function() {
                    angular.element($window).unbind("resize.fixedtopright");
                  //  $content.unbind("scroll");
                    element.unbind('adjustoverflow');
                });
                adjustLeft();
                adjustOverflow();

            }

        }
    }




    angular.module(moduleName)
        .directive('fixedTopRight', ["$window",directive]);

});



