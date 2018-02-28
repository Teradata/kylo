define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    var _this = this;
    Object.defineProperty(exports, "__esModule", { value: true });
    'use strict';
    angular.module(module_name_1.moduleName).run(['$templateCache', function ($templateCache) {
            _this.$templateCache.put('accordion-menu.tmpl.html', ' <md-list class="side-menu " > \n' +
                ' <md-list-item ng-repeat="section in vm.menu" flex layout-fill ng-if="!section.hidden"> \n' +
                ' <menu-link section="section" ng-if="section.type === \'link\'" style="width:100%"></menu-link>\n' +
                ' <menu-toggle section="section" ng-if="section.type === \'toggle\' " style="width:100%"></menu-toggle>\n' +
                '<md-divider></md-divider>' +
                ' </md-list-item>\n'
                + '</md-list>' + '');
        }])
        .directive('accordionMenu', ['$location', '$timeout', 'AccordionMenuService',
        function ($location, $timeout, AccordionMenuService) {
            return {
                bindToController: {
                    menu: '=',
                    collapsed: '=',
                    allowMultipleOpen: '=?'
                },
                controllerAs: 'vm',
                templateUrl: 'accordion-menu.tmpl.html',
                link: function ($scope, $element) {
                },
                controller: function ($scope, $element) {
                    var _this = this;
                    /**
                     * Pointer to the current section that is open
                     * @type {null}
                     */
                    this.openedSection = null;
                    this.autoFocusContent = false;
                    /**
                     * Allow for multiple open sections
                     * @type {boolean}
                     */
                    this.allowMultipleOpen = angular.isDefined(this.allowMultipleOpen) ? this.allowMultipleOpen : false;
                    /**
                     * list all toggle sections
                     * @type {Array}
                     */
                    this.toggleSections = [];
                    /**
                     * Initially open those sections that are set to be expanded
                     */
                    var setToggleSections = function () {
                        _this.toggleSections = _.filter(_this.menu, function (item) {
                            return item.type == 'toggle';
                        });
                        _.each(_this.toggleSections, function (section) {
                            if (section.expanded == true) {
                                section.expandIcon = 'expand_less';
                            }
                            else {
                                section.expandIcon = 'expand_more';
                            }
                            if (section.elementId == undefined) {
                                section.elementId = section.text.toLowerCase().split(' ').join('_');
                            }
                        });
                    };
                    setToggleSections();
                    $scope.$watchCollection(function () { return _this.menu; }, function (newValue, oldValue) {
                        setToggleSections();
                    });
                    this.focusSection = function () {
                        // set flag to be used later when
                        // $locationChangeSuccess calls openPage()
                        $scope.autoFocusContent = true;
                    };
                    /**
                     * is the menu collapsed
                     * @returns {boolean}
                     */
                    this.isCollapsed = function () {
                        return _this.collapsed == true;
                    };
                    /**
                     * open the menu item
                     * @param section
                     */
                    this.openToggleItem = function (section) {
                        AccordionMenuService.openToggleItem(section, $element, _this.allowMultipleOpen, _this.toggleSections);
                    };
                }
            };
        }]);
});
//# sourceMappingURL=accordion-menu.js.map