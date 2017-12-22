define(["require", "exports", "./notification.service", "./module", "./module-require"], function (require, exports, notification_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function notificationServiceFactory(i) {
        return i.get("NotificationService");
    }
    exports.notificationServiceFactory = notificationServiceFactory;
    exports.notificationServiceProvider = {
        provide: notification_service_1.NotificationService,
        useFactory: notificationServiceFactory,
        deps: ["$injector"]
    };
});
//# sourceMappingURL=angular2.js.map