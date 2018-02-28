define(["require", "exports", "@angular/upgrade/static", "angular", "../module-name", "./notification-menu.component"], function (require, exports, static_1, angular, module_name_1, notification_menu_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    angular.module(module_name_1.moduleName)
        .directive("notificationMenu", static_1.downgradeComponent({ component: notification_menu_component_1.NotificationMenuComponent }));
});
//# sourceMappingURL=angular1.js.map