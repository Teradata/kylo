import * as angular from 'angular';
import {moduleName} from './module-name';

/**
 * We need to import Angular1 items until they are converted over to ng2
 */

//        import 'kylo-feedmgr';
//      import  'kylo-services';
//      import  'kylo-common';
import {SparkQueryEngine} from './services/spark/spark-query-engine';
import {downgradeComponent, downgradeInjectable} from "@angular/upgrade/static";
import {QueryEngineFactory} from "./wrangler/query-engine-factory.service";
//import {VisualQuerySaveService} from "./services/save.service";
//import {VisualQueryStoreComponent} from "./store/store.component";
//import {TransformDataComponent} from "./transform-data/transform-data.component";
//import {WranglerDataService} from "./transform-data/services/wrangler-data.service";
//import {WranglerTableService} from "./transform-data/services/wrangler-table.service";
//import VisualQueryProfileStatsController from "./transform-data/profile-stats/VisualQueryProfileStats";
//import {VisualQueryPainterService} from "./transform-data/visual-query-table/visual-query-painter.service";
//import {VisualQueryTable} from "./transform-data/visual-query-table/visual-query-table.component";

/*
angular.module(moduleName)
    .service("VisualQueryEngineFactory", QueryEngineFactory)
    .provider("$$wranglerInjector", {
        $get: function () {
            return QueryEngineFactory.$$wranglerInjector;
        }
    });
*/
