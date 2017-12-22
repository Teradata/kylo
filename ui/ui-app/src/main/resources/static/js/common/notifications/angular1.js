define(["require", "exports", "@angular/upgrade/static", "angular", "./notification-menu.component"], function (require, exports, static_1, angular, notification_menu_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(require("common/module-name"))
        .directive("notificationMenu", static_1.downgradeComponent({ component: notification_menu_component_1.NotificationMenuComponent }));
});
//# sourceMappingURL=angular1.js.map