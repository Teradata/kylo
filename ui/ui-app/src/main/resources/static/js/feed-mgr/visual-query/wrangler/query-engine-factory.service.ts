import {Compiler, Injectable, Injector, NgModuleRef, Type} from "@angular/core";
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
import {moduleName} from "../module-name";

/**
 * Map of wrangler engine name to implementation.
 */
type QueryEngineRef = Type<any> | string;
const ENGINES: { [name: string]: QueryEngineRef } = {};



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
@Injectable()
export class QueryEngineFactory {

    /**
     * Angular 2 injector used by the Wrangler module.
     */
    public static $$wranglerInjector: any = null;

    /**
     * Wrangler module.
     */
    private wrangler: Observable<NgModuleRef<WranglerModule>>;

//    static readonly $inject: string[] = ["$$angularInjector", "$ocLazyLoad"];


    private $ocLazyLoad: oc.ILazyLoad;

    /**
     * Constructs a {@code QueryEngineFactory}.
     */
    constructor(private $$angularInjector: Injector) {
        this.$ocLazyLoad = $$angularInjector.get("$ocLazyLoad");
        this.wrangler = Observable.fromPromise($$angularInjector.get(Compiler).compileModuleAndAllComponentsAsync(WranglerModule))
            .map(factories => factories.ngModuleFactory.create($$angularInjector))
            .do(wrangler => {
                QueryEngineFactory.$$wranglerInjector = wrangler.injector;
                console.log("Set the wrangler.injector",  QueryEngineFactory.$$wranglerInjector);
            } )
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
                const $injector = angular.element(document.body).injector();
                //const $injector = module.injector.get("$injector");
                if (ENGINES[standardName]) {
                    return Observable.of(this.createEngine(ENGINES[standardName], $injector));
                } else {
                    return this.loadPluginEngine(module,standardName);
                }
            })
            .toPromise();
    }

    private loadPluginEngine(module:any,standardName:string) :Observable<QueryEngine<any>> {
        const $injector = module.injector.get("$injector");
        return Observable.fromPromise(this.$ocLazyLoad.load("plugin/" + standardName + "/" + standardName + "-query-engine"))
            .map(() => {
                if (ENGINES[standardName]) {
                    return this.createEngine(ENGINES[standardName], $injector);
                } else {
                    throw "Unsupported query engine: " + name;
                }
            });
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

