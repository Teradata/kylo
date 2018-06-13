import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {RouterBreadcrumbsComponent} from "./ui-router-breadcrumbs.component";

angular.module(moduleName)
    .directive("uiRouterBreadcrumbs", downgradeComponent({component: RouterBreadcrumbsComponent}) as angular.IDirectiveFactory);
