/**
 * Controller for the Main App index.html
 */
(function () {

    var controller = function($scope,$http,$location, $window, $mdSidenav, $mdMedia, $mdBottomSheet, $log, $q, $element,$rootScope, RestUrlService,StateService,ElasticSearchService,SideNavService,ConfigurationService){

        var self = this;
        self.toggleSideNavList   = toggleSideNavList;
        self.menu = [];
        self.selectedMenuItem = null;
        self.selectMenuItem = selectMenuItem;
        self.currentState = null;
        self.topNavMenu = [];

        self.sideNavOpen = SideNavService.isLockOpen;
        self.sideNavService = SideNavService;

        this.searchQuery = '';

        this.search = function(){
            ElasticSearchService.searchQuery = this.searchQuery;
            if(self.currentState.name != 'search') {
                StateService.navigateToSearch();
            }
        }

        this.onSearchKeypress = function($event) {
            if ($event.which === 13) {
                self.search();
            }
        }



        function buildSideNavMenu() {
           var menu = [];
             menu.push({sref:"feeds",icon:"link",text:"Feeds",defaultActive:false,fullscreen:false});
            menu.push({sref:"categories",icon:"star",text:"Categories",defaultActive:false,fullscreen:false});
            menu.push({sref:"tables",icon:"layers",text:"Tables",defaultActive:false,fullscreen:false});
            menu.push({sref:"visual-query",icon:"border_color",text:"Visual Query",defaultActive:false, fullscreen:true});
            self.selectedMenuItem = menu[0];
            self.menu = menu;
        }

     this.gotoOperationsManager = function(){
         if(self.opsManagerUrl == undefined) {
             $http.get(ConfigurationService.MODULE_URLS).then(function(response){
               self.opsManagerUrl = response.data.opsMgr;
                 window.location.href=window.location.origin+self.opsManagerUrl;
             });
         }
       else {
             window.location.href=window.location.origin+self.opsManagerUrl;
         }
     }

        this.isSideNavHidden = function(){
           return  ($mdMedia('gt-md') && SideNavService.isLockOpen)
        }


        function buildAdminMenu(){
            var menu = [];
            menu.push({sref:"registered-templates",icon:"folder_special",text:"Templates",defaultActive:false});
            self.adminMenu = menu;
        }

        function toggleSideNavList() {
            // var pending = $mdBottomSheet.hide() || $q.when(true);
            $q.when(true).then(function(){
                $mdSidenav('left').toggle();
            });
        }

        function closeSideNavList() {
                $mdSidenav('left').close();
        }

        function selectMenuItem($event,menuItem) {
            self.selectedMenuItem = menuItem;
            closeSideNavList();
        }

        buildSideNavMenu();
        buildAdminMenu();


        $rootScope.$on('$stateChangeSuccess',function(event,toState){
            self.currentState = toState;
            if(self.currentState.name != 'search'){
                self.searchQuery = '';
            }
        });
    };

    angular.module(MODULE_FEED_MGR).controller('MainController',controller);



}());