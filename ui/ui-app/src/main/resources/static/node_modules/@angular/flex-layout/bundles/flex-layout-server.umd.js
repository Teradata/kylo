/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/common'), require('@angular/platform-server'), require('@angular/flex-layout/core'), require('@angular/core')) :
	typeof define === 'function' && define.amd ? define('@angular/flex-layout/server', ['exports', '@angular/common', '@angular/platform-server', '@angular/flex-layout/core', '@angular/core'], factory) :
	(factory((global.ng = global.ng || {}, global.ng['flex-layout'] = global.ng['flex-layout'] || {}, global.ng['flex-layout'].server = {}),global.ng.common,global.ng.platformServer,global.ng.flexLayout.core,global.ng.core));
}(this, (function (exports,common,platformServer,core,core$1) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * Activate all of the registered breakpoints in sequence, and then
 * retrieve the associated stylings from the virtual stylesheet
 * @param {?} serverSheet the virtual stylesheet that stores styles for each
 *        element
 * @param {?} matchMedia the service to activate/deactive breakpoints
 * @param {?} breakpoints the registered breakpoints to activate/deactivate
 * @return {?}
 */
function generateStaticFlexLayoutStyles(serverSheet, matchMedia, breakpoints) {
    // Store the custom classes in the following map, that way only
    // one class gets allocated per HTMLElement, and each class can
    // be referenced in the static media queries
    var /** @type {?} */ classMap = new Map();
    // Get the initial stylings for all of the directives, and initialize
    // the fallback block of stylings, then reverse the breakpoints list
    // to traverse in the proper order
    var /** @type {?} */ defaultStyles = new Map(serverSheet.stylesheet);
    var /** @type {?} */ styleText = generateCss(defaultStyles, 'all', classMap);
    breakpoints.reverse();
    breakpoints.forEach(function (bp, i) {
        serverSheet.clearStyles();
        (/** @type {?} */ (matchMedia)).activateBreakpoint(bp);
        var /** @type {?} */ stylesheet = new Map(serverSheet.stylesheet);
        if (stylesheet.size > 0) {
            styleText += generateCss(stylesheet, bp.mediaQuery, classMap);
        }
        (/** @type {?} */ (matchMedia)).deactivateBreakpoint(breakpoints[i]);
    });
    return styleText;
}
/**
 * Create a style tag populated with the dynamic stylings from Flex
 * components and attach it to the head of the DOM
 * @param {?} serverSheet
 * @param {?} matchMedia
 * @param {?} _document
 * @param {?} breakpoints
 * @return {?}
 */
function FLEX_SSR_SERIALIZER_FACTORY(serverSheet, matchMedia, _document, breakpoints) {
    return function () {
        // This is the style tag that gets inserted into the head of the DOM,
        // populated with the manual media queries
        var /** @type {?} */ styleTag = _document.createElement('style');
        var /** @type {?} */ styleText = generateStaticFlexLayoutStyles(serverSheet, matchMedia, breakpoints);
        styleTag.classList.add(core.CLASS_NAME + "ssr");
        styleTag.textContent = styleText;
        _document.head.appendChild(styleTag);
    };
}
/**
 *  Provider to set static styles on the server
 */
var /** @type {?} */ SERVER_PROVIDERS = [
    {
        provide: /** @type {?} */ (platformServer.BEFORE_APP_SERIALIZED),
        useFactory: FLEX_SSR_SERIALIZER_FACTORY,
        deps: [
            core.StylesheetMap,
            core.MatchMedia,
            common.DOCUMENT,
            core.BREAKPOINTS,
        ],
        multi: true
    },
    {
        provide: core.SERVER_TOKEN,
        useValue: true
    },
    {
        provide: core.MatchMedia,
        useClass: core.ServerMatchMedia
    }
];
var /** @type {?} */ nextId = 0;
var /** @type {?} */ IS_DEBUG_MODE = false;
/**
 * create \@media queries based on a virtual stylesheet
 * * Adds a unique class to each element and stores it
 *   in a shared classMap for later reuse
 * @param {?} stylesheet the virtual stylesheet that stores styles for each
 *        element
 * @param {?} mediaQuery the given \@media CSS selector for the current breakpoint
 * @param {?} classMap the map of HTML elements to class names to avoid duplications
 * @return {?}
 */
function generateCss(stylesheet, mediaQuery, classMap) {
    var /** @type {?} */ css = '';
    stylesheet.forEach(function (styles, el) {
        var /** @type {?} */ keyVals = '', /** @type {?} */ className = getClassName(el, classMap);
        styles.forEach(function (v, k) {
            keyVals += v ? format(k + ":" + v + ";") : '';
        });
        // Build list of CSS styles; each with a className
        css += format("." + className + " {", keyVals, '}');
    });
    // Group 1 or more styles (each with className) in a specific mediaQuery
    return format("@media " + mediaQuery + " {", css, '}');
}
/**
 * For debugging purposes, prefix css segment with linefeed(s) for easy
 * debugging purposes.
 * @param {...?} list
 * @return {?}
 */
function format() {
    var list = [];
    for (var _i = 0; _i < arguments.length; _i++) {
        list[_i] = arguments[_i];
    }
    var /** @type {?} */ result = '';
    list.forEach(function (css, i) {
        result += IS_DEBUG_MODE ? formatSegment(css, i != 0) : css;
    });
    return result;
}
/**
 * @param {?} css
 * @param {?=} asPrefix
 * @return {?}
 */
function formatSegment(css, asPrefix) {
    if (asPrefix === void 0) { asPrefix = true; }
    return asPrefix ? '\n' + css : css + '\n';
}
/**
 * Get className associated with CSS styling
 * If not found, generate global className and set
 * association.
 * @param {?} stylesheet
 * @param {?} classMap
 * @return {?}
 */
function getClassName(stylesheet, classMap) {
    var /** @type {?} */ className = classMap.get(stylesheet);
    if (!className) {
        className = "" + core.CLASS_NAME + nextId++;
        classMap.set(stylesheet, className);
    }
    stylesheet.classList.add(className);
    return className;
}

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var FlexLayoutServerModule = /** @class */ (function () {
    function FlexLayoutServerModule() {
    }
    FlexLayoutServerModule.decorators = [
        { type: core$1.NgModule, args: [{
                    providers: [SERVER_PROVIDERS]
                },] },
    ];
    /** @nocollapse */
    FlexLayoutServerModule.ctorParameters = function () { return []; };
    return FlexLayoutServerModule;
}());

exports.FlexLayoutServerModule = FlexLayoutServerModule;
exports.generateStaticFlexLayoutStyles = generateStaticFlexLayoutStyles;
exports.FLEX_SSR_SERIALIZER_FACTORY = FLEX_SSR_SERIALIZER_FACTORY;
exports.SERVER_PROVIDERS = SERVER_PROVIDERS;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=flex-layout-server.umd.js.map
