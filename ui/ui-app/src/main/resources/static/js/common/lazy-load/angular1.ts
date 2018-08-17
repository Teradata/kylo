import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {LazyLoadComponent} from "./lazy-load.component";

angular.module(moduleName)
    .directive("lazyLoad", downgradeComponent({component: LazyLoadComponent}) as angular.IDirectiveFactory);
