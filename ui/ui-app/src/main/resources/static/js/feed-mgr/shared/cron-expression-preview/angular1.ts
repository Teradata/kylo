import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../../module-name";
import {CronExpressionPreview} from "./cron-expression-preview.component";

angular.module(moduleName)
    .directive("cronExpressionPreview", downgradeComponent({component: CronExpressionPreview}) as angular.IDirectiveFactory);
