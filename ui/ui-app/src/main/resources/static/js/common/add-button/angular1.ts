import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {AddButtonComponent} from "./add-button.component";

angular.module(moduleName)
    .directive("addButton", downgradeComponent({component: AddButtonComponent}) as angular.IDirectiveFactory);
