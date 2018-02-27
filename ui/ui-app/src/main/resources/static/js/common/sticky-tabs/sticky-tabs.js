define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName).directive("stickyTabs", ["$window", "$compile", "BroadcastService", function () {
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
                    tabsWrapper.css('position', 'absolute');
                    //tabsWrapper.css('width','100%');
                    var width = angular.element('#content').width();
                    if (width == 0) {
                        width = angular.element(this.$window).width() - side.width();
                    }
                    tabsWrapper.css('width', width + 'px');
                    var tabsContentWrapper = element.find('md-tabs-content-wrapper');
                    tabsContentWrapper.css('margin-top', '80px');
                    var headerHeight = header.height();
                    angular.element(this.$window).on("resize.stickytab", function () {
                        resize();
                    });
                    this.BroadcastService.subscribe($scope, this.BroadcastConstants.CONTENT_WINDOW_RESIZED, this.onContentWindowResized);
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
                    $scope.$on('$destroy', function () {
                        //tabsWrapper.css('top', '0px')
                        angular.element(this.$window).off("resize.stickytab");
                        //angular.element('#content').unbind("scroll");
                    });
                }
                //set margin-top = top
            };
        }
    ]);
});
//# sourceMappingURL=sticky-tabs.js.map