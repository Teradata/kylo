/**
 * undefined
 * @version v3.1.8
 * @link undefined
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('angular'), require('@angular/core'), require('@angular/upgrade/static'), require('@uirouter/core'), require('@uirouter/angular'), require('@uirouter/angularjs'), require('@uirouter/rx')) :
	typeof define === 'function' && define.amd ? define(['exports', 'angular', '@angular/core', '@angular/upgrade/static', '@uirouter/core', '@uirouter/angular', '@uirouter/angularjs', '@uirouter/rx'], factory) :
	(factory((global['@uirouter/angular-hybrid'] = global['@uirouter/angular-hybrid'] || {}),global.angular,global.ng.core,global.ng.upgrade.static,global['@uirouter/core'],global['@uirouter/angular'],global['@uirouter/angularjs'],global['@uirouter/rx']));
}(this, (function (exports,angular,_angular_core,_angular_upgrade_static,_uirouter_core,_uirouter_angular,_uirouter_angularjs,_uirouter_rx) { 'use strict';

/**
 * Create a ng1 module for the ng1 half of the hybrid application to depend on.
 *
 * Example:
 * const myApp = angular.module('myApp', ['ui.router.upgrade']);
 */
var upgradeModule = angular.module('ui.router.upgrade', ['ui.router']);
function objectFactory() { return {}; }
/**
 * UIViewNgUpgrade is a component bridge from ng1 ui-view to ng2 ui-view
 *
 * When a ui-router for ng1 is registering a state it checks if a view's
 * `component:` is an ng2 Component class. If so, it creates a special ng1 template
 * which references this component, i.e., <ui-view-ng-upgrade></ui-view-ng-upgrade>
 *
 * See that code by searching ng1-to-ng2 source for: "$stateProvider.decorator"
 *
 * ---
 *
 * ng1-to-ng2 component bridge process:
 *
 * 1)
 * When an ng1 template creates a ui-view which is targeted by a ng2 Component,
 *
 * ```
 * <a ui-sref="foo">Go to foo</a>
 * <div ui-view> <!-- ui-view created in ng1 template -->
 * </div> <!-- targeted with { component: Ng2RoutedComponent } -->
 * ```
 *
 * the state decorator spits out a custom template.  That template loads this
 * ng2 Component adapter as a downgraded-to-ng1 directive.
 *
 * ```
 * <a ui-sref="foo">Go to foo</a>
 * <div ui-view> <!-- decorated template references the downgraded component -->
 *   <ui-view-ng-upgrade> <!-- downgraded adapter component -->
 *   </ui-view-ng-upgrade>
 * </div>
 * ```
 *
 * This downgraded ng2 Component then creates a child UIView (ng2 component)
 *
 * ```
 * <a ui-sref="foo">Go to foo</a>
 * <div ui-view> <!-- custom template references the downgraded component -->
 *   <ui-view-ng-upgrade> <!-- ng2 component adapter downgraded to ng1-->
 *     <ui-view> <!-- pure ng2 ui-view -->
 *      </ui-view>
 *   </ui-view-ng-upgrade>
 * </div>
 * ```
 *
 * which in turn is filled with the routed ng2 component.
 *
 * ```
 * <a ui-sref="foo">Go to foo</a>
 * <div ui-view> <!-- ng1 ui-view -->
 *   <ui-view-ng-upgrade> <!-- ng2 component adapter (downgraded to ng1)-->
 *     <ui-view> <!-- pure ng2 ui-view -->
 *       <ng2-routed-component> <!-- ng2 component hosted in ng2 ui-view -->
 *         <h1>ng2 routed component contents</h1>
 *       </ng2-routed-component>
 *     </ui-view>
 *   </ui-view-ng-upgrade>
 * </div>
 * ```
 *
 * This adapter exposes exposes the parent view context (ParentUIViewInject)
 * as an ng2 DI Provider, which the nested ng2 UIView requires.
 *
 * It gets the ParentUIViewContext information (from the parent ng1 ui-view) by walking
 * up the DOM and grabbing the .data('$uiView') which the ng1 ui-view directive exposes.
 */
var UIViewNgUpgrade = /** @class */ (function () {
    function UIViewNgUpgrade(ref, parent, registry // access the root state
    ) {
        // From the ui-view-ng-upgrade component's element ref, walk up the DOM two elements...
        // There will first be an ng1 ui-view which hosts this element, and then that ui-view's parent element.
        // That (parent) element has access to the proper "parent viewcontext"
        // The ng2 ui-view component is inside this ui-view-ng-upgrade directive, which is inside the ng1 "host" ui-view.
        // Both ui-views share the same "view context" information (the view's fqn and created-by-state context information)
        var ng1elem = angular.element(ref.nativeElement).parent().parent();
        // Expose getters on PARENT_INJECT for context (creation state) and fqn (view address)
        // These will be used by further nested UIView
        Object.defineProperty(parent, "context", {
            get: function () {
                var data = ng1elem['inheritedData']('$uiView');
                return (data && data.$cfg) ? data.$cfg.viewDecl.$context : registry.root();
            },
            enumerable: true
        });
        Object.defineProperty(parent, "fqn", {
            get: function () {
                var data = ng1elem['inheritedData']('$uiView');
                return (data && data.$uiView) ? data.$uiView.fqn : null;
            },
            enumerable: true
        });
    }
    UIViewNgUpgrade.decorators = [
        { type: _angular_core.Component, args: [{
                    selector: 'ui-view-ng-upgrade',
                    template: "<ui-view [name]=\"name\"></ui-view>",
                    // provide a blank object as PARENT_INJECT.
                    // The component will add property getters when it is constructed.
                    viewProviders: [{ provide: _uirouter_angular.UIView.PARENT_INJECT, useFactory: objectFactory }],
                },] },
    ];
    /** @nocollapse */
    UIViewNgUpgrade.ctorParameters = function () { return [
        { type: _angular_core.ElementRef, },
        { type: undefined, decorators: [{ type: _angular_core.Inject, args: [_uirouter_angular.UIView.PARENT_INJECT,] },] },
        { type: _uirouter_core.StateRegistry, },
    ]; };
    UIViewNgUpgrade.propDecorators = {
        'name': [{ type: _angular_core.Input },],
    };
    return UIViewNgUpgrade;
}());
/**********************************
 * Ng2 @NgModule and bootstrap code
 **********************************/
// Register the ng1 DI '$uiRouter' object as an ng2 Provider.
function uiRouterUpgradeFactory(router, injector) {
    var modules = injector.get(_uirouter_angular.UIROUTER_MODULE_TOKEN, []);
    modules.forEach(function (module) { return _uirouter_angular.applyModuleConfig(router, injector, module); });
    return router;
}
function getUIRouter($injector) {
    return $injector.get('$uiRouter');
}
function getParentUIViewInject(r) {
    return { fqn: null, context: r.root() };
}
/**
 * This NgModule should be added to the root module of the hybrid app.
 */
var UIRouterUpgradeModule = /** @class */ (function () {
    function UIRouterUpgradeModule() {
    }
    UIRouterUpgradeModule.decorators = [
        { type: _angular_core.NgModule, args: [{
                    imports: [_uirouter_angular.UIRouterModule, _angular_upgrade_static.UpgradeModule],
                    declarations: [UIViewNgUpgrade],
                    providers: [
                        // @uirouter/angular code will use the ng1 $uiRouter instance instead of creating its own.
                        { provide: '$uiRouter', useFactory: getUIRouter, deps: ['$injector'] },
                        { provide: _uirouter_core.UIRouter, useFactory: uiRouterUpgradeFactory, deps: ['$uiRouter', _angular_core.Injector] },
                        { provide: _uirouter_angular.UIROUTER_ROOT_MODULE, useValue: {}, multi: true },
                        { provide: _uirouter_angular.UIView.PARENT_INJECT, useFactory: getParentUIViewInject, deps: [_uirouter_core.StateRegistry] }
                    ].concat(_uirouter_angular._UIROUTER_SERVICE_PROVIDERS),
                    entryComponents: [
                        UIViewNgUpgrade
                    ],
                    exports: [UIViewNgUpgrade, _uirouter_angular.UIRouterModule]
                },] },
    ];
    /** @nocollapse */
    UIRouterUpgradeModule.ctorParameters = function () { return []; };
    return UIRouterUpgradeModule;
}());
// Downgrade the UIViewNgUpgrade ng2 Component to an ng1 directive.
// The directive is used in a (generated) view template by the (host) ng1 ui-router,
// whenever it finds a view configured with a `component: <Ng2ComponentClass>`
upgradeModule.directive("uiViewNgUpgrade", _angular_upgrade_static.downgradeComponent({
    component: UIViewNgUpgrade,
    inputs: ['name']
}));
upgradeModule.run(['$injector', function (ng1Injector) {
        var $uiRouter = ng1Injector.get('$uiRouter');
        /** Add support for observable state and param changes */
        $uiRouter.plugin(_uirouter_rx.UIRouterRx);
        // Expose a merged ng1/ng2 injector as a Resolvable (on the root state).
        // This mimics how ui-router-ng2 exposes the root ng2 Injector, but
        // it retrieves from ng1 injector first, then ng2 injector if the token isn't found.
        var mergedInjector = {
            get: function (token, ng2NotFoundValue) {
                var ng2Injector = ng1Injector.get('$$angularInjector');
                return (ng1Injector.has(token) && ng1Injector.get(token)) || ng2Injector.get(token, ng2NotFoundValue);
            }
        };
        var ng2InjectorResolvable = _uirouter_core.Resolvable.fromData(_uirouter_angular.NATIVE_INJECTOR_TOKEN, mergedInjector);
        $uiRouter.stateRegistry.root().resolvables.push(ng2InjectorResolvable);
    }]);
/** Adds support for `loadChildren`: Angular NgModule lazy loading via @gntools/webpack */
upgradeModule.config(['$stateRegistryProvider', function ($stateRegistry) {
        $stateRegistry.decorator('lazyLoad', _uirouter_angular.ng2LazyLoadBuilder);
    }]);
/**
 * Define a stateProvider `views` builder decorator.
 * The decorator first applies the standard views builder function.
 * Then it finds any view components which are **actually** a Ng2 Component Class.
 * It overwrites that view's config with a ng1-to-ng2 hybrid config.
 *
 * In place of the template provider, it simply puts a <ui-view-ng-upgrade/> component
 * which that provides a ng1 -> ng2 boundary in the component tree.
 */
upgradeModule.config(['$stateRegistryProvider', function ($stateRegistry) {
        $stateRegistry.decorator('views', function (state, parentFn) {
            var views = parentFn(state);
            _uirouter_core.forEach(views, function (viewDecl, viewName) {
                if (viewDecl.$type === 'ng1-to-ng2' || typeof viewDecl.component === 'function') {
                    // Update the view config.
                    // Override default ng1 `component:` behavior (of defining a templateProvider)
                    // with a <ui-view-ng-upgrade> adapter directive template
                    viewDecl.$type = "ng1-to-ng2";
                    viewDecl.templateProvider = null;
                    viewDecl.template = "<ui-view-ng-upgrade name='" + viewDecl.$uiViewName + "'></ui-view-ng-upgrade>";
                }
            });
            return views;
        });
    }]);
// UI-Router ViewConfig factories take a view declaration object from a state.views: { foo: <ViewDeclaration> }
// and return a runtime config object (a ViewConfig)
upgradeModule.run(['$view', '$templateFactory', function ($view, $templateFactory) {
        // Register a ViewConfig factory for views of type `ng2`
        $view._pluginapi._viewConfigFactory('ng2', function (path, config) { return new _uirouter_angular.Ng2ViewConfig(path, config); });
        // Register a ViewConfig factory for views of type `ng1-to-ng2`.
        // Returns both an ng1 config and an ng2 config allowing either ng1 or ng2 ui-view components to be targeted.
        $view._pluginapi._viewConfigFactory('ng1-to-ng2', function (path, config) {
            var ng1ViewConfig = new _uirouter_angularjs.Ng1ViewConfig(path, Object.assign({}, config, { $type: 'ng1' }), $templateFactory);
            var ng2ViewConfig = new _uirouter_angular.Ng2ViewConfig(path, Object.assign({}, config, { $type: 'ng2' }));
            return [ng2ViewConfig, ng1ViewConfig];
        });
    }]);

exports.upgradeModule = upgradeModule;
exports.objectFactory = objectFactory;
exports.UIViewNgUpgrade = UIViewNgUpgrade;
exports.uiRouterUpgradeFactory = uiRouterUpgradeFactory;
exports.getUIRouter = getUIRouter;
exports.getParentUIViewInject = getParentUIViewInject;
exports.UIRouterUpgradeModule = UIRouterUpgradeModule;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=ui-router-angular-hybrid.js.map
