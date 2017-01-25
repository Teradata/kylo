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




    angular.module(MODULE_FEED_MGR)
        .directive('fixedTopRight', directive);

})();



