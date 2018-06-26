import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {IconPickerDialog} from "./icon-picker-dialog.component";

angular.module(moduleName)
    .directive("iconPickerDialog", downgradeComponent({component: IconPickerDialog}) as angular.IDirectiveFactory);
