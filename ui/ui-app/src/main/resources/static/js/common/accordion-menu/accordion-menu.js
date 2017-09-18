define(['angular','common/module-name'], function (angular,moduleName) {
    'use strict';

    angular.module(moduleName)
        .run(['$templateCache', function ($templateCache) {
            $templateCache.put('accordion-menu.tmpl.html',
                ' <md-list class="side-menu " > \n' +
                ' <md-list-item ng-repeat="section in vm.menu" flex layout-fill ng-if="!section.hidden"> \n' +
            ' <menu-link section="section" ng-if="section.type === \'link\'" style="width:100%"></menu-link>\n' +
            ' <menu-toggle section="section" ng-if="section.type === \'toggle\' " style="width:100%"></menu-toggle>\n' +
                '<md-divider></md-divider>' +
            ' </md-list-item>\n'
                + '</md-list>' + '');
        }])
        .directive('accordionMenu', ['$location','$timeout','AccordionMenuService',function ($location,$timeout, AccordionMenuService) {
            return {
                bindToController: {
                    menu: '=',
                    collapsed: '=',
                    allowMultipleOpen:'=?'
                },
                controllerAs: 'vm',
                templateUrl: 'accordion-menu.tmpl.html',
                link: function ($scope, $element) {

                },
                controller: function($scope,$element){
                    var self = this;
                    /**
                     * Pointer to the current section that is open
                     * @type {null}
                     */
                    self.openedSection = null;

                    self.autoFocusContent = false;
                    /**
                     * Allow for multiple open sections
                     * @type {boolean}
                     */
                    self.allowMultipleOpen = angular.isDefined(self.allowMultipleOpen) ? self.allowMultipleOpen : false;

                    /**
                     * list all toggle sections
                     * @type {Array}
                     */
                    self.toggleSections = []


                    /**
                     * Initially open those sections that are set to be expanded
                     */
                    var setToggleSections = function(){
                        self.toggleSections = _.filter(self.menu,function(item){
                            return item.type == 'toggle';
                        });

                        _.each(self.toggleSections,function(section){
                            if(section.expanded == true) {
                                section.expandIcon = 'expand_less';
                            }
                            else {
                                section.expandIcon = 'expand_more';
                            }
                            if(section.elementId == undefined) {
                                section.elementId = section.text.toLowerCase().split(' ').join('_');
                            }
                        })
                    }

                    setToggleSections();

                    $scope.$watchCollection(
                        function(){ return self.menu},
                        function( newValue, oldValue ) {
                         setToggleSections();
                        }
                    );



                    self.focusSection = function () {
                        // set flag to be used later when
                        // $locationChangeSuccess calls openPage()
                        $scope.autoFocusContent = true;
                    };

                    /**
                     * is the menu collapsed
                     * @returns {boolean}
                     */
                    self.isCollapsed = function() {
                        return self.collapsed == true;
                    }

                    /**
                     * open the menu item
                     * @param section
                     */
                    self.openToggleItem = function (section) {
                        AccordionMenuService.openToggleItem(section,$element,self.allowMultipleOpen,self.toggleSections);

                    }

                }
            };
        }])
});
