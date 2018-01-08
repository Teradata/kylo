import {FactoryProvider} from "@angular/core";

import "./module"; // ensure module is loaded
import "./module-require";
import {NotificationService} from "./notification.service";

export function notificationServiceFactory(i: any) {
    return i.get("NotificationService");
}

export const notificationServiceProvider: FactoryProvider = {
    provide: NotificationService,
    useFactory: notificationServiceFactory,
    deps: ["$injector"]
};
