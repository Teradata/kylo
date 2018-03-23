import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {NotificationMenuComponent} from "./notification-menu.component";

angular.module(moduleName)
    .directive("notificationMenu", downgradeComponent({component: NotificationMenuComponent}) as angular.IDirectiveFactory);
