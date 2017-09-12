import {QueryEngine} from "./query-engine"
import {SparkQueryEngine} from "./spark/spark-query-engine";
import {DatasourcesServiceStatic} from "../../services/DatasourcesService.typings";
import {TeradataQueryEngine} from "./teradata/teradata-query-engine";

declare const angular: angular.IAngularStatic;

let moduleName = require("feed-mgr/visual-query/module-name");

/**
 * A factory for creating {@code QueryEngine} objects.
 */
export class QueryEngineFactory {

    /**
     * Constructs a {@code QueryEngineFactory}.
     */
    constructor(private $http: angular.IHttpService, private $mdDialog: angular.material.IDialogService, private $timeout: angular.ITimeoutService,
                private DatasourcesService: DatasourcesServiceStatic.DatasourcesService, private HiveService: any, private RestUrlService: any, private uiGridConstants: any,
                private VisualQueryService: any) {
    }

    /**
     * Creates a new engine of the specified type.
     *
     * @param name - the type of engine
     * @returns the query engine
     */
    getEngine(name: string): QueryEngine<any> {
        let standardName = name.toLowerCase();
        if (standardName === "spark") {
            return new SparkQueryEngine(this.$http, this.$mdDialog, this.$timeout, this.DatasourcesService, this.HiveService, this.RestUrlService, this.uiGridConstants, this.VisualQueryService);
        } else if (standardName === "teradata") {
            return new TeradataQueryEngine(this.$http, this.$mdDialog, this.DatasourcesService, this.RestUrlService, this.uiGridConstants, this.VisualQueryService);
        } else {
            throw new Error("Unsupported query engine: " + name);
        }
    }
}

angular.module(moduleName).service("VisualQueryEngineFactory", ["$http", "$mdDialog", "$timeout", "DatasourcesService", "HiveService", "RestUrlService", "uiGridConstants",
    "VisualQueryService", QueryEngineFactory]);
