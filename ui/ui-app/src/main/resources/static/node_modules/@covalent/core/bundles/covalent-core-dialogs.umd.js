(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/material/dialog'), require('@angular/common'), require('@angular/forms'), require('@angular/material/input'), require('@angular/material/button')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/material/dialog', '@angular/common', '@angular/forms', '@angular/material/input', '@angular/material/button'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.dialogs = {}),global.ng.core,global.ng.material.dialog,global.ng.common,global.ng.forms,global.ng.material.input,global.ng.material.button));
}(this, (function (exports,core,dialog,common,forms,input,button) { 'use strict';

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDialogTitleDirective = /** @class */ (function () {
    function TdDialogTitleDirective() {
    }
    return TdDialogTitleDirective;
}());
TdDialogTitleDirective.decorators = [
    { type: core.Directive, args: [{ selector: 'td-dialog-title' },] },
];
/** @nocollapse */
TdDialogTitleDirective.ctorParameters = function () { return []; };
var TdDialogContentDirective = /** @class */ (function () {
    function TdDialogContentDirective() {
    }
    return TdDialogContentDirective;
}());
TdDialogContentDirective.decorators = [
    { type: core.Directive, args: [{ selector: 'td-dialog-content' },] },
];
/** @nocollapse */
TdDialogContentDirective.ctorParameters = function () { return []; };
var TdDialogActionsDirective = /** @class */ (function () {
    function TdDialogActionsDirective() {
    }
    return TdDialogActionsDirective;
}());
TdDialogActionsDirective.decorators = [
    { type: core.Directive, args: [{ selector: 'td-dialog-actions' },] },
];
/** @nocollapse */
TdDialogActionsDirective.ctorParameters = function () { return []; };
var TdDialogComponent = /** @class */ (function () {
    function TdDialogComponent() {
    }
    /**
     * @return {?}
     */
    TdDialogComponent.prototype.ngAfterContentInit = function () {
        if (this.dialogTitle.length > 1) {
            throw new Error('Duplicate td-dialog-title component at in td-dialog.');
        }
        if (this.dialogContent.length > 1) {
            throw new Error('Duplicate td-dialog-content component at in td-dialog.');
        }
        if (this.dialogActions.length > 1) {
            throw new Error('Duplicate td-dialog-actions component at in td-dialog.');
        }
    };
    return TdDialogComponent;
}());
TdDialogComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-dialog',
                template: "<div class=\"td-dialog-wrapper\">\n  <h3 class=\"td-dialog-title\" *ngIf=\"dialogTitle.length > 0\">\n    <ng-content select=\"td-dialog-title\"></ng-content>\n  </h3>\n  <div class=\"td-dialog-content\" *ngIf=\"dialogContent.length > 0\">\n    <ng-content select=\"td-dialog-content\"></ng-content>\n  </div>\n  <div class=\"td-dialog-actions\" *ngIf=\"dialogActions.length > 0\">\n    <span class=\"td-dialog-spacer\"></span>\n    <ng-content select=\"td-dialog-actions\"></ng-content>\n  </div>\n</div>",
                styles: [".td-dialog-title{\n  margin-top:0;\n  margin-bottom:20px; }\n.td-dialog-content{\n  margin-bottom:16px; }\n.td-dialog-actions{\n  position:relative;\n  top:16px;\n  left:16px; }\n  ::ng-deep [dir='rtl'] .td-dialog-actions{\n    right:16px;\n    left:auto; }\n:host{\n  display:block; }\n  :host .td-dialog-actions{\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex; }\n    :host .td-dialog-actions .td-dialog-spacer{\n      -webkit-box-flex:1;\n          -ms-flex:1;\n              flex:1; }\n    :host .td-dialog-actions ::ng-deep button{\n      text-transform:uppercase;\n      margin-left:8px;\n      padding-left:8px;\n      padding-right:8px;\n      min-width:64px; }\n      [dir='rtl'] :host .td-dialog-actions ::ng-deep button{\n        margin-right:8px;\n        margin-left:inherit; }\n"],
            },] },
];
/** @nocollapse */
TdDialogComponent.ctorParameters = function () { return []; };
TdDialogComponent.propDecorators = {
    "dialogTitle": [{ type: core.ContentChildren, args: [TdDialogTitleDirective,] },],
    "dialogContent": [{ type: core.ContentChildren, args: [TdDialogContentDirective,] },],
    "dialogActions": [{ type: core.ContentChildren, args: [TdDialogActionsDirective,] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdAlertDialogComponent = /** @class */ (function () {
    /**
     * @param {?} _dialogRef
     */
    function TdAlertDialogComponent(_dialogRef) {
        this._dialogRef = _dialogRef;
        this.closeButton = 'CLOSE';
    }
    /**
     * @return {?}
     */
    TdAlertDialogComponent.prototype.close = function () {
        this._dialogRef.close();
    };
    return TdAlertDialogComponent;
}());
TdAlertDialogComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-alert-dialog',
                template: "<td-dialog>\n  <td-dialog-title *ngIf=\"title\">\n    {{title}}\n  </td-dialog-title>\n  <td-dialog-content>\n    <span class=\"td-dialog-message\">{{message}}</span>\n  </td-dialog-content>\n  <td-dialog-actions>\n    <button mat-button color=\"accent\" (click)=\"close()\">{{closeButton}}</button>\n  </td-dialog-actions>\n</td-dialog>",
                styles: [".td-dialog-message{\n  word-break:break-word; }\n"],
            },] },
];
/** @nocollapse */
TdAlertDialogComponent.ctorParameters = function () { return [
    { type: dialog.MatDialogRef, },
]; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdConfirmDialogComponent = /** @class */ (function () {
    /**
     * @param {?} _dialogRef
     */
    function TdConfirmDialogComponent(_dialogRef) {
        this._dialogRef = _dialogRef;
        this.cancelButton = 'CANCEL';
        this.acceptButton = 'ACCEPT';
    }
    /**
     * @return {?}
     */
    TdConfirmDialogComponent.prototype.cancel = function () {
        this._dialogRef.close(false);
    };
    /**
     * @return {?}
     */
    TdConfirmDialogComponent.prototype.accept = function () {
        this._dialogRef.close(true);
    };
    return TdConfirmDialogComponent;
}());
TdConfirmDialogComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-confirm-dialog',
                template: "<td-dialog>\n  <td-dialog-title *ngIf=\"title\">\n    {{title}}\n  </td-dialog-title>\n  <td-dialog-content>\n    <span class=\"td-dialog-message\">{{message}}</span>\n  </td-dialog-content>\n  <td-dialog-actions>\n    <button mat-button\n            #closeBtn\n            (keydown.arrowright)=\"acceptBtn.focus()\"\n            (click)=\"cancel()\">{{cancelButton}}</button>\n    <button mat-button\n            color=\"accent\"\n            #acceptBtn\n            (keydown.arrowleft)=\"closeBtn.focus()\"\n            (click)=\"accept()\">{{acceptButton}}</button>\n  </td-dialog-actions>\n</td-dialog>",
                styles: [".td-dialog-message{\n  word-break:break-word; }\n"],
            },] },
];
/** @nocollapse */
TdConfirmDialogComponent.ctorParameters = function () { return [
    { type: dialog.MatDialogRef, },
]; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdPromptDialogComponent = /** @class */ (function () {
    /**
     * @param {?} _dialogRef
     */
    function TdPromptDialogComponent(_dialogRef) {
        this._dialogRef = _dialogRef;
        this.cancelButton = 'CANCEL';
        this.acceptButton = 'ACCEPT';
    }
    /**
     * @return {?}
     */
    TdPromptDialogComponent.prototype.ngAfterViewInit = function () {
        var _this = this;
        // focus input once everything is rendered and good to go
        Promise.resolve().then(function () {
            ((_this._input.nativeElement)).focus();
        });
    };
    /**
     * Method executed when input is focused
     * Selects all text
     * @return {?}
     */
    TdPromptDialogComponent.prototype.handleInputFocus = function () {
        ((this._input.nativeElement)).select();
    };
    /**
     * @return {?}
     */
    TdPromptDialogComponent.prototype.cancel = function () {
        this._dialogRef.close(undefined);
    };
    /**
     * @return {?}
     */
    TdPromptDialogComponent.prototype.accept = function () {
        this._dialogRef.close(this.value);
    };
    return TdPromptDialogComponent;
}());
TdPromptDialogComponent.decorators = [
    { type: core.Component, args: [{
                selector: 'td-prompt-dialog',
                template: "<td-dialog>\n  <td-dialog-title *ngIf=\"title\">\n    {{title}}\n  </td-dialog-title>\n  <td-dialog-content>\n    <span class=\"td-dialog-message\">{{message}}</span>\n    <form #form=\"ngForm\" novalidate>\n      <div class=\"td-dialog-input-wrapper\">\n        <mat-form-field class=\"td-dialog-input\">\n          <input matInput\n                #input\n                (focus)=\"handleInputFocus()\"\n                (keydown.enter)=\"$event.preventDefault(); form.valid && accept()\"\n                [(ngModel)]=\"value\"\n                name=\"value\"\n                required/>\n        </mat-form-field>\n      </div>\n    </form>\n  </td-dialog-content>\n  <td-dialog-actions>\n    <button mat-button\n            #closeBtn\n            (keydown.arrowright)=\"acceptBtn.focus()\"\n            (click)=\"cancel()\">{{cancelButton}}</button>\n    <button mat-button\n            color=\"accent\"\n            #acceptBtn\n            (keydown.arrowleft)=\"closeBtn.focus()\"\n            [disabled]=\"!form.valid\"\n            (click)=\"accept()\">{{acceptButton}}</button>\n  </td-dialog-actions>\n</td-dialog>",
                styles: [".td-dialog-input-wrapper{\n  -webkit-box-orient:horizontal;\n  -webkit-box-direction:normal;\n      -ms-flex-direction:row;\n          flex-direction:row;\n  -webkit-box-sizing:border-box;\n          box-sizing:border-box;\n  display:-webkit-box;\n  display:-ms-flexbox;\n  display:flex; }\n  .td-dialog-input-wrapper .td-dialog-input{\n    -webkit-box-flex:1;\n        -ms-flex:1;\n            flex:1;\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box; }\n.td-dialog-message{\n  word-break:break-word; }\n"],
            },] },
];
/** @nocollapse */
TdPromptDialogComponent.ctorParameters = function () { return [
    { type: dialog.MatDialogRef, },
]; };
TdPromptDialogComponent.propDecorators = {
    "_input": [{ type: core.ViewChild, args: ['input',] },],
};
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
 * @record
 */
/**
 * @record
 */
var TdDialogService = /** @class */ (function () {
    /**
     * @param {?} _dialogService
     */
    function TdDialogService(_dialogService) {
        this._dialogService = _dialogService;
    }
    /**
     * params:
     * - component: ComponentType<T>
     * - config: MatDialogConfig
     * Wrapper function over the open() method in MatDialog.
     * Opens a modal dialog containing the given component.
     * @template T
     * @param {?} component
     * @param {?=} config
     * @return {?}
     */
    TdDialogService.prototype.open = function (component, config) {
        return this._dialogService.open(component, config);
    };
    /**
     * Wrapper function over the closeAll() method in MatDialog.
     * Closes all of the currently-open dialogs.
     * @return {?}
     */
    TdDialogService.prototype.closeAll = function () {
        this._dialogService.closeAll();
    };
    /**
     * params:
     * - config: IAlertConfig {
     *     message: string;
     *     title?: string;
     *     viewContainerRef?: ViewContainerRef;
     *     closeButton?: string;
     * }
     *
     * Opens an alert dialog with the provided config.
     * Returns an MatDialogRef<TdAlertDialogComponent> object.
     * @param {?} config
     * @return {?}
     */
    TdDialogService.prototype.openAlert = function (config) {
        var /** @type {?} */ dialogConfig = this._createConfig(config);
        var /** @type {?} */ dialogRef = this._dialogService.open(TdAlertDialogComponent, dialogConfig);
        var /** @type {?} */ alertDialogComponent = dialogRef.componentInstance;
        alertDialogComponent.title = config.title;
        alertDialogComponent.message = config.message;
        if (config.closeButton) {
            alertDialogComponent.closeButton = config.closeButton;
        }
        return dialogRef;
    };
    /**
     * params:
     * - config: IConfirmConfig {
     *     message: string;
     *     title?: string;
     *     viewContainerRef?: ViewContainerRef;
     *     acceptButton?: string;
     *     cancelButton?: string;
     * }
     *
     * Opens a confirm dialog with the provided config.
     * Returns an MatDialogRef<TdConfirmDialogComponent> object.
     * @param {?} config
     * @return {?}
     */
    TdDialogService.prototype.openConfirm = function (config) {
        var /** @type {?} */ dialogConfig = this._createConfig(config);
        var /** @type {?} */ dialogRef = this._dialogService.open(TdConfirmDialogComponent, dialogConfig);
        var /** @type {?} */ confirmDialogComponent = dialogRef.componentInstance;
        confirmDialogComponent.title = config.title;
        confirmDialogComponent.message = config.message;
        if (config.acceptButton) {
            confirmDialogComponent.acceptButton = config.acceptButton;
        }
        if (config.cancelButton) {
            confirmDialogComponent.cancelButton = config.cancelButton;
        }
        return dialogRef;
    };
    /**
     * params:
     * - config: IPromptConfig {
     *     message: string;
     *     title?: string;
     *     value?: string;
     *     viewContainerRef?: ViewContainerRef;
     *     acceptButton?: string;
     *     cancelButton?: string;
     * }
     *
     * Opens a prompt dialog with the provided config.
     * Returns an MatDialogRef<TdPromptDialogComponent> object.
     * @param {?} config
     * @return {?}
     */
    TdDialogService.prototype.openPrompt = function (config) {
        var /** @type {?} */ dialogConfig = this._createConfig(config);
        var /** @type {?} */ dialogRef = this._dialogService.open(TdPromptDialogComponent, dialogConfig);
        var /** @type {?} */ promptDialogComponent = dialogRef.componentInstance;
        promptDialogComponent.title = config.title;
        promptDialogComponent.message = config.message;
        promptDialogComponent.value = config.value;
        if (config.acceptButton) {
            promptDialogComponent.acceptButton = config.acceptButton;
        }
        if (config.cancelButton) {
            promptDialogComponent.cancelButton = config.cancelButton;
        }
        return dialogRef;
    };
    /**
     * @param {?} config
     * @return {?}
     */
    TdDialogService.prototype._createConfig = function (config) {
        var /** @type {?} */ dialogConfig = new dialog.MatDialogConfig();
        dialogConfig.width = '400px';
        Object.assign(dialogConfig, config);
        return dialogConfig;
    };
    return TdDialogService;
}());
TdDialogService.decorators = [
    { type: core.Injectable },
];
/** @nocollapse */
TdDialogService.ctorParameters = function () { return [
    { type: dialog.MatDialog, },
]; };
/**
 * @param {?} parent
 * @param {?} dialog
 * @return {?}
 */
function DIALOG_PROVIDER_FACTORY(parent, dialog$$1) {
    return parent || new TdDialogService(dialog$$1);
}
var DIALOG_PROVIDER = {
    // If there is already service available, use that. Otherwise, provide a new one.
    provide: TdDialogService,
    deps: [[new core.Optional(), new core.SkipSelf(), TdDialogService], dialog.MatDialog],
    useFactory: DIALOG_PROVIDER_FACTORY,
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_DIALOGS = [
    TdAlertDialogComponent,
    TdConfirmDialogComponent,
    TdPromptDialogComponent,
    TdDialogComponent,
    TdDialogTitleDirective,
    TdDialogActionsDirective,
    TdDialogContentDirective,
];
var TD_DIALOGS_ENTRY_COMPONENTS = [
    TdAlertDialogComponent,
    TdConfirmDialogComponent,
    TdPromptDialogComponent,
];
var CovalentDialogsModule = /** @class */ (function () {
    function CovalentDialogsModule() {
    }
    return CovalentDialogsModule;
}());
CovalentDialogsModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    forms.FormsModule,
                    common.CommonModule,
                    dialog.MatDialogModule,
                    input.MatInputModule,
                    button.MatButtonModule,
                ],
                declarations: [
                    TD_DIALOGS,
                ],
                exports: [
                    TD_DIALOGS,
                ],
                providers: [
                    DIALOG_PROVIDER,
                ],
                entryComponents: [
                    TD_DIALOGS_ENTRY_COMPONENTS,
                ],
            },] },
];
/** @nocollapse */
CovalentDialogsModule.ctorParameters = function () { return []; };

exports.CovalentDialogsModule = CovalentDialogsModule;
exports.TdDialogTitleDirective = TdDialogTitleDirective;
exports.TdDialogContentDirective = TdDialogContentDirective;
exports.TdDialogActionsDirective = TdDialogActionsDirective;
exports.TdDialogComponent = TdDialogComponent;
exports.TdAlertDialogComponent = TdAlertDialogComponent;
exports.TdConfirmDialogComponent = TdConfirmDialogComponent;
exports.TdPromptDialogComponent = TdPromptDialogComponent;
exports.TdDialogService = TdDialogService;
exports.DIALOG_PROVIDER_FACTORY = DIALOG_PROVIDER_FACTORY;
exports.DIALOG_PROVIDER = DIALOG_PROVIDER;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-dialogs.umd.js.map
