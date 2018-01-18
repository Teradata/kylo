import {Compiler, Injector, NgModuleRef, Type} from "@angular/core";
import * as angular from "angular";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/operator/do";
import "rxjs/add/operator/map";
import "rxjs/add/operator/mergeMap";
import "rxjs/add/operator/publishLast";
import "rxjs/add/operator/toPromise";
import {Observable} from "rxjs/Observable";

import {WranglerModule} from "./core/wrangler.module";
import {QueryEngine} from "./query-engine";

/**
 * Map of wrangler engine name to implementation.
 */
type QueryEngineRef = Type<any> | string;
const ENGINES: { [name: string]: QueryEngineRef } = {};

/**
 * Angular 2 injector used by the Wrangler module.
 */
let $$wranglerInjector: any = null;

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
     * Wrangler module.
     */
    private wrangler: Observable<NgModuleRef<WranglerModule>>;

    static readonly $inject: string[] = ["$$angularInjector", "$ocLazyLoad"];

    /**
     * Constructs a {@code QueryEngineFactory}.
     */
    constructor($$angularInjector: Injector, private $ocLazyLoad: ocLazyLoad) {
        this.wrangler = Observable.fromPromise($$angularInjector.get(Compiler).compileModuleAndAllComponentsAsync(WranglerModule))
            .map(factories => factories.ngModuleFactory.create($$angularInjector))
            .do(wrangler => $$wranglerInjector = wrangler.injector)
            .publishLast()
            .refCount();
    }

    /**
     * Creates a new engine of the specified type.
     *
     * @param name - the type of engine
     * @returns the query engine
     */
    getEngine(name: string): Promise<QueryEngine<any>> {
        // Get engine short name
        let standardName = name.toLowerCase();
        if (!standardName.match(/^[a-z]+$/)) {
            throw new Error("Unsupported query engine: " + name);
        }

        // Load the engine
        return this.wrangler
            .mergeMap(module => {
                const $injector = module.injector.get("$injector");
                if (ENGINES[standardName]) {
                    return Observable.of(this.createEngine(ENGINES[standardName], $injector));
                } else {
                    return Observable.fromPromise(this.$ocLazyLoad.load("plugin/" + standardName + "/" + standardName + "-query-engine"))
                        .map(() => {
                            if (ENGINES[standardName]) {
                                return this.createEngine(ENGINES[standardName], $injector);
                            } else {
                                throw "Unsupported query engine: " + name;
                            }
                        });
                }
            })
            .toPromise();
    }

    /**
     * Instantiates the specified query engine.
     *
     * @param ref - a query engine reference
     * @param $injector - Angular 1 injector
     * @returns the query engine
     */
    private createEngine(ref: QueryEngineRef, $injector: angular.auto.IInjectorService): QueryEngine<any> {
        return $injector.instantiate(ref as any) as QueryEngine<any>;
    }
}

angular.module(require("feed-mgr/visual-query/module-name"))
    .service("VisualQueryEngineFactory", QueryEngineFactory)
    .provider("$$wranglerInjector", {
        $get: function () {
            return $$wranglerInjector;
        }
    });
