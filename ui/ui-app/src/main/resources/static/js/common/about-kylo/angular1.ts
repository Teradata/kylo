import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import AboutKyloDialogController from "./AboutKyloService";

angular.module(moduleName)
    .directive("aboutKyloDialogController", downgradeComponent({component: AboutKyloDialogController}) as angular.IDirectiveFactory);
