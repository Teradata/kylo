/**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
angular.module(MODULE_FEED_MGR).service('SideNavService', function () {

    var self = this;
    this.isLockOpen = true;

    this.hideSideNav = function(){
        self.isLockOpen = false;
    }
    this.showSideNav = function () {
        self.isLockOpen = true;
    }

});