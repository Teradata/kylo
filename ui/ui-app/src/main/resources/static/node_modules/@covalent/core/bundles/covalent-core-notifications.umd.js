(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/common')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/common'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.notifications = {}),global.ng.core,global.ng.common));
}(this, (function (exports,core,common) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/** @enum {string} */
var TdNotificationCountPositionY = {
    Top: 'top',
    Bottom: 'bottom',
    Center: 'center',
};
/** @enum {string} */
var TdNotificationCountPositionX = {
    Before: 'before',
    After: 'after',
    Center: 'center',
};
var TdNotificationCountComponent = /** @class */ (function () {
    function TdNotificationCountComponent() {
        this._notifications = 0;
        /**
         * color?: "primary" | "accent" | "warn"
         * Sets the theme color of the notification tip. Defaults to "warn"
         */
        this.color = 'warn';
    }
    Object.defineProperty(TdNotificationCountComponent.prototype, "positionX", {
        /**
         * @return {?}
         */
        get: function () {
            return this._positionX;
        },
        /**
         * positionX?: TdNotificationCountPositionX or "before" | "after" | "center"
         * Sets the X position of the notification tip.
         * Defaults to "after" if it has content, else 'center'.
         * @param {?} positionX
         * @return {?}
         */
        set: function (positionX) {
            this._positionX = positionX;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "positionY", {
        /**
         * @return {?}
         */
        get: function () {
            return this._positionY;
        },
        /**
         * positionY?: TdNotificationCountPositionY or "top" | "bottom" | "center"
         * Sets the Y position of the notification tip.
         * Defaults to "top" if it has content, else 'center'.
         * @param {?} positionY
         * @return {?}
         */
        set: function (positionY) {
            this._positionY = positionY;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "notifications", {
        /**
         * notifications?: number | boolean
         * Number for the notification count. Shows component only if the input is a positive number or 'true'
         * @param {?} notifications
         * @return {?}
         */
        set: function (notifications) {
            this._notifications = notifications;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "hideHost", {
        /**
         * @return {?}
         */
        get: function () {
            return !this.show && !this._hasContent();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "noCount", {
        /**
         * Sets the component in its 'noCount' state if [notifications] is a boolean 'true'.
         * Makes the notification tip show without a count.
         * @return {?}
         */
        get: function () {
            return this._notifications === true;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "notificationsDisplay", {
        /**
         * Notification display string when a count is available.
         * Anything over 99 gets set as 99+
         * @return {?}
         */
        get: function () {
            if (this._notifications > 99) {
                return '99+';
            }
            return this._notifications.toString();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdNotificationCountComponent.prototype, "show", {
        /**
         * Shows notification tip only when [notifications] is true or a positive integer.
         * @return {?}
         */
        get: function () {
            return this._notifications === true || (!isNaN(/** @type {?} */ (this._notifications)) && this._notifications > 0);
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Check if [positionX] and [positionY] have been set as inputs, else use defaults depending on component content.
     * @return {?}
     */
    TdNotificationCountComponent.prototype.ngAfterContentInit = function () {
        if (!this._positionX) {
            this.positionX = this._hasContent() ? TdNotificationCountPositionX.After : TdNotificationCountPositionX.Center;
        }
        if (!this._positionY) {
            this.positionY = this._hasContent() ? TdNotificationCountPositionY.Top : TdNotificationCountPositionY.Center;
        }
    };
    /**
     * Method to check if element has any kind of content (elements or text)
     * @return {?}
     */
    TdNotificationCountComponent.prototype._hasContent = function () {
        if (this.content) {
            var /** @type {?} */ contentElement = this.content.nativeElement;
            return contentElement && (contentElement.children.length > 0 || !!contentElement.textContent.trim());
        }
        return false;
    };
    return TdNotificationCountComponent;
}());
TdNotificationCountComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-notification-count',
                styles: [":host{\n  position:relative;\n  display:block;\n  text-align:center;\n  min-width:40px;\n  height:40px; }\n  :host.td-notification-hidden{\n    min-width:0; }\n.td-notification-count{\n  line-height:21px;\n  width:20px;\n  height:20px;\n  position:absolute;\n  font-size:10px;\n  font-weight:600;\n  border-radius:50%;\n  z-index:1; }\n  .td-notification-count.td-notification-center-x{\n    margin-left:auto;\n    margin-right:auto;\n    left:0;\n    right:0; }\n  .td-notification-count.td-notification-center-y{\n    margin-top:auto;\n    margin-bottom:auto;\n    top:0;\n    bottom:0; }\n  .td-notification-count.td-notification-top{\n    top:0; }\n  .td-notification-count.td-notification-bottom{\n    bottom:0; }\n  .td-notification-count.td-notification-before{\n    left:0; }\n  .td-notification-count.td-notification-after{\n    right:0; }\n  .td-notification-count.td-notification-no-count{\n    width:8px;\n    height:8px; }\n    .td-notification-count.td-notification-no-count.td-notification-top{\n      top:8px; }\n    .td-notification-count.td-notification-no-count.td-notification-bottom{\n      bottom:8px; }\n    .td-notification-count.td-notification-no-count.td-notification-before{\n      left:8px; }\n    .td-notification-count.td-notification-no-count.td-notification-after{\n      right:8px; }\n  ::ng-deep [dir='rtl'] .td-notification-count.td-notification-before{\n    right:0;\n    left:auto; }\n  ::ng-deep [dir='rtl'] .td-notification-count.td-notification-after{\n    left:0;\n    right:auto; }\n  ::ng-deep [dir='rtl'] .td-notification-count.td-notification-no-count.td-notification-before{\n    right:8px;\n    left:auto; }\n  ::ng-deep [dir='rtl'] .td-notification-count.td-notification-no-count.td-notification-after{\n    left:8px;\n    right:auto; }\n.td-notification-content, .td-notification-content ::ng-deep > *{\n  line-height:40px; }\n"],
                template: "<div #content class=\"td-notification-content\">\n  <ng-content></ng-content>\n</div>\n<div *ngIf=\"show\"\n     class=\"td-notification-count mat-{{color}}\"\n     [class.td-notification-top]=\"positionY === 'top'\"\n     [class.td-notification-bottom]=\"positionY === 'bottom'\"\n     [class.td-notification-before]=\"positionX === 'before'\"\n     [class.td-notification-after]=\"positionX === 'after'\"\n     [class.td-notification-center-y]=\"positionY === 'center'\"\n     [class.td-notification-center-x]=\"positionX === 'center'\"\n     [class.td-notification-no-count]=\"noCount\">\n  {{noCount ? '' : notificationsDisplay}}\n</div>",
                changeDetection: core.ChangeDetectionStrategy.OnPush,
            },] },
];
/** @nocollapse */
TdNotificationCountComponent.ctorParameters = function () { return []; };
TdNotificationCountComponent.propDecorators = {
    "content": [{ type: core.ViewChild, args: ['content',] },],
    "color": [{ type: core.Input },],
    "positionX": [{ type: core.Input },],
    "positionY": [{ type: core.Input },],
    "notifications": [{ type: core.Input },],
    "hideHost": [{ type: core.HostBinding, args: ['class.td-notification-hidden',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_NOTIFICATIONS = [
    TdNotificationCountComponent,
];
var CovalentNotificationsModule = /** @class */ (function () {
    function CovalentNotificationsModule() {
    }
    return CovalentNotificationsModule;
}());
CovalentNotificationsModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common.CommonModule,
                ],
                declarations: [
                    TD_NOTIFICATIONS,
                ],
                exports: [
                    TD_NOTIFICATIONS,
                ],
            },] },
];
/** @nocollapse */
CovalentNotificationsModule.ctorParameters = function () { return []; };

exports.CovalentNotificationsModule = CovalentNotificationsModule;
exports.TdNotificationCountPositionY = TdNotificationCountPositionY;
exports.TdNotificationCountPositionX = TdNotificationCountPositionX;
exports.TdNotificationCountComponent = TdNotificationCountComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-notifications.umd.js.map
