import * as angular from "angular";
import { downgradeInjectable } from '@angular/upgrade/static';
import {PreviewDatasetCollectionService} from "../feed-mgr/catalog/api/services/preview-dataset-collection.service";
import {PreviewDataSet} from "../feed-mgr/catalog/datasource/preview-schema/model/preview-data-set";
import {moduleName} from './module-name';
angular.module(moduleName)
    .service("PreviewDatasetCollectionService", downgradeInjectable(PreviewDatasetCollectionService));
