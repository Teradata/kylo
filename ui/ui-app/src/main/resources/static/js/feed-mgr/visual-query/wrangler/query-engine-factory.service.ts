import {Compiler, Injectable, Injector, NgModuleRef, ReflectiveInjector, Type} from "@angular/core";
import "rxjs/add/observable/fromPromise";
import "rxjs/add/operator/do";
import "rxjs/add/operator/map";
import "rxjs/add/operator/mergeMap";
import "rxjs/add/operator/publishLast";
import "rxjs/add/operator/toPromise";
import {Observable} from "rxjs/Observable";

import {WranglerModule} from "./core/wrangler.module";
import {QueryEngine} from "./query-engine";

declare const SystemJS: any;

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
    public static $$wranglerInjector: Injector = null;

    /**
     * Wrangler module.
     */
    private wrangler: Observable<NgModuleRef<WranglerModule>>;

    /**
     * Constructs a {@code QueryEngineFactory}.
     */
    constructor(private $$angularInjector: Injector) {
        this.wrangler = Observable.fromPromise($$angularInjector.get(Compiler).compileModuleAndAllComponentsAsync(WranglerModule))
            .map(factories => factories.ngModuleFactory.create($$angularInjector))
            .do(wrangler => {
                QueryEngineFactory.$$wranglerInjector = wrangler.injector;
                console.log("Set the wrangler.injector", QueryEngineFactory.$$wranglerInjector);
            })
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
                if (ENGINES[standardName]) {
                    return Observable.of(this.createEngine(ENGINES[standardName]));
                } else {
                    return this.loadPluginEngine(standardName);
                }
            })
            .toPromise();
    }

    private loadPluginEngine(standardName: string): Observable<QueryEngine<any>> {
        return Observable.fromPromise(SystemJS.import("plugin/" + standardName + "/" + standardName + "-query-engine"))
            .map(() => {
                if (ENGINES[standardName]) {
                    return this.createEngine(ENGINES[standardName]);
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
    private createEngine(ref: QueryEngineRef): QueryEngine<any> {
        const $injector = ReflectiveInjector.resolveAndCreate([{provide: "QueryEngine", useClass: ref as Type<any>}], QueryEngineFactory.$$wranglerInjector);
        return $injector.get("QueryEngine") as QueryEngine<any>;
    }
}

