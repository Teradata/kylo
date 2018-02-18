define(["require", "exports", "angular", "underscore", "./module-name", "./module", "../services/services.module", "pascalprecht.translate"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccessConstants = require('../constants/AccessConstants');
    var directive = /** @class */ (function () {
        function directive($mdSidenav, $mdDialog, $rootScope, $transitions, $timeout, SideNavService, AccessControlService, StateService, AccordionMenuService, AngularModuleExtensionService, $filter) {
            var _this = this;
            this.$mdSidenav = $mdSidenav;
            this.$mdDialog = $mdDialog;
            this.$rootScope = $rootScope;
            this.$transitions = $transitions;
            this.$timeout = $timeout;
            this.SideNavService = SideNavService;
            this.AccessControlService = AccessControlService;
            this.StateService = StateService;
            this.AccordionMenuService = AccordionMenuService;
            this.AngularModuleExtensionService = AngularModuleExtensionService;
            this.$filter = $filter;
            this.restrict = "E";
            this.scope = {};
            this.templateUrl = 'js/side-nav/side-nav.html';
            this.link = function ($scope, $element) {
                /**
               * Build the Feed Manager Left Nav
               * @param allowed
               */
                // let AccessConstants = new AccessConstants();
                var buildFeedManagerMenu = function () {
                    var links = [];
                    links.push({ sref: "feeds", type: 'link', icon: "linear_scale",
                        text: _this.$filter('translate')('views.main.feeds'),
                        permission: AccessConstants.UI_STATES.FEEDS.permissions });
                    links.push({ sref: "categories", type: 'link', icon: "folder_special", text: _this.$filter('translate')('views.main.categories'), permission: AccessConstants.UI_STATES.CATEGORIES.permissions });
                    links.push({ sref: "service-level-agreements", type: 'link', icon: "beenhere", text: _this.$filter('translate')('views.main.sla'), permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions });
                    links.push({ sref: "visual-query", type: 'link', icon: "transform", text: _this.$filter('translate')('views.main.visual-query'), fullscreen: true, permission: AccessConstants.UI_STATES.VISUAL_QUERY.permissions });
                    links.push({ sref: "catalog", type: 'link', icon: "grid_on", text: _this.$filter('translate')('views.main.tables'), permission: AccessConstants.UI_STATES.TABLES.permissions });
                    addExtensionLinks(MENU_KEY.FEED_MGR, links);
                    var menu = ({ type: 'toggle',
                        text: _this.$filter('translate')('views.main.feed-manage'),
                        narrowText: _this.$filter('translate')('views.main.feed-manage-narrow'),
                        expanded: true,
                        elementId: 'toggle_feed_manager',
                        links: links });
                    menu.links = links;
                    menuMap[MENU_KEY.FEED_MGR] = menu;
                    return menu;
                };
                /**
                 * Build the Ops Manager Left Nav
                 * TODO Switch Permissions to correct ones (i.e remove OPERATIONS_MANAGER_ACCESS, add in detailed permission AccessConstants.CHARTS_ACCESS)
                 * @param allowed
                 */
                var buildOpsManagerMenu = function () {
                    var links = [];
                    links.push({ sref: "dashboard", type: 'link', icon: "dashboard", text: _this.$filter('translate')('views.main.dashboard'), defaultActive: false, permission: AccessConstants.UI_STATES.DASHBOARD.permissions });
                    links.push({ sref: "service-health", type: 'link', icon: "vector_triangle", text: _this.$filter('translate')('views.main.services'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_HEALTH.permissions });
                    links.push({ sref: "jobs", type: 'link', icon: "settings", text: _this.$filter('translate')('views.main.jobs'), defaultActive: false, permission: AccessConstants.UI_STATES.JOBS.permissions });
                    links.push({ sref: "alerts", icon: "notifications", text: _this.$filter('translate')('views.main.alerts'), defaultActive: false, permission: AccessConstants.UI_STATES.ALERTS.permissions });
                    links.push({ sref: "service-level-assessments", type: 'link', icon: "work", text: _this.$filter('translate')('views.main.sla-assessments'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions });
                    links.push({ sref: "scheduler", type: 'link', icon: "today", text: _this.$filter('translate')('views.main.sla-schedule'), defaultActive: false, permission: AccessConstants.UI_STATES.SCHEDULER.permissions });
                    links.push({ sref: "charts", type: 'link', icon: "insert_chart", text: _this.$filter('translate')('views.main.charts'), defaultActive: false, permission: AccessConstants.UI_STATES.CHARTS.permissions });
                    addExtensionLinks(MENU_KEY.OPS_MGR, links);
                    var menu = ({ type: 'toggle',
                        text: _this.$filter('translate')('views.main.operations'),
                        narrowText: _this.$filter('translate')('views.main.operations-narrow'),
                        expanded: false,
                        links: links });
                    menu.links = links;
                    menuMap[MENU_KEY.OPS_MGR] = menu;
                    return menu;
                };
                /**
                  * Build the Admin Menu
                  * @param allowed
                  */
                var buildAdminMenu = function () {
                    var links = [];
                    links.push({ sref: "datasources", type: "link", icon: "storage", text: _this.$filter('translate')('views.main.data-sources'), defaultActive: false, permission: _this.AccessControlService.DATASOURCE_ACCESS });
                    links.push({ sref: "domain-types", type: "link", icon: "local_offer", text: _this.$filter('translate')('views.main.domain-types'), defaultActive: false, permission: _this.AccessControlService.FEEDS_ADMIN });
                    links.push({ sref: "business-metadata", type: 'link', icon: "business", text: _this.$filter('translate')('views.main.properties'), defaultActive: false, permission: AccessConstants.CATEGORIES_ADMIN });
                    links.push({ sref: "registered-templates", type: 'link', icon: "layers", text: _this.$filter('translate')('views.main.templates'), defaultActive: false, permission: AccessConstants.TEMPLATES_ACCESS });
                    links.push({ sref: "users", type: 'link', icon: "account_box", text: _this.$filter('translate')('views.main.users'), defaultActive: false, permission: AccessConstants.USERS_ACCESS });
                    links.push({ sref: "groups", type: 'link', icon: "group", text: _this.$filter('translate')('views.main.groups'), defaultActive: false, permission: AccessConstants.GROUP_ACCESS });
                    //links.push({sref: "sla-email-templates",type:'link', icon: "email", text: $filter('translate')('views.main.sla-email'), defaultActive: false, permission: AccessConstants.SLA_EMAIL_TEMPLATES_ACCESS});
                    addExtensionLinks(MENU_KEY.ADMIN, links);
                    var menu = ({ type: 'toggle',
                        text: _this.$filter('translate')('views.main.admin'),
                        narrowText: _this.$filter('translate')('views.main.admin-narrow'),
                        expanded: false,
                        links: links,
                    });
                    menu.links = links;
                    menuMap[MENU_KEY.ADMIN] = menu;
                    return menu;
                };
                var buildSideNavMenu = function () {
                    var menu = [];
                    //identify any additional menu items
                    extensionsMenus = _this.AngularModuleExtensionService.getNavigationMenu();
                    menu.push(buildOpsManagerMenu());
                    menu.push(buildFeedManagerMenu());
                    menu.push(buildAdminMenu());
                    buildExtensionsMenu(menu);
                    buildMenuStateMap(menu);
                    toggleSections = _.filter(menu, function (item) {
                        return item.type == 'toggle';
                    });
                    //clear the binding
                    $scope.menu.length = 0;
                    //readd in the values
                    _.each(menu, function (item) {
                        $scope.menu.push(item);
                    });
                };
                $scope.sideNavService = _this.SideNavService;
                /**
                 * The menu
                 * @type {Array}
                 */
                $scope.menu = [];
                /**
                 * The selected menu item
                 * @type {null}
                 */
                $scope.selectedMenuItem = null;
                var selectMenuItem = function ($event, menuItem) {
                    $scope.selectedMenuItem = menuItem;
                    closeSideNavList();
                };
                var expandCollapseSideNavList = function () {
                    if ($scope.collapsed) {
                        expand();
                    }
                    else {
                        collapse();
                    }
                };
                /**
                 * Function to call when selecting an item on the left nav
                 * @type {selectMenuItem}
                 */
                $scope.selectMenuItem = selectMenuItem;
                $scope.collapsed = false;
                $scope.expandCollapseSideNavList = expandCollapseSideNavList;
                $scope.adminTitle = _this.$filter('translate')('views.main.adminTitle');
                $scope.feedManagerTitle = _this.$filter('translate')('views.main.feedManagerTitle');
                $scope.opsManagerTitle = _this.$filter('translate')('views.main.opsManagerTitle');
                $scope.menuTitle = '';
                /**
                  * A map with the moduleName
                  * @type {{}}
                  */
                var menuMap = {};
                var MENU_KEY = { "OPS_MGR": "OPS_MGR", "FEED_MGR": "FEED_MGR", "ADMIN": "ADMIN" };
                var extensionsMenus = {};
                /**
                 * a pointer to the highlighted menu item
                 * @type {null}
                 */
                var currentMenuLink = null;
                /**
                 * A map with the sref,parent menu toggle text
                 * this is used to determine what Accordion group should be open given the current state
                 * @type {{}}
                 */
                var menuStateToMenuToggleMap = {};
                /**
                 * Map of the state (sref) to the menu item
                 * @type {{}}
                 */
                var menuStateMap = {};
                /**
                 * Array of the top level accordion toggle menu items (i.e. Feed Manager, Operations, Admin)
                 * @type {Array}
                 */
                var toggleSections = [];
                /**
                 * the <accordion-menu> html $element
                 * @type {null}
                 */
                var $accordionElement = null;
                function updateMenuText() {
                    var toggleItems = _.filter($scope.menu, function (item) {
                        return item.type == 'toggle';
                    });
                    _.each(toggleItems, function (item) {
                        if (item.origText == undefined) {
                            item.origText = item.text;
                        }
                        item.collapsed = $scope.collapsed;
                        if ($scope.collapsed) {
                            item.text = item.narrowText;
                        }
                        else {
                            item.text = item.origText;
                        }
                    });
                }
                function collapse() {
                    $scope.collapsed = true;
                    // angular.element('md-sidenav > md-content >div:first').css('overflow-','hidden')
                    angular.element('md-sidenav').css('overflow', 'hidden');
                    angular.element('md-sidenav > md-content').css('overflow', 'hidden');
                    angular.element('md-sidenav').addClass('collapsed');
                    updateMenuText();
                }
                function expand() {
                    $scope.collapsed = false;
                    //  angular.element('md-sidenav > md-content >div:first').css('overflow-y','auto')
                    angular.element('md-sidenav').css('overflow', 'auto');
                    angular.element('md-sidenav > md-content').css('overflow', 'auto');
                    angular.element('md-sidenav').removeClass('collapsed');
                    updateMenuText();
                }
                /**
                 * Check if the Side Nav is hidden or not
                 * @returns {*|boolean}
                 */
                /* $mdMedia: any;
                $q: any;*/
                var isSideNavHidden = function () {
                    return ($scope.$mdMedia('gt-md') && _this.SideNavService.isLockOpen);
                };
                var toggleSideNavList = function () {
                    $scope.$q.when(true).then(function () {
                        _this.$mdSidenav('left').toggle();
                    });
                };
                /**
                 * Builds additional menu items
                 * @param rootMenu
                 */
                var buildExtensionsMenu = function (rootMenu) {
                    var additionalKeys = _.keys(extensionsMenus);
                    additionalKeys = _.filter(additionalKeys, function (key) { return MENU_KEY[key] == undefined; });
                    if (additionalKeys.length > 0) {
                        _.each(additionalKeys, function (key) {
                            var menu = extensionsMenus[key];
                            //ensure this is a toggle type
                            menu.type = 'toggle';
                            _.each(menu.links, function (link) {
                                //ensure we set this type to be a child
                                link.type = 'link';
                            });
                            menuMap[key] = menu;
                            rootMenu.push(menu);
                        });
                    }
                };
                var addExtensionLinks = function (menuName, links) {
                    var extensionLinks = extensionsMenus[menuName];
                    if (extensionLinks && extensionLinks.links) {
                        _.each(extensionLinks.links, function (link) {
                            //ensure we set this type to be a child
                            link.type = 'link';
                            links.push(link);
                        });
                    }
                };
                var closeSideNavList = function () {
                    _this.$mdSidenav('left').close();
                };
                var accessDeniedDialog = function (title, content) {
                    _this.$mdDialog.show(_this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title(title)
                        .textContent(content)
                        .ariaLabel(_this.$filter('translate')('views.main.access-denied'))
                        .ok(_this.$filter('translate')('views.main.got-it')));
                };
                function joinArray(mainArr, joiningArr) {
                    _.each(joiningArr, function (item) {
                        mainArr.push(item);
                    });
                    return mainArr;
                }
                var buildMenuStateMap = function (menu) {
                    menuStateToMenuToggleMap = {};
                    menuStateMap = {};
                    _.each(menu, function (menuToggle) {
                        _.each(menuToggle.links, function (item) {
                            menuStateToMenuToggleMap[item.sref] = menuToggle;
                            menuStateMap[item.sref] = item;
                        });
                    });
                };
                function menuToggleItemForModuleName(moduleName) {
                    if (moduleName.indexOf('opsmgr') >= 0) {
                        return menuMap[MENU_KEY.OPS_MGR];
                    }
                    else if (moduleName.indexOf('feedmgr') >= 0 &&
                        moduleName != 'kylo.feedmgr.datasources' &&
                        moduleName != "kylo.feedmgr.domain-types" &&
                        moduleName != 'kylo.feedmgr.templates') {
                        return menuMap[MENU_KEY.FEED_MGR];
                    }
                    else if (moduleName.indexOf('auth') >= 0 ||
                        moduleName == 'kylo.feedmgr.datasources' ||
                        moduleName == "kylo.feedmgr.domain-types" ||
                        moduleName == 'kylo.feedmgr.templates') {
                        return menuMap[MENU_KEY.ADMIN];
                    }
                    else {
                        return null;
                    }
                }
                var onAngularExtensionsInitialized = function () {
                    buildSideNavMenu();
                };
                _this.AngularModuleExtensionService.onInitialized(onAngularExtensionsInitialized);
                if (_this.AngularModuleExtensionService.isInitialized()) {
                    buildSideNavMenu();
                }
                _this.$transitions.onSuccess({}, function (transition) {
                    var currentState = transition.to();
                    var parentMenu = menuStateToMenuToggleMap[currentState.name];
                    var menuLink = menuStateMap[currentState.name];
                    if (menuLink != undefined) {
                        if (currentMenuLink != null && currentMenuLink != menuLink) {
                            currentMenuLink.selected = false;
                        }
                        currentMenuLink = menuLink;
                        currentMenuLink.selected = true;
                    }
                    if ($accordionElement == null) {
                        $accordionElement = $element.find('accordion-menu');
                    }
                    if (parentMenu == undefined && currentState != undefined && currentState.data != undefined) {
                        //attempt to locate the menu based upon the moduleName defined on the state
                        var moduleName = currentState.data.module;
                        if (moduleName != undefined) {
                            var menuToggle = menuToggleItemForModuleName(moduleName);
                            if (menuToggle != null) {
                                parentMenu = menuToggle;
                            }
                        }
                    }
                    if (parentMenu != undefined &&
                        $accordionElement != null &&
                        $accordionElement != undefined) {
                        if (!parentMenu.expanded) {
                            _this.AccordionMenuService.openToggleItem(parentMenu, $accordionElement, false, toggleSections);
                        }
                    }
                });
            }; // end of this.link
        } // close constructor
        directive.factory = function () {
            var sidedirective = function ($mdSidenav, $mdDialog, $rootScope, $transitions, $timeout, SideNavService, AccessControlService, StateService, AccordionMenuService, AngularModuleExtensionService, $filter) {
                return new directive($mdSidenav, $mdDialog, $rootScope, $transitions, $timeout, SideNavService, AccessControlService, StateService, AccordionMenuService, AngularModuleExtensionService, $filter);
            };
            return sidedirective;
        };
        return directive;
    }());
    exports.directive = directive;
    angular.module(module_name_1.moduleName).directive('kyloSideNav', directive.factory());
});
//# sourceMappingURL=side-nav.js.map