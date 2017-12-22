import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {NotificationMenuComponent} from "./notification-menu.component";

angular.module(require("common/module-name"))
    .directive("notificationMenu", downgradeComponent({component: NotificationMenuComponent}) as angular.IDirectiveFactory);
