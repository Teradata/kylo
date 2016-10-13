/**
 * Used to programmatically open/close the side nav bar
 */
angular.module(MODULE_FEED_MGR).service('SideNavService', function (BroadcastService) {

    var self = this;
    this.isLockOpen = true;

    this.hideSideNav = function(){
        if (self.isLockOpen) {
            self.isLockOpen = false;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }

    }

    this.showSideNav = function () {
        if (!self.isLockOpen) {
            self.isLockOpen = true;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }

    }

});