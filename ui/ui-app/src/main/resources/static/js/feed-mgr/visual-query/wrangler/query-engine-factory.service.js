define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ENGINES = {};
    var moduleName = require("feed-mgr/visual-query/module-name");
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
        function QueryEngineFactory($injector, $ocLazyLoad) {
            this.$injector = $injector;
            this.$ocLazyLoad = $ocLazyLoad;
        }
        /**
         * Creates a new engine of the specified type.
         *
         * @param name - the type of engine
         * @returns the query engine
         */
        QueryEngineFactory.prototype.getEngine = function (name) {
            var _this = this;
            var standardName = name.toLowerCase();
            if (!standardName.match(/^[a-z]+$/)) {
                throw new Error("Unsupported query engine: " + name);
            }
            if (ENGINES[standardName]) {
                return Promise.resolve(this.createEngine(ENGINES[standardName]));
            }
            else {
                return new Promise(function (resolve, reject) {
                    _this.$ocLazyLoad.load("plugin/" + standardName + "/" + standardName + "-query-engine")
                        .then(function () {
                        if (ENGINES[standardName]) {
                            resolve(_this.createEngine(ENGINES[standardName]));
                        }
                        else {
                            reject("Unsupported query engine: " + name);
                        }
                    }, reject);
                });
            }
        };
        /**
         * Instantiates the specified query engine.
         *
         * @param ref - a query engine reference
         * @returns the query engine
         */
        QueryEngineFactory.prototype.createEngine = function (ref) {
            return this.$injector.instantiate(ref);
        };
        return QueryEngineFactory;
    }());
    exports.QueryEngineFactory = QueryEngineFactory;
    angular.module(moduleName).service("VisualQueryEngineFactory", ["$injector", "$ocLazyLoad", QueryEngineFactory]);
});
//# sourceMappingURL=query-engine-factory.service.js.map