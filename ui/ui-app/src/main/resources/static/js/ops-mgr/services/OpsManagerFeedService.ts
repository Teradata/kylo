import * as angular from "angular";
import {moduleName} from "../module-name";
import * as moment from "moment";
import {OperationsFeedUtil} from "../../services/operations-feed-util";
import {OpsManagerFeedService} from "../../services/ops-manager-feed.service";
import {downgradeInjectable} from "@angular/upgrade/static";
import {TranslateService} from "@ngx-translate/core";

angular.module(moduleName)
        .service('OpsManagerFeedService',downgradeInjectable(OpsManagerFeedService));
