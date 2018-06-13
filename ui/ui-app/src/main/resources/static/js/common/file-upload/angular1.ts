import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {UploadFileComponent} from "./file-upload.component";

angular.module(moduleName)
    .directive("uploadFile", downgradeComponent({component: UploadFileComponent}) as angular.IDirectiveFactory);
