import * as angular from "angular";

import {QueryEngine} from "./query-engine";

const ENGINES: { [name: string]: [string | Function] } = {};
let moduleName = require("feed-mgr/visual-query/module-name");
type QueryEngineRef = [string | Function];

/**
 * Registers the specified query engine with this factory.
 *
 * @param name - the identifier for the query engine
 * @param engine - the reference for constructing an instance of the engine
 */
export function registerQueryEngine(name: string, engine: QueryEngineRef) {
    ENGINES[name] = engine;
}

/**
 * A factory for creating {@code QueryEngine} objects.
 */
export class QueryEngineFactory {

    /**
     * Constructs a {@code QueryEngineFactory}.
     */
    constructor(private $injector: angular.auto.IInjectorService, private $ocLazyLoad: ocLazyLoad) {
    }

    /**
     * Creates a new engine of the specified type.
     *
     * @param name - the type of engine
     * @returns the query engine
     */
    getEngine(name: string): Promise<QueryEngine<any>> {
        let standardName = name.toLowerCase();
        if (!standardName.match(/^[a-z]+$/)) {
            throw new Error("Unsupported query engine: " + name);
        }

        if (ENGINES[standardName]) {
            return Promise.resolve(this.createEngine(ENGINES[standardName]));
        } else {
            return new Promise((resolve, reject) => {
                this.$ocLazyLoad.load("plugin/" + standardName + "/" + standardName + "-query-engine")
                    .then(() => {
                        if (ENGINES[standardName]) {
                            resolve(this.createEngine(ENGINES[standardName]));
                        } else {
                            reject("Unsupported query engine: " + name);
                        }
                    }, reject);
            });
        }
    }

    /**
     * Instantiates the specified query engine.
     *
     * @param ref - a query engine reference
     * @returns the query engine
     */
    private createEngine(ref: QueryEngineRef): QueryEngine<any> {
        return this.$injector.instantiate(ref as any) as QueryEngine<any>;
    }
}

angular.module(moduleName).service("VisualQueryEngineFactory", ["$injector", "$ocLazyLoad", QueryEngineFactory]);
