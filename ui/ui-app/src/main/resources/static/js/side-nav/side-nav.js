define(['angular','side-nav/module-name'], function (angular,moduleName) {

    var directive = function ( $mdSidenav, $mdDialog,$rootScope,$transitions,$timeout, SideNavService, AccessControlService, StateService) {
        return {
            restrict: "E",
            scope:{},
            templateUrl: 'js/side-nav/side-nav.html',
            link: function ($scope) {
                $scope.sideNavService = SideNavService;

                $scope.menuTitle = '';

                var MODULES = {"FEED_MGR":{name:"kylo.feedmgr",icon:"business_center",text:"Feed Manager"},"OPS_MGR":{name:"kylo.opsmgr",icon:"dashboard",text:"Operations Manager",link:"dashboard"}};

                var defaultModule = MODULES.OPS_MGR;



                /**
                 * The selected menu item
                 * @type {null}
                 */
                $scope.selectedMenuItem = null;
                /**
                 * Function to call when selecting an item on the left nav
                 * @type {selectMenuItem}
                 */
                $scope.selectMenuItem = selectMenuItem;



                $scope.collapsed = false;

                $scope.expandCollapseSideNavList = expandCollapseSideNavList;

                $scope.adminTitle = "Admin";
                $scope.feedManagerTitle = "Feed Manager";
                $scope.opsManagerTitle = "Operations";





                /**
                 * Build the Feed Manager Left Nav
                 * @param allowed
                 */
                function buildFeedManagerMenu() {
                    var menu = ({type:'toggle', text: "Feed Manager",narrowText:'',expanded:true});
                    var links = [];
                    links.push({sref: "feeds",type:'link', icon: "linear_scale", text: "Feeds", defaultActive: false, fullscreen: false, permission: AccessControlService.FEEDS_ACCESS});
                    links.push({sref: "categories",type:'link', icon: "folder_special", text: "Categories", defaultActive: false, fullscreen: false, permission: AccessControlService.CATEGORIES_ACCESS});
                    links.push({sref: "tables",type:'link', icon: "grid_on", text: "Tables", defaultActive: false, fullscreen: false, permission: AccessControlService.FEED_MANAGER_ACCESS});
                    links.push({sref: "service-level-agreements",type:'link', icon: "beenhere", text: "SLA", defaultActive: false, fullscreen: false, permission: AccessControlService.FEEDS_ACCESS});
                    links.push({sref: "visual-query",type:'link', icon: "transform", text: "Visual Query", defaultActive: false, fullscreen: true, permission: AccessControlService.FEED_MANAGER_ACCESS});
                    menu.links = links;
                 return menu;

                }

                /**
                 * Build the Ops Manager Left Nav
                 * @param allowed
                 */
                function buildOpsManagerMenu() {
                       var menu = ({type:'toggle', text: "Operations",narrowText:'',expanded:false});
                       var links = [];
                       links.push({sref: "dashboard",type:'link', icon: "dashboard", text: "Dashboard", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       links.push({sref: "service-health",type:'link', icon: "vector_triangle", text: "Services", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       links.push({sref: "jobs",type:'link', icon: "settings", text: "Jobs", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       links.push({sref: "alerts", icon: "notifications", text: "Alerts", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       links.push({sref: "scheduler",type:'link', icon: "today", text: "SLA Schedule", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       links.push({sref: "charts",type:'link', icon: "insert_chart", text: "Charts", defaultActive: false, permission: AccessControlService.OPERATIONS_MANAGER_ACCESS});
                       menu.links = links;
                    return menu;
                }

                /**
                 * Build the Admin Menu
                 * @param allowed
                 */
                function buildAdminMenu(allowed) {

                    var menu = ({type:'toggle', text: "Admin",narrowText:'',expanded:false});
                    var links = [];
                    links.push({sref: "business-metadata", type:'link', icon: "business", text: "Properties", defaultActive: false, permission: AccessControlService.CATEGORIES_ADMIN});
                    links.push({sref: "registered-templates",type:'link', icon: "layers", text: "Templates", defaultActive: false, permission: AccessControlService.TEMPLATES_ACCESS});
                    links.push({sref: "users",type:'link', icon: "account_box", text: "Users", defaultActive: false, permission: AccessControlService.USERS_ACCESS});
                    links.push({sref: "groups",type:'link', icon: "group", text: "Groups", defaultActive: false, permission: AccessControlService.GROUP_ACCESS});
                    menu.links = links;
                  return menu
                }

                /**
                 * Check if the Side Nav is hidden or not
                 * @returns {*|boolean}
                 */
                function isSideNavHidden() {
                    return ($mdMedia('gt-md') && SideNavService.isLockOpen)
                };

                function toggleSideNavList() {
                    $q.when(true).then(function () {
                        $mdSidenav('left').toggle();
                    });
                }

                function updateMenuText() {
                    var toggleItems = _.filter($scope.menu,function(item){
                        return item.type == 'toggle';
                    });
                    _.each(toggleItems,function(item){
                        if(item.origText == undefined) {
                            item.origText = item.text;
                        }
                        item.collapsed = $scope.collapsed;
                        if($scope.collapsed){
                            item.text = item.narrowText;

                        }
                        else {
                            item.text = item.origText;
                        }
                    })
                }

                function collapse(){
                    $scope.collapsed = true;
                   // angular.element('md-sidenav > md-content >div:first').css('overflow-','hidden')
                    angular.element('md-sidenav').css('overflow','hidden')
                    angular.element('md-sidenav > md-content').css('overflow','hidden')
                    angular.element('md-sidenav').addClass('collapsed');
                    updateMenuText();
                }

                function expand(){
                    $scope.collapsed = false;
                  //  angular.element('md-sidenav > md-content >div:first').css('overflow-y','auto')
                    angular.element('md-sidenav').css('overflow','auto')
                    angular.element('md-sidenav > md-content').css('overflow','auto')
                    angular.element('md-sidenav').removeClass('collapsed');
                    updateMenuText();
                }

                function expandCollapseSideNavList() {
                    if($scope.collapsed){
                        expand();
                    }
                    else {
                        collapse();
                    }
                }

                function closeSideNavList() {
                    $mdSidenav('left').close();
                }

                function selectMenuItem($event, menuItem) {
                    $scope.selectedMenuItem = menuItem;
                    closeSideNavList();
                }

                function accessDeniedDialog(title,content){
                    $mdDialog.show(
                        $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title(title)
                            .textContent(content)
                            .ariaLabel('Access Deined')
                            .ok('Got it!')
                    );
                }

                function joinArray(mainArr,joiningArr){
                    _.each(joiningArr,function(item){
                        mainArr.push(item);
                    })
                    return mainArr;
                }

                function buildSideNavMenu() {
                    var menu = [];
                    menu.push(buildOpsManagerMenu());
                    menu.push(buildFeedManagerMenu());
                    menu.push(buildAdminMenu());
                    $scope.menu = menu;
                }
                buildSideNavMenu();


/*
                $transitions.onSuccess({},function(transition){
                    currentState = transition.to();
                    console.log('CURRENT STATE ',currentState,transition)
                    var currentModule = null;
                    if(currentState.data != undefined && currentState.data.module !== undefined && currentState.data.module.indexOf(MODULES.FEED_MGR.name) >=0) {
                        currentModule = MODULES.FEED_MGR;
                    }
                    else if(currentState.data != undefined && currentState.data.module !== undefined && currentState.data.module.indexOf(MODULES.OPS_MGR.name) >=0) {
                        currentModule = MODULES.OPS_MGR;
                    }
                    if(sideNavModule == null && currentModule == null){
                        //set the default module
                        currentModule = defaultModule;
                    }

                    if(sideNavModule == null || (currentModule != null &&  sideNavModule != currentModule)) {
                        sideNavModule = currentModule.name;
                        var otherModule = MODULES.FEED_MGR.name == currentModule.name ? MODULES.OPS_MGR : MODULES.FEED_MGR;
                        //change happened... rebuild nav

                            buildSideNavMenu();
                           $scope.otherModule = otherModule;
                           $timeout(function(){
                               $scope.showMenu = true;
                               if(!$scope.collapsed) {
                                   angular.element('md-sidenav').css('overflow', 'auto')
                                   angular.element('md-sidenav > md-content').css('overflow', 'auto')
                               }
                           },500)

                    }



                });
                */

            }
        }
    };

    angular.module(moduleName).directive('kyloSideNav', ['$mdSidenav','$mdDialog','$rootScope','$transitions','$timeout','SideNavService','AccessControlService','StateService', directive]);
});
