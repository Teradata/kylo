define(["require", "exports", "@angular/core", "angular", "rxjs/Observable", "./core/wrangler.module", "rxjs/add/observable/fromPromise", "rxjs/add/operator/do", "rxjs/add/operator/map", "rxjs/add/operator/mergeMap", "rxjs/add/operator/publishLast", "rxjs/add/operator/toPromise"], function (require, exports, core_1, angular, Observable_1, wrangler_module_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ENGINES = {};
    /**
     * Angular 2 injector used by the Wrangler module.
     */
    var $$wranglerInjector = null;
    /**
     * Registers the specified query engine with this factory.
     *
     * @param name - the identifier for the query engine
     * @param engine - the reference for constructing an instance of the engine
     */
    function registerQueryEngine(name, engine) {
        ENGINES[name] = engine;
    }
    exports.registerQueryEngine = registerQueryEngine;
    /**
     * A factory for creating {@code QueryEngine} objects.
     */
    var QueryEngineFactory = /** @class */ (function () {
        /**
         * Constructs a {@code QueryEngineFactory}.
         */
        function QueryEngineFactory($$angularInjector, $ocLazyLoad) {
            this.$ocLazyLoad = $ocLazyLoad;
            this.wrangler = Observable_1.Observable.fromPromise($$angularInjector.get(core_1.Compiler).compileModuleAndAllComponentsAsync(wrangler_module_1.WranglerModule))
                .map(function (factories) { return factories.ngModuleFactory.create($$angularInjector); })
                .do(function (wrangler) { return $$wranglerInjector = wrangler.injector; })
                .publishLast()
                .refCount();
        }
        /**
         * Creates a new engine of the specified type.
         *
         * @param name - the type of engine
         * @returns the query engine
         */
        QueryEngineFactory.prototype.getEngine = function (name) {
            var _this = this;
            // Get engine short name
            var standardName = name.toLowerCase();
            if (!standardName.match(/^[a-z]+$/)) {
                throw new Error("Unsupported query engine: " + name);
            }
            // Load the engine
            return this.wrangler
                .mergeMap(function (module) {
                var $injector = module.injector.get("$injector");
                if (ENGINES[standardName]) {
                    return Observable_1.Observable.of(_this.createEngine(ENGINES[standardName], $injector));
                }
                else {
                    return Observable_1.Observable.fromPromise(_this.$ocLazyLoad.load("plugin/" + standardName + "/" + standardName + "-query-engine"))
                        .map(function () {
                        if (ENGINES[standardName]) {
                            return _this.createEngine(ENGINES[standardName], $injector);
                        }
                        else {
                            throw "Unsupported query engine: " + name;
                        }
                    });
                }
            })
                .toPromise();
        };
        /**
         * Instantiates the specified query engine.
         *
         * @param ref - a query engine reference
         * @param $injector - Angular 1 injector
         * @returns the query engine
         */
        QueryEngineFactory.prototype.createEngine = function (ref, $injector) {
            return $injector.instantiate(ref);
        };
        QueryEngineFactory.$inject = ["$$angularInjector", "$ocLazyLoad"];
        return QueryEngineFactory;
    }());
    exports.QueryEngineFactory = QueryEngineFactory;
    angular.module(require("feed-mgr/visual-query/module-name"))
        .service("VisualQueryEngineFactory", QueryEngineFactory)
        .provider("$$wranglerInjector", {
        $get: function () {
            return $$wranglerInjector;
        }
    });
});
//# sourceMappingURL=query-engine-factory.service.js.map