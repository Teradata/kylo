(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('rxjs/Subject'), require('@covalent/core/common'), require('@angular/cdk/portal'), require('@angular/cdk/overlay'), require('@angular/common'), require('@angular/material/progress-bar'), require('@angular/material/progress-spinner')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', 'rxjs/Subject', '@covalent/core/common', '@angular/cdk/portal', '@angular/cdk/overlay', '@angular/common', '@angular/material/progress-bar', '@angular/material/progress-spinner'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.loading = {}),global.ng.core,global.Rx,global.covalent.core.common,global.ng.cdk.portal,global.ng.cdk.overlay,global.ng.common,global.ng.material['progress-bar'],global.ng.material['progress-spinner']));
}(this, (function (exports,core,Subject,common,portal,overlay,common$1,progressBar,progressSpinner) { 'use strict';

/*! *****************************************************************************
Copyright (c) Microsoft Corporation. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at http://www.apache.org/licenses/LICENSE-2.0
THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED
WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.
See the Apache Version 2.0 License for specific language governing permissions
and limitations under the License.
***************************************************************************** */
/* global Reflect, Promise */
var extendStatics = Object.setPrototypeOf ||
    ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
    function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
function __extends(d, b) {
    extendStatics(d, b);
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
}

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/** @enum {string} */
var LoadingType = {
    Circular: 'circular',
    Linear: 'linear',
};
/** @enum {string} */
var LoadingMode = {
    Determinate: 'determinate',
    Indeterminate: 'indeterminate',
};
/** @enum {string} */
var LoadingStrategy = {
    Overlay: 'overlay',
    Replace: 'replace',
};
/** @enum {string} */
var LoadingStyle = {
    FullScreen: 'fullscreen',
    Overlay: 'overlay',
    None: 'none',
};
var TD_CIRCLE_DIAMETER = 100;
var TdLoadingComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _changeDetectorRef
     */
    function TdLoadingComponent(_elementRef, _changeDetectorRef) {
        this._elementRef = _elementRef;
        this._changeDetectorRef = _changeDetectorRef;
        this._animationIn = new Subject.Subject();
        this._animationOut = new Subject.Subject();
        this._mode = LoadingMode.Indeterminate;
        this._defaultMode = LoadingMode.Indeterminate;
        this._value = 0;
        this._circleDiameter = TD_CIRCLE_DIAMETER;
        /**
         * Flag for animation
         */
        this.animation = false;
        this.style = LoadingStyle.None;
        /**
         * type: LoadingType
         * Sets type of [TdLoadingComponent] rendered.
         */
        this.type = LoadingType.Circular;
        /**
         * color: primary' | 'accent' | 'warn'
         * Sets theme color of [TdLoadingComponent] rendered.
         */
        this.color = 'primary';
    }
    Object.defineProperty(TdLoadingComponent.prototype, "mode", {
        /**
         * @return {?}
         */
        get: function () {
            return this._mode;
        },
        /**
         * Sets mode of [TdLoadingComponent] to LoadingMode.Determinate or LoadingMode.Indeterminate
         * @param {?} mode
         * @return {?}
         */
        set: function (mode) {
            this._defaultMode = mode;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdLoadingComponent.prototype, "value", {
        /**
         * @return {?}
         */
        get: function () {
            return this._value;
        },
        /**
         * Sets value of [TdLoadingComponent] if mode is 'LoadingMode.Determinate'
         * @param {?} value
         * @return {?}
         */
        set: function (value) {
            this._value = value;
            // Check for changes for `OnPush` change detection
            this._changeDetectorRef.markForCheck();
        },
        enumerable: true,
        configurable: true
    });
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.ngDoCheck = function () {
        // When overlay is used and the host width has a value greater than 1px
        // set the circle diameter when possible incase the loading component was rendered in a hidden state
        if (this.isOverlay() && this._hostHeight() > 1) {
            if (this.animation) {
                this._setCircleDiameter();
                this._changeDetectorRef.markForCheck();
            }
        }
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.getHeight = function () {
        // Ignore height if style is `overlay` or `fullscreen`.
        // Add height if child elements have a height and style is `none`, else return default height.
        if (this.isOverlay() || this.isFullScreen()) {
            return undefined;
        }
        else {
            return this.height ? this.height + "px" : '150px';
        }
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.getCircleDiameter = function () {
        return this._circleDiameter;
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.getCircleStrokeWidth = function () {
        // we calculate the stroke width by setting it as 10% of its diameter
        var /** @type {?} */ strokeWidth = this.getCircleDiameter() / 10;
        return Math.abs(strokeWidth);
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.isCircular = function () {
        return this.type === LoadingType.Circular;
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.isLinear = function () {
        return this.type === LoadingType.Linear;
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.isFullScreen = function () {
        return this.style === LoadingStyle.FullScreen;
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.isOverlay = function () {
        return this.style === LoadingStyle.Overlay;
    };
    /**
     * @param {?} event
     * @return {?}
     */
    TdLoadingComponent.prototype.animationComplete = function (event) {
        // Check to see if its "in" or "out" animation to execute the proper callback
        if (!event.fromState) {
            this.inAnimationCompleted();
        }
        else {
            this.outAnimationCompleted();
        }
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.inAnimationCompleted = function () {
        this._animationIn.next(undefined);
    };
    /**
     * @return {?}
     */
    TdLoadingComponent.prototype.outAnimationCompleted = function () {
        /* little hack to reset the loader value and animation before removing it from DOM
            * else, the loader will appear with prev value when its registered again
            * and will do an animation going prev value to 0.
            */
        this.value = 0;
        // Check for changes for `OnPush` change detection
        this._changeDetectorRef.markForCheck();
        this._animationOut.next(undefined);
    };
    /**
     * Starts in animation and returns an observable for completition event.
     * @return {?}
     */
    TdLoadingComponent.prototype.startInAnimation = function () {
        /* need to switch back to the selected mode, so we have saved it in another variable
            *  and then recover it. (issue with protractor)
            */
        this._mode = this._defaultMode;
        // Set values before the animations starts
        this._setCircleDiameter();
        // Check for changes for `OnPush` change detection
        this.animation = true;
        this._changeDetectorRef.markForCheck();
        return this._animationIn.asObservable();
    };
    /**
     * Starts out animation and returns an observable for completition event.
     * @return {?}
     */
    TdLoadingComponent.prototype.startOutAnimation = function () {
        this.animation = false;
        /* need to switch back and forth from determinate/indeterminate so the setInterval()
            * inside mat-progress-spinner stops and protractor doesnt timeout waiting to sync.
            */
        this._mode = LoadingMode.Determinate;
        // Check for changes for `OnPush` change detection
        this._changeDetectorRef.markForCheck();
        return this._animationOut.asObservable();
    };
    /**
     * Calculate the proper diameter for the circle and set it
     * @return {?}
     */
    TdLoadingComponent.prototype._setCircleDiameter = function () {
        // we set a default diameter of 100 since this is the default in material
        var /** @type {?} */ diameter = TD_CIRCLE_DIAMETER;
        // if height is provided, then we take that as diameter
        if (this.height) {
            diameter = this.height;
            // else if its not provided, then we take the host height
        }
        else if (this.height === undefined) {
            diameter = this._hostHeight();
        }
        // if the diameter is over TD_CIRCLE_DIAMETER, we set TD_CIRCLE_DIAMETER
        if (!!diameter && diameter <= TD_CIRCLE_DIAMETER) {
            this._circleDiameter = Math.floor(diameter);
        }
        else {
            this._circleDiameter = TD_CIRCLE_DIAMETER;
        }
    };
    /**
     * Returns the host height of the loading component
     * @return {?}
     */
    TdLoadingComponent.prototype._hostHeight = function () {
        if ((this._elementRef.nativeElement)) {
            return ((this._elementRef.nativeElement)).getBoundingClientRect().height;
        }
        return 0;
    };
    return TdLoadingComponent;
}());
TdLoadingComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-loading',
                styles: [".td-loading-wrapper{\n  position:relative;\n  display:block; }\n  .td-loading-wrapper.td-fullscreen{\n    position:inherit; }\n  .td-loading-wrapper .td-loading{\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex;\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    -webkit-box-align:center;\n        -ms-flex-align:center;\n            align-items:center;\n    -ms-flex-line-pack:center;\n        align-content:center;\n    max-width:100%;\n    -webkit-box-pack:center;\n        -ms-flex-pack:center;\n            justify-content:center;\n    -webkit-box-flex:1;\n        -ms-flex:1;\n            flex:1; }\n  .td-loading-wrapper.td-overlay .td-loading{\n    position:absolute;\n    margin:0;\n    top:0;\n    left:0;\n    right:0;\n    z-index:1000; }\n    .td-loading-wrapper.td-overlay .td-loading mat-progress-bar{\n      position:absolute;\n      top:0;\n      left:0;\n      right:0; }\n  .td-loading-wrapper.td-overlay-circular .td-loading{\n    bottom:0; }\n"],
                template: "<div class=\"td-loading-wrapper\"\n    [style.min-height]=\"getHeight()\"\n    [class.td-overlay-circular]=\"(isOverlay() || isFullScreen()) && !isLinear()\"\n    [class.td-overlay]=\"isOverlay() || isFullScreen()\"\n    [class.td-fullscreen]=\"isFullScreen()\">\n  <div [@tdFadeInOut]=\"animation\"\n     (@tdFadeInOut.done)=\"animationComplete($event)\"\n     [style.min-height]=\"getHeight()\"\n     class=\"td-loading\">\n    <mat-progress-spinner *ngIf=\"isCircular()\"\n                        [mode]=\"mode\"\n                        [value]=\"value\"\n                        [color]=\"color\"\n                        [diameter]=\"getCircleDiameter()\"\n                        [strokeWidth]=\"getCircleStrokeWidth()\">\n    </mat-progress-spinner>\n    <mat-progress-bar *ngIf=\"isLinear()\"\n                     [mode]=\"mode\"\n                     [value]=\"value\"\n                     [color]=\"color\">\n    </mat-progress-bar>\n  </div>\n  <ng-template [cdkPortalHost]=\"content\"></ng-template>\n</div>",
                animations: [
                    common.TdFadeInOutAnimation(),
                ],
            },] },
];
/** @nocollapse */
TdLoadingComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.ChangeDetectorRef, },
]; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * @record
 */
/**
 * @record
 */
/**
 * NOTE: \@internal usage only.
 */
var TdLoadingFactory = /** @class */ (function () {
    /**
     * @param {?} _componentFactoryResolver
     * @param {?} _overlay
     * @param {?} _injector
     */
    function TdLoadingFactory(_componentFactoryResolver, _overlay, _injector) {
        this._componentFactoryResolver = _componentFactoryResolver;
        this._overlay = _overlay;
        this._injector = _injector;
    }
    /**
     * Uses material `Overlay` services to create a DOM element and attach the loading component
     * into it. Leveraging the state and configuration from it.
     *
     * Saves a reference in context to be called when registering/resolving the loading element.
     * @param {?} options
     * @return {?}
     */
    TdLoadingFactory.prototype.createFullScreenComponent = function (options) {
        var _this = this;
        ((options)).height = undefined;
        ((options)).style = LoadingStyle.FullScreen;
        var /** @type {?} */ loadingRef = this._initializeContext();
        var /** @type {?} */ loading = false;
        var /** @type {?} */ overlayRef;
        loadingRef.observable
            .subscribe(function (registered) {
            if (registered > 0 && !loading) {
                loading = true;
                overlayRef = _this._createOverlay();
                loadingRef.componentRef = overlayRef.attach(new portal.ComponentPortal(TdLoadingComponent));
                _this._mapOptions(options, loadingRef.componentRef.instance);
                loadingRef.componentRef.instance.startInAnimation();
                loadingRef.componentRef.changeDetectorRef.detectChanges();
            }
            else if (registered <= 0 && loading) {
                loading = false;
                var /** @type {?} */ subs_1 = loadingRef.componentRef.instance.startOutAnimation().subscribe(function () {
                    subs_1.unsubscribe();
                    loadingRef.componentRef.destroy();
                    overlayRef.detach();
                    overlayRef.dispose();
                });
            }
        });
        return loadingRef;
    };
    /**
     * Creates a loading component dynamically and attaches it into the given viewContainerRef.
     * Leverages TemplatePortals from material to inject the template inside of it so it fits
     * perfectly when overlaying it.
     *
     * Saves a reference in context to be called when registering/resolving the loading element.
     * @param {?} options
     * @param {?} viewContainerRef
     * @param {?} templateRef
     * @return {?}
     */
    TdLoadingFactory.prototype.createOverlayComponent = function (options, viewContainerRef, templateRef) {
        ((options)).height = undefined;
        ((options)).style = LoadingStyle.Overlay;
        var /** @type {?} */ loadingRef = this._createComponent(options);
        var /** @type {?} */ loading = false;
        loadingRef.componentRef.instance.content = new portal.TemplatePortal(templateRef, viewContainerRef);
        viewContainerRef.clear();
        viewContainerRef.insert(loadingRef.componentRef.hostView, 0);
        loadingRef.observable
            .subscribe(function (registered) {
            if (registered > 0 && !loading) {
                loading = true;
                loadingRef.componentRef.instance.startInAnimation();
            }
            else if (registered <= 0 && loading) {
                loading = false;
                loadingRef.componentRef.instance.startOutAnimation();
            }
        });
        return loadingRef;
    };
    /**
     * Creates a loading component dynamically and attaches it into the given viewContainerRef.
     * Replaces the template with the loading component depending if it was registered or resolved.
     *
     * Saves a reference in context to be called when registering/resolving the loading element.
     * @param {?} options
     * @param {?} viewContainerRef
     * @param {?} templateRef
     * @param {?} context
     * @return {?}
     */
    TdLoadingFactory.prototype.createReplaceComponent = function (options, viewContainerRef, templateRef, context) {
        var /** @type {?} */ nativeElement = (templateRef.elementRef.nativeElement);
        ((options)).height = nativeElement.nextElementSibling ?
            nativeElement.nextElementSibling.scrollHeight : undefined;
        ((options)).style = LoadingStyle.None;
        var /** @type {?} */ loadingRef = this._createComponent(options);
        var /** @type {?} */ loading = false;
        viewContainerRef.createEmbeddedView(templateRef, context);
        loadingRef.observable
            .subscribe(function (registered) {
            if (registered > 0 && !loading) {
                loading = true;
                var /** @type {?} */ index = viewContainerRef.indexOf(loadingRef.componentRef.hostView);
                if (index < 0) {
                    viewContainerRef.clear();
                    viewContainerRef.insert(loadingRef.componentRef.hostView, 0);
                }
                loadingRef.componentRef.instance.startInAnimation();
            }
            else if (registered <= 0 && loading) {
                loading = false;
                var /** @type {?} */ subs_2 = loadingRef.componentRef.instance.startOutAnimation().subscribe(function () {
                    subs_2.unsubscribe();
                    // passing context so when the template is re-attached, we can keep the reference of the variables
                    var /** @type {?} */ cdr = viewContainerRef.createEmbeddedView(templateRef, context);
                    viewContainerRef.detach(viewContainerRef.indexOf(loadingRef.componentRef.hostView));
                    /**
                               * Need to call "markForCheck" and "detectChanges" on attached template, so its detected by parent component when attached
                               * with "OnPush" change detection
                               */
                    cdr.detectChanges();
                    cdr.markForCheck();
                });
            }
        });
        return loadingRef;
    };
    /**
     * Creates a fullscreen overlay for the loading usage.
     * @return {?}
     */
    TdLoadingFactory.prototype._createOverlay = function () {
        var /** @type {?} */ state = new overlay.OverlayConfig();
        state.hasBackdrop = false;
        state.positionStrategy = this._overlay.position().global().centerHorizontally().centerVertically();
        return this._overlay.create(state);
    };
    /**
     * Creates a generic component dynamically waiting to be attached to a viewContainerRef.
     * @param {?} options
     * @return {?}
     */
    TdLoadingFactory.prototype._createComponent = function (options) {
        var /** @type {?} */ compRef = this._initializeContext();
        compRef.componentRef = this._componentFactoryResolver
            .resolveComponentFactory(TdLoadingComponent).create(this._injector);
        this._mapOptions(options, compRef.componentRef.instance);
        return compRef;
    };
    /**
     * Initialize context for loading component.
     * @return {?}
     */
    TdLoadingFactory.prototype._initializeContext = function () {
        var /** @type {?} */ subject = new Subject.Subject();
        return {
            observable: subject.asObservable(),
            subject: subject,
            componentRef: undefined,
            times: 0,
        };
    };
    /**
     * Maps configuration to the loading component instance.
     * @param {?} options
     * @param {?} instance
     * @return {?}
     */
    TdLoadingFactory.prototype._mapOptions = function (options, instance) {
        instance.style = options.style;
        if (options.type !== undefined) {
            instance.type = options.type;
        }
        if (options.height !== undefined) {
            instance.height = options.height;
        }
        if (options.mode !== undefined) {
            instance.mode = options.mode;
        }
        if (options.color !== undefined) {
            instance.color = options.color;
        }
    };
    return TdLoadingFactory;
}());
TdLoadingFactory.decorators = [
    { type: core.Injectable },
];
/** @nocollapse */
TdLoadingFactory.ctorParameters = function () { return [
    { type: core.ComponentFactoryResolver, },
    { type: overlay.Overlay, },
    { type: core.Injector, },
]; };
/**
 * @param {?} parent
 * @param {?} componentFactoryResolver
 * @param {?} overlay
 * @param {?} injector
 * @return {?}
 */
function LOADING_FACTORY_PROVIDER_FACTORY(parent, componentFactoryResolver, overlay$$1, injector) {
    return parent || new TdLoadingFactory(componentFactoryResolver, overlay$$1, injector);
}
var LOADING_FACTORY_PROVIDER = {
    // If there is already a service available, use that. Otherwise, provide a new one.
    provide: TdLoadingFactory,
    deps: [[new core.Optional(), new core.SkipSelf(), TdLoadingFactory], core.ComponentFactoryResolver, overlay.Overlay, core.Injector],
    useFactory: LOADING_FACTORY_PROVIDER_FACTORY,
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * @record
 */
var TdLoadingConfig = /** @class */ (function () {
    /**
     * @param {?} config
     */
    function TdLoadingConfig(config) {
        this.name = config.name;
        if (!this.name) {
            throw Error('Name is required for [TdLoading] configuration.');
        }
        this.mode = config.mode ? config.mode : LoadingMode.Indeterminate;
        this.type = config.type ? config.type : LoadingType.Circular;
        this.color = config.color ? config.color : 'primary';
    }
    return TdLoadingConfig;
}());
/**
 * @record
 */
var TdLoadingDirectiveConfig = /** @class */ (function (_super) {
    __extends(TdLoadingDirectiveConfig, _super);
    /**
     * @param {?} config
     */
    function TdLoadingDirectiveConfig(config) {
        var _this = _super.call(this, config) || this;
        _this.strategy = config.strategy ? config.strategy : LoadingStrategy.Replace;
        return _this;
    }
    return TdLoadingDirectiveConfig;
}(TdLoadingConfig));
var TdLoadingService = /** @class */ (function () {
    /**
     * @param {?} _loadingFactory
     */
    function TdLoadingService(_loadingFactory) {
        this._loadingFactory = _loadingFactory;
        this._context = {};
        this._timeouts = {};
        this.create({
            name: 'td-loading-main',
        });
    }
    /**
     * params:
     * - config: ILoadingDirectiveConfig
     * - viewContainerRef: ViewContainerRef
     * - templateRef: TemplateRef<Object>
     *
     * Creates an replace loading mask and attaches it to the viewContainerRef.
     * Replaces the templateRef with the mask when a request is registered on it.
     *
     * NOTE: \@internal usage only.
     * @param {?} config
     * @param {?} viewContainerRef
     * @param {?} templateRef
     * @param {?} context
     * @return {?}
     */
    TdLoadingService.prototype.createComponent = function (config, viewContainerRef, templateRef, context) {
        var /** @type {?} */ directiveConfig = new TdLoadingDirectiveConfig(config);
        if (this._context[directiveConfig.name]) {
            throw Error("Name duplication: [TdLoading] directive has a name conflict with " + directiveConfig.name + ".");
        }
        if (directiveConfig.strategy === LoadingStrategy.Overlay) {
            this._context[directiveConfig.name] = this._loadingFactory.createOverlayComponent(directiveConfig, viewContainerRef, templateRef);
        }
        else {
            this._context[directiveConfig.name] = this._loadingFactory.createReplaceComponent(directiveConfig, viewContainerRef, templateRef, context);
        }
        return this._context[directiveConfig.name];
    };
    /**
     * params:
     * - config: ITdLoadingConfig
     *
     * Creates a fullscreen loading mask and attaches it to the DOM with the given configuration.
     * Only displayed when the mask has a request registered on it.
     * @param {?} config
     * @return {?}
     */
    TdLoadingService.prototype.create = function (config) {
        var /** @type {?} */ fullscreenConfig = new TdLoadingConfig(config);
        this.removeComponent(fullscreenConfig.name);
        this._context[fullscreenConfig.name] = this._loadingFactory.createFullScreenComponent(fullscreenConfig);
    };
    /**
     * params:
     * - name: string
     *
     * Removes `loading` component from service context.
     * @param {?} name
     * @return {?}
     */
    TdLoadingService.prototype.removeComponent = function (name) {
        if (this._context[name]) {
            this._context[name].subject.unsubscribe();
            if (this._context[name].componentRef) {
                this._context[name].componentRef.destroy();
            }
            this._context[name] = undefined;
            delete this._context[name];
        }
    };
    /**
     * params:
     * - name: string
     * - registers?: number
     * returns: true if successful
     *
     * Resolves a request for the loading mask referenced by the name parameter.
     * Can optionally pass registers argument to set a number of register calls.
     *
     * If no paramemeters are used, then default main mask will be used.
     *
     * e.g. loadingService.register()
     * @param {?=} name
     * @param {?=} registers
     * @return {?}
     */
    TdLoadingService.prototype.register = function (name, registers) {
        var _this = this;
        if (name === void 0) { name = 'td-loading-main'; }
        if (registers === void 0) { registers = 1; }
        // try registering into the service if the loading component has been instanciated or if it exists.
        if (this._context[name]) {
            registers = registers < 1 ? 1 : registers;
            this._context[name].times += registers;
            this._context[name].subject.next(this._context[name].times);
            return true;
        }
        else {
            // if it doesnt exist, set a timeout so its registered after change detection happens
            // this in case "register" occured on the `ngOnInit` lifehook cycle.
            if (!this._timeouts[name]) {
                this._timeouts[name] = setTimeout(function () {
                    _this.register(name, registers);
                });
            }
            else {
                // if it timeout occured and still doesnt exist, it means the tiemout wasnt needed so we clear it.
                this._clearTimeout(name);
            }
        }
        return false;
    };
    /**
     * params:
     * - name: string
     * - resolves?: number
     * returns: true if successful
     *
     * Resolves a request for the loading mask referenced by the name parameter.
     * Can optionally pass resolves argument to set a number of resolve calls.
     *
     * If no paramemeters are used, then default main mask will be used.
     *
     * e.g. loadingService.resolve()
     * @param {?=} name
     * @param {?=} resolves
     * @return {?}
     */
    TdLoadingService.prototype.resolve = function (name, resolves) {
        if (name === void 0) { name = 'td-loading-main'; }
        if (resolves === void 0) { resolves = 1; }
        // clear timeout if the loading component is "resolved" before its "registered"
        this._clearTimeout(name);
        if (this._context[name]) {
            resolves = resolves < 1 ? 1 : resolves;
            if (this._context[name].times > 0) {
                var /** @type {?} */ times = this._context[name].times;
                times -= resolves;
                this._context[name].times = times < 0 ? 0 : times;
            }
            this._context[name].subject.next(this._context[name].times);
            return true;
        }
        return false;
    };
    /**
     * params:
     * - name: string
     * returns: true if successful
     *
     * Resolves all request for the loading mask referenced by the name parameter.
     *
     * If no paramemeters are used, then default main mask will be used.
     *
     * e.g. loadingService.resolveAll()
     * @param {?=} name
     * @return {?}
     */
    TdLoadingService.prototype.resolveAll = function (name) {
        if (name === void 0) { name = 'td-loading-main'; }
        // clear timeout if the loading component is "resolved" before its "registered"
        this._clearTimeout(name);
        if (this._context[name]) {
            this._context[name].times = 0;
            this._context[name].subject.next(this._context[name].times);
            return true;
        }
        return false;
    };
    /**
     * params:
     * - name: string
     * - value: number
     * returns: true if successful
     *
     * Set value on a loading mask referenced by the name parameter.
     * Usage only available if its mode is 'determinate' and if loading is showing.
     * @param {?} name
     * @param {?} value
     * @return {?}
     */
    TdLoadingService.prototype.setValue = function (name, value) {
        if (this._context[name]) {
            var /** @type {?} */ instance = this._context[name].componentRef.instance;
            if (instance.mode === LoadingMode.Determinate && instance.animation) {
                instance.value = value;
                return true;
            }
        }
        return false;
    };
    /**
     * Clears timeout linked to the name.
     * @param {?} name Name of the loading component to be cleared
     * @return {?}
     */
    TdLoadingService.prototype._clearTimeout = function (name) {
        clearTimeout(this._timeouts[name]);
        delete this._timeouts[name];
    };
    return TdLoadingService;
}());
TdLoadingService.decorators = [
    { type: core.Injectable },
];
/** @nocollapse */
TdLoadingService.ctorParameters = function () { return [
    { type: TdLoadingFactory, },
]; };
/**
 * @param {?} parent
 * @param {?} loadingFactory
 * @return {?}
 */
function LOADING_PROVIDER_FACTORY(parent, loadingFactory) {
    return parent || new TdLoadingService(loadingFactory);
}
var LOADING_PROVIDER = {
    // If there is already a service available, use that. Otherwise, provide a new one.
    provide: TdLoadingService,
    deps: [[new core.Optional(), new core.SkipSelf(), TdLoadingService], TdLoadingFactory],
    useFactory: LOADING_PROVIDER_FACTORY,
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * Context class for variable reference
 */
var TdLoadingContext = /** @class */ (function () {
    function TdLoadingContext() {
        this.$implicit = undefined;
        this.tdLoading = undefined;
    }
    return TdLoadingContext;
}());
// Constant for generation of the id for the next component
var TD_LOADING_NEXT_ID = 0;
var TdLoadingDirective = /** @class */ (function () {
    /**
     * @param {?} _viewContainerRef
     * @param {?} _templateRef
     * @param {?} _loadingService
     */
    function TdLoadingDirective(_viewContainerRef, _templateRef, _loadingService) {
        this._viewContainerRef = _viewContainerRef;
        this._templateRef = _templateRef;
        this._loadingService = _loadingService;
        this._context = new TdLoadingContext();
        /**
         * tdLoadingColor?: "primary" | "accent" | "warn"
         * Sets the theme color of the loading component. Defaults to "primary"
         */
        this.color = 'primary';
    }
    Object.defineProperty(TdLoadingDirective.prototype, "name", {
        /**
         * tdLoading: string
         * Name reference of the loading mask, used to register/resolve requests to the mask.
         * @param {?} name
         * @return {?}
         */
        set: function (name) {
            if (!this._name) {
                if (name) {
                    this._name = name;
                }
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdLoadingDirective.prototype, "until", {
        /**
         * tdLoadingUntil?: any
         * If its null, undefined or false it will be used to register requests to the mask.
         * Else if its any value that can be resolved as true, it will resolve the mask.
         * [name] is optional when using [until], but can still be used to register/resolve it manually.
         * @param {?} until
         * @return {?}
         */
        set: function (until) {
            if (!this._name) {
                this._name = 'td-loading-until-' + TD_LOADING_NEXT_ID++;
            }
            this._context.$implicit = this._context.tdLoading = until;
            if (!until) {
                this._loadingService.register(this._name);
            }
            else {
                this._loadingService.resolveAll(this._name);
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdLoadingDirective.prototype, "type", {
        /**
         * tdLoadingType?: LoadingType or ['linear' | 'circular']
         * Sets the type of loading mask depending on value.
         * Defaults to [LoadingType.Circular | 'circular'].
         * @param {?} type
         * @return {?}
         */
        set: function (type) {
            switch (type) {
                case LoadingType.Linear:
                    this._type = LoadingType.Linear;
                    break;
                default:
                    this._type = LoadingType.Circular;
                    break;
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdLoadingDirective.prototype, "mode", {
        /**
         * tdLoadingMode?: LoadingMode or ['determinate' | 'indeterminate']
         * Sets the mode of loading mask depending on value.
         * Defaults to [LoadingMode.Indeterminate | 'indeterminate'].
         * @param {?} mode
         * @return {?}
         */
        set: function (mode) {
            switch (mode) {
                case LoadingMode.Determinate:
                    this._mode = LoadingMode.Determinate;
                    break;
                default:
                    this._mode = LoadingMode.Indeterminate;
                    break;
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdLoadingDirective.prototype, "strategy", {
        /**
         * tdLoadingStrategy?: LoadingStrategy or ['replace' | 'overlay']
         * Sets the strategy of loading mask depending on value.
         * Defaults to [LoadingMode.Replace | 'replace'].
         * @param {?} stategy
         * @return {?}
         */
        set: function (stategy) {
            switch (stategy) {
                case LoadingStrategy.Overlay:
                    this._strategy = LoadingStrategy.Overlay;
                    break;
                default:
                    this._strategy = LoadingStrategy.Replace;
                    break;
            }
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Registers component in the DOM, so it will be available when calling resolve/register.
     * @return {?}
     */
    TdLoadingDirective.prototype.ngOnInit = function () {
        this._registerComponent();
    };
    /**
     * Remove component when directive is destroyed.
     * @return {?}
     */
    TdLoadingDirective.prototype.ngOnDestroy = function () {
        this._loadingService.removeComponent(this._name);
        this._loadingRef = undefined;
    };
    /**
     * Creates [TdLoadingComponent] and attaches it to this directive's [ViewContainerRef].
     * Passes this directive's [TemplateRef] to modify DOM depending on loading `strategy`.
     * @return {?}
     */
    TdLoadingDirective.prototype._registerComponent = function () {
        if (!this._name) {
            throw new Error('Name is needed to register loading directive');
        }
        // Check if `TdLoadingComponent` has been created before trying to add one again.
        // There is a weird edge case when using `[routerLinkActive]` that calls the `ngOnInit` twice in a row
        if (!this._loadingRef) {
            this._loadingRef = this._loadingService.createComponent({
                name: this._name,
                type: this._type,
                mode: this._mode,
                color: this.color,
                strategy: this._strategy,
            }, this._viewContainerRef, this._templateRef, this._context);
        }
    };
    return TdLoadingDirective;
}());
TdLoadingDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[tdLoading]',
            },] },
];
/** @nocollapse */
TdLoadingDirective.ctorParameters = function () { return [
    { type: core.ViewContainerRef, },
    { type: core.TemplateRef, },
    { type: TdLoadingService, },
]; };
TdLoadingDirective.propDecorators = {
    "name": [{ type: core.Input, args: ['tdLoading',] },],
    "until": [{ type: core.Input, args: ['tdLoadingUntil',] },],
    "type": [{ type: core.Input, args: ['tdLoadingType',] },],
    "mode": [{ type: core.Input, args: ['tdLoadingMode',] },],
    "strategy": [{ type: core.Input, args: ['tdLoadingStrategy',] },],
    "color": [{ type: core.Input, args: ['tdLoadingColor',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_LOADING = [
    TdLoadingComponent,
    TdLoadingDirective,
];
var TD_LOADING_ENTRY_COMPONENTS = [
    TdLoadingComponent,
];
var CovalentLoadingModule = /** @class */ (function () {
    function CovalentLoadingModule() {
    }
    return CovalentLoadingModule;
}());
CovalentLoadingModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common$1.CommonModule,
                    progressBar.MatProgressBarModule,
                    progressSpinner.MatProgressSpinnerModule,
                    overlay.OverlayModule,
                    portal.PortalModule,
                ],
                declarations: [
                    TD_LOADING,
                ],
                exports: [
                    TD_LOADING,
                ],
                providers: [
                    LOADING_FACTORY_PROVIDER,
                    LOADING_PROVIDER,
                ],
                entryComponents: [
                    TD_LOADING_ENTRY_COMPONENTS,
                ],
            },] },
];
/** @nocollapse */
CovalentLoadingModule.ctorParameters = function () { return []; };

exports.CovalentLoadingModule = CovalentLoadingModule;
exports.LoadingType = LoadingType;
exports.LoadingMode = LoadingMode;
exports.LoadingStrategy = LoadingStrategy;
exports.LoadingStyle = LoadingStyle;
exports.TD_CIRCLE_DIAMETER = TD_CIRCLE_DIAMETER;
exports.TdLoadingComponent = TdLoadingComponent;
exports.TdLoadingContext = TdLoadingContext;
exports.TdLoadingDirective = TdLoadingDirective;
exports.TdLoadingConfig = TdLoadingConfig;
exports.TdLoadingDirectiveConfig = TdLoadingDirectiveConfig;
exports.TdLoadingService = TdLoadingService;
exports.LOADING_PROVIDER_FACTORY = LOADING_PROVIDER_FACTORY;
exports.LOADING_PROVIDER = LOADING_PROVIDER;
exports.TdLoadingFactory = TdLoadingFactory;
exports.LOADING_FACTORY_PROVIDER_FACTORY = LOADING_FACTORY_PROVIDER_FACTORY;
exports.LOADING_FACTORY_PROVIDER = LOADING_FACTORY_PROVIDER;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-loading.umd.js.map
