(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/coercion'), require('@angular/forms'), require('@covalent/core/common'), require('@angular/cdk/portal'), require('rxjs/Observable'), require('rxjs/Subject'), require('@angular/common'), require('@angular/material/icon'), require('@angular/material/button')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/coercion', '@angular/forms', '@covalent/core/common', '@angular/cdk/portal', 'rxjs/Observable', 'rxjs/Subject', '@angular/common', '@angular/material/icon', '@angular/material/button'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.file = {}),global.ng.core,global.ng.cdk.coercion,global.ng.forms,global.covalent.core.common,global.ng.cdk.portal,global.Rx,global.Rx,global.ng.common,global.ng.material.icon,global.ng.material.button));
}(this, (function (exports,core,coercion,forms,common,portal,Observable,Subject,common$1,icon,button) { 'use strict';

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
var TdFileSelectDirective = /** @class */ (function () {
    /**
     * @param {?} model
     */
    function TdFileSelectDirective(model) {
        this.model = model;
        this._multiple = false;
        /**
         * fileSelect?: function
         * Event emitted when a file or files are selected in host [HTMLInputElement].
         * Emits a [FileList | File] object.
         * Alternative to not use [(ngModel)].
         */
        this.onFileSelect = new core.EventEmitter();
    }
    Object.defineProperty(TdFileSelectDirective.prototype, "multiple", {
        /**
         * multiple?: boolean
         * Sets whether multiple files can be selected at once in host element, or just a single file.
         * Can also be 'multiple' native attribute.
         * @param {?} multiple
         * @return {?}
         */
        set: function (multiple) {
            this._multiple = coercion.coerceBooleanProperty(multiple);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdFileSelectDirective.prototype, "multipleBinding", {
        /**
         * Binds native 'multiple' attribute if [multiple] property is 'true'.
         * @return {?}
         */
        get: function () {
            return this._multiple ? '' : undefined;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Listens to 'change' host event to get [HTMLInputElement] files.
     * Emits the 'onFileSelect' event with a [FileList] or [File] depending if 'multiple' attr exists in host.
     * Uses [(ngModel)] if declared, instead of emitting 'onFileSelect' event.
     * @param {?} event
     * @return {?}
     */
    TdFileSelectDirective.prototype.onChange = function (event) {
        if (event.target instanceof HTMLInputElement) {
            var /** @type {?} */ fileInputEl = ((event.target));
            var /** @type {?} */ files = fileInputEl.files;
            if (files.length) {
                var /** @type {?} */ value = this._multiple ? (files.length > 1 ? files : files[0]) : files[0];
                this.model ? this.model.update.emit(value) : this.onFileSelect.emit(value);
            }
        }
    };
    return TdFileSelectDirective;
}());
TdFileSelectDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[tdFileSelect]',
            },] },
];
/** @nocollapse */
TdFileSelectDirective.ctorParameters = function () { return [
    { type: forms.NgModel, decorators: [{ type: core.Optional }, { type: core.Host },] },
]; };
TdFileSelectDirective.propDecorators = {
    "multiple": [{ type: core.Input, args: ['multiple',] },],
    "onFileSelect": [{ type: core.Output, args: ['fileSelect',] },],
    "multipleBinding": [{ type: core.HostBinding, args: ['attr.multiple',] },],
    "onChange": [{ type: core.HostListener, args: ['change', ['$event'],] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdFileDropBase = /** @class */ (function () {
    function TdFileDropBase() {
    }
    return TdFileDropBase;
}());
/* tslint:disable-next-line */
var _TdFileDropMixinBase = common.mixinDisabled(TdFileDropBase);
var TdFileDropDirective = /** @class */ (function (_super) {
    __extends(TdFileDropDirective, _super);
    /**
     * @param {?} _renderer
     * @param {?} _element
     */
    function TdFileDropDirective(_renderer, _element) {
        var _this = _super.call(this) || this;
        _this._renderer = _renderer;
        _this._element = _element;
        _this._multiple = false;
        /**
         * fileDrop?: function
         * Event emitted when a file or files are dropped in host element after being validated.
         * Emits a [FileList | File] object.
         */
        _this.onFileDrop = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdFileDropDirective.prototype, "multiple", {
        /**
         * multiple?: boolean
         * Sets whether multiple files can be dropped at once in host element, or just a single file.
         * Can also be 'multiple' native attribute.
         * @param {?} multiple
         * @return {?}
         */
        set: function (multiple) {
            this._multiple = coercion.coerceBooleanProperty(multiple);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdFileDropDirective.prototype, "multipleBinding", {
        /**
         * Binds native 'multiple' attribute if [multiple] property is 'true'.
         * @return {?}
         */
        get: function () {
            return this._multiple ? '' : undefined;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdFileDropDirective.prototype, "disabledBinding", {
        /**
         * Binds native 'disabled' attribute if [disabled] property is 'true'.
         * @return {?}
         */
        get: function () {
            return this.disabled ? '' : undefined;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Listens to 'drop' host event to get validated transfer items.
     * Emits the 'onFileDrop' event with a [FileList] or [File] depending if 'multiple' attr exists in host.
     * Stops event propagation and default action from browser for 'drop' event.
     * @param {?} event
     * @return {?}
     */
    TdFileDropDirective.prototype.onDrop = function (event) {
        if (!this.disabled) {
            var /** @type {?} */ transfer = ((event)).dataTransfer;
            var /** @type {?} */ files = transfer.files;
            if (files.length) {
                var /** @type {?} */ value = this._multiple ? (files.length > 1 ? files : files[0]) : files[0];
                this.onFileDrop.emit(value);
            }
        }
        this._renderer.removeClass(this._element.nativeElement, 'drop-zone');
        this._stopEvent(event);
    };
    /**
     * Listens to 'dragover' host event to validate transfer items.
     * Checks if 'multiple' attr exists in host to allow multiple file drops.
     * Stops event propagation and default action from browser for 'dragover' event.
     * @param {?} event
     * @return {?}
     */
    TdFileDropDirective.prototype.onDragOver = function (event) {
        var /** @type {?} */ transfer = ((event)).dataTransfer;
        transfer.dropEffect = this._typeCheck(transfer.types);
        if (this.disabled || (!this._multiple &&
            ((transfer.items && transfer.items.length > 1) || ((transfer)).mozItemCount > 1))) {
            transfer.dropEffect = 'none';
        }
        else {
            transfer.dropEffect = 'copy';
        }
        this._stopEvent(event);
    };
    /**
     * Listens to 'dragenter' host event to add animation class 'drop-zone' which can be overriden in host.
     * Stops event propagation and default action from browser for 'dragenter' event.
     * @param {?} event
     * @return {?}
     */
    TdFileDropDirective.prototype.onDragEnter = function (event) {
        if (!this.disabled) {
            this._renderer.addClass(this._element.nativeElement, 'drop-zone');
        }
        this._stopEvent(event);
    };
    /**
     * Listens to 'dragleave' host event to remove animation class 'drop-zone'.
     * Stops event propagation and default action from browser for 'dragleave' event.
     * @param {?} event
     * @return {?}
     */
    TdFileDropDirective.prototype.onDragLeave = function (event) {
        this._renderer.removeClass(this._element.nativeElement, 'drop-zone');
        this._stopEvent(event);
    };
    /**
     * Validates if the transfer item types are 'Files'.
     * @param {?} types
     * @return {?}
     */
    TdFileDropDirective.prototype._typeCheck = function (types) {
        var /** @type {?} */ dropEffect = 'none';
        if (types) {
            if ((((types)).contains && ((types)).contains('Files'))
                || (((types)).indexOf && ((types)).indexOf('Files') !== -1)) {
                dropEffect = 'copy';
            }
        }
        return dropEffect;
    };
    /**
     * @param {?} event
     * @return {?}
     */
    TdFileDropDirective.prototype._stopEvent = function (event) {
        event.preventDefault();
        event.stopPropagation();
    };
    return TdFileDropDirective;
}(_TdFileDropMixinBase));
TdFileDropDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[tdFileDrop]',
                inputs: ['disabled'],
            },] },
];
/** @nocollapse */
TdFileDropDirective.ctorParameters = function () { return [
    { type: core.Renderer2, },
    { type: core.ElementRef, },
]; };
TdFileDropDirective.propDecorators = {
    "multiple": [{ type: core.Input, args: ['multiple',] },],
    "onFileDrop": [{ type: core.Output, args: ['fileDrop',] },],
    "multipleBinding": [{ type: core.HostBinding, args: ['attr.multiple',] },],
    "disabledBinding": [{ type: core.HostBinding, args: ['attr.disabled',] },],
    "onDrop": [{ type: core.HostListener, args: ['drop', ['$event'],] },],
    "onDragOver": [{ type: core.HostListener, args: ['dragover', ['$event'],] },],
    "onDragEnter": [{ type: core.HostListener, args: ['dragenter', ['$event'],] },],
    "onDragLeave": [{ type: core.HostListener, args: ['dragleave', ['$event'],] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdFileInputLabelDirective = /** @class */ (function (_super) {
    __extends(TdFileInputLabelDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdFileInputLabelDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdFileInputLabelDirective;
}(portal.TemplatePortalDirective));
TdFileInputLabelDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-file-input-label]ng-template',
            },] },
];
/** @nocollapse */
TdFileInputLabelDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdFileInputBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdFileInputBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdFileInputBase;
}());
/* tslint:disable-next-line */
var _TdFileInputMixinBase = common.mixinControlValueAccessor(common.mixinDisabled(TdFileInputBase));
var TdFileInputComponent = /** @class */ (function (_super) {
    __extends(TdFileInputComponent, _super);
    /**
     * @param {?} _renderer
     * @param {?} _changeDetectorRef
     */
    function TdFileInputComponent(_renderer, _changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._renderer = _renderer;
        _this._multiple = false;
        /**
         * select?: function
         * Event emitted a file is selected
         * Emits a [File | FileList] object.
         */
        _this.onSelect = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdFileInputComponent.prototype, "inputElement", {
        /**
         * @return {?}
         */
        get: function () {
            return this._inputElement.nativeElement;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdFileInputComponent.prototype, "multiple", {
        /**
         * @return {?}
         */
        get: function () {
            return this._multiple;
        },
        /**
         * multiple?: boolean
         * Sets if multiple files can be dropped/selected at once in [TdFileInputComponent].
         * @param {?} multiple
         * @return {?}
         */
        set: function (multiple) {
            this._multiple = coercion.coerceBooleanProperty(multiple);
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Method executed when a file is selected.
     * @param {?} files
     * @return {?}
     */
    TdFileInputComponent.prototype.handleSelect = function (files) {
        this.writeValue(files);
        this.onSelect.emit(files);
    };
    /**
     * Used to clear the selected files from the [TdFileInputComponent].
     * @return {?}
     */
    TdFileInputComponent.prototype.clear = function () {
        this.writeValue(undefined);
        this._renderer.setProperty(this.inputElement, 'value', '');
    };
    /**
     * Method executed when the disabled value changes
     * @param {?} v
     * @return {?}
     */
    TdFileInputComponent.prototype.onDisabledChange = function (v) {
        if (v) {
            this.clear();
        }
    };
    return TdFileInputComponent;
}(_TdFileInputMixinBase));
TdFileInputComponent.decorators = [
    { type: core.Component, args: [{
                changeDetection: core.ChangeDetectionStrategy.OnPush,
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdFileInputComponent; }),
                        multi: true,
                    }],
                selector: 'td-file-input',
                inputs: ['disabled', 'value'],
                styles: [":host{ }\n  :host .td-file-input{\n    padding-left:8px;\n    padding-right:8px; }\n  :host input.td-file-input-hidden{\n    display:none; }\n  :host .drop-zone{\n    border-radius:3px; }\n    :host .drop-zone *{\n      pointer-events:none; }\n"],
                template: "<div>\n  <button mat-raised-button\n          class=\"td-file-input\"\n          type=\"button\"\n          [color]=\"color\"\n          [multiple]=\"multiple\"\n          [disabled]=\"disabled\"\n          (keyup.enter)=\"fileInput.click()\"\n          (click)=\"fileInput.click()\"\n          (fileDrop)=\"handleSelect($event)\"\n          tdFileDrop>\n    <ng-content></ng-content>\n  </button>\n  <input #fileInput\n          class=\"td-file-input-hidden\"\n          type=\"file\"\n          [attr.accept]=\"accept\"\n          (fileSelect)=\"handleSelect($event)\"\n          [multiple]=\"multiple\"\n          [disabled]=\"disabled\"\n          tdFileSelect>\n</div>",
            },] },
];
/** @nocollapse */
TdFileInputComponent.ctorParameters = function () { return [
    { type: core.Renderer2, },
    { type: core.ChangeDetectorRef, },
]; };
TdFileInputComponent.propDecorators = {
    "_inputElement": [{ type: core.ViewChild, args: ['fileInput',] },],
    "color": [{ type: core.Input, args: ['color',] },],
    "multiple": [{ type: core.Input, args: ['multiple',] },],
    "accept": [{ type: core.Input, args: ['accept',] },],
    "onSelect": [{ type: core.Output, args: ['select',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdFileUploadBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdFileUploadBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdFileUploadBase;
}());
/* tslint:disable-next-line */
var _TdFileUploadMixinBase = common.mixinControlValueAccessor(common.mixinDisabled(TdFileUploadBase));
var TdFileUploadComponent = /** @class */ (function (_super) {
    __extends(TdFileUploadComponent, _super);
    /**
     * @param {?} _changeDetectorRef
     */
    function TdFileUploadComponent(_changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._multiple = false;
        _this._required = false;
        /**
         * defaultColor?: string
         * Sets browse button color. Uses same color palette accepted as [MatButton] and defaults to 'primary'.
         */
        _this.defaultColor = 'primary';
        /**
         * activeColor?: string
         * Sets upload button color. Uses same color palette accepted as [MatButton] and defaults to 'accent'.
         */
        _this.activeColor = 'accent';
        /**
         * cancelColor?: string
         * Sets cancel button color. Uses same color palette accepted as [MatButton] and defaults to 'warn'.
         */
        _this.cancelColor = 'warn';
        /**
         * select?: function
         * Event emitted when a file is selected.
         * Emits a [File | FileList] object.
         */
        _this.onSelect = new core.EventEmitter();
        /**
         * upload?: function
         * Event emitted when upload button is clicked.
         * Emits a [File | FileList] object.
         */
        _this.onUpload = new core.EventEmitter();
        /**
         * cancel?: function
         * Event emitted when cancel button is clicked.
         */
        _this.onCancel = new core.EventEmitter();
        return _this;
    }
    Object.defineProperty(TdFileUploadComponent.prototype, "multiple", {
        /**
         * @return {?}
         */
        get: function () {
            return this._multiple;
        },
        /**
         * multiple?: boolean
         * Sets if multiple files can be dropped/selected at once in [TdFileUploadComponent].
         * @param {?} multiple
         * @return {?}
         */
        set: function (multiple) {
            this._multiple = coercion.coerceBooleanProperty(multiple);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdFileUploadComponent.prototype, "required", {
        /**
         * @return {?}
         */
        get: function () {
            return this._required;
        },
        /**
         * required?: boolean
         * Forces at least one file upload.
         * Defaults to 'false'
         * @param {?} required
         * @return {?}
         */
        set: function (required) {
            this._required = coercion.coerceBooleanProperty(required);
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Method executed when upload button is clicked.
     * @return {?}
     */
    TdFileUploadComponent.prototype.uploadPressed = function () {
        if (this.value) {
            this.onUpload.emit(this.value);
        }
    };
    /**
     * Method executed when a file is selected.
     * @param {?} value
     * @return {?}
     */
    TdFileUploadComponent.prototype.handleSelect = function (value) {
        this.value = value;
        this.onSelect.emit(value);
    };
    /**
     * Methods executed when cancel button is clicked.
     * Clears files.
     * @return {?}
     */
    TdFileUploadComponent.prototype.cancel = function () {
        this.value = undefined;
        this.onCancel.emit(undefined);
        // check if the file input is rendered before clearing it
        if (this.fileInput) {
            this.fileInput.clear();
        }
    };
    /**
     * Method executed when the disabled value changes
     * @param {?} v
     * @return {?}
     */
    TdFileUploadComponent.prototype.onDisabledChange = function (v) {
        if (v) {
            this.cancel();
        }
    };
    return TdFileUploadComponent;
}(_TdFileUploadMixinBase));
TdFileUploadComponent.decorators = [
    { type: core.Component, args: [{
                changeDetection: core.ChangeDetectionStrategy.OnPush,
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdFileUploadComponent; }),
                        multi: true,
                    }],
                selector: 'td-file-upload',
                inputs: ['disabled', 'value'],
                styles: [".td-file-upload{\n  padding-left:8px;\n  padding-right:8px; }\n.td-file-upload-cancel{\n  height:24px;\n  width:24px;\n  position:relative;\n  top:24px;\n  left:-12px; }\n  ::ng-deep [dir='rtl'] .td-file-upload-cancel{\n    right:-12px;\n    left:0; }\n  .td-file-upload-cancel mat-icon{\n    border-radius:12px;\n    vertical-align:baseline; }\n.drop-zone{\n  border-radius:3px; }\n  .drop-zone *{\n    pointer-events:none; }\n"],
                template: "<td-file-input *ngIf=\"!value\"\n               [(ngModel)]=\"value\"\n               [multiple]=\"multiple\"\n               [disabled]=\"disabled\"\n               [accept]=\"accept\"\n               [color]=\"defaultColor\"\n               (select)=\"handleSelect($event)\">\n  <ng-template [cdkPortalHost]=\"inputLabel\" [ngIf]=\"true\"></ng-template>\n</td-file-input>\n<div *ngIf=\"value\">\n  <button #fileUpload\n          class=\"td-file-upload\"\n          mat-raised-button\n          type=\"button\"\n          [color]=\"activeColor\"\n          (keyup.delete)=\"cancel()\"\n          (keyup.backspace)=\"cancel()\"\n          (keyup.escape)=\"cancel()\"\n          (click)=\"uploadPressed()\">\n    <ng-content></ng-content>\n  </button>\n  <button mat-icon-button\n          type=\"button\"\n          class=\"td-file-upload-cancel\"\n          [color]=\"cancelColor\"\n          (click)=\"cancel()\">\n    <mat-icon>cancel</mat-icon>\n  </button>\n</div>",
            },] },
];
/** @nocollapse */
TdFileUploadComponent.ctorParameters = function () { return [
    { type: core.ChangeDetectorRef, },
]; };
TdFileUploadComponent.propDecorators = {
    "fileInput": [{ type: core.ViewChild, args: [TdFileInputComponent,] },],
    "inputLabel": [{ type: core.ContentChild, args: [TdFileInputLabelDirective,] },],
    "defaultColor": [{ type: core.Input, args: ['defaultColor',] },],
    "activeColor": [{ type: core.Input, args: ['activeColor',] },],
    "cancelColor": [{ type: core.Input, args: ['cancelColor',] },],
    "multiple": [{ type: core.Input, args: ['multiple',] },],
    "required": [{ type: core.Input, args: ['required',] },],
    "accept": [{ type: core.Input, args: ['accept',] },],
    "onSelect": [{ type: core.Output, args: ['select',] },],
    "onUpload": [{ type: core.Output, args: ['upload',] },],
    "onCancel": [{ type: core.Output, args: ['cancel',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * @record
 */
var TdFileService = /** @class */ (function () {
    function TdFileService() {
        this._progressSubject = new Subject.Subject();
        this._progressObservable = this._progressSubject.asObservable();
    }
    Object.defineProperty(TdFileService.prototype, "progress", {
        /**
         * Gets progress observable to keep track of the files being uploaded.
         * Needs to be supported by backend.
         * @return {?}
         */
        get: function () {
            return this._progressObservable;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * params:
     * - options: IUploadOptions {
     *     url: string,
     *     method: 'post' | 'put',
     *     file?: File,
     *     headers?: {[key: string]: string},
     *     formData?: FormData
     * }
     *
     * Uses underlying [XMLHttpRequest] to upload a file to a url.
     * Will be depricated when angular fixes [Http] to allow [FormData] as body.
     * @param {?} options
     * @return {?}
     */
    TdFileService.prototype.upload = function (options) {
        var _this = this;
        return new Observable.Observable(function (subscriber) {
            var /** @type {?} */ xhr = new XMLHttpRequest();
            var /** @type {?} */ formData = new FormData();
            if (options.file !== undefined) {
                formData.append('file', options.file);
            }
            else if (options.formData !== undefined) {
                formData = options.formData;
            }
            else {
                return subscriber.error('For [IUploadOptions] you have to set either the [file] or the [formData] property.');
            }
            xhr.upload.onprogress = function (event) {
                var /** @type {?} */ progress = 0;
                if (event.lengthComputable) {
                    progress = Math.round(event.loaded / event.total * 100);
                }
                _this._progressSubject.next(progress);
            };
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        subscriber.next(xhr.response);
                        subscriber.complete();
                    }
                    else {
                        subscriber.error(xhr.response);
                    }
                }
            };
            xhr.open(options.method, options.url, true);
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            if (options.headers) {
                for (var /** @type {?} */ key in options.headers) {
                    xhr.setRequestHeader(key, options.headers[key]);
                }
            }
            xhr.send(formData);
        });
    };
    return TdFileService;
}());
TdFileService.decorators = [
    { type: core.Injectable },
];
/** @nocollapse */
TdFileService.ctorParameters = function () { return []; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_FILE = [
    TdFileSelectDirective,
    TdFileDropDirective,
    TdFileUploadComponent,
    TdFileInputComponent,
    TdFileInputLabelDirective,
];
var CovalentFileModule = /** @class */ (function () {
    function CovalentFileModule() {
    }
    return CovalentFileModule;
}());
CovalentFileModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    forms.FormsModule,
                    common$1.CommonModule,
                    icon.MatIconModule,
                    button.MatButtonModule,
                    portal.PortalModule,
                ],
                declarations: [
                    TD_FILE,
                ],
                exports: [
                    TD_FILE,
                ],
                providers: [
                    TdFileService,
                ],
            },] },
];
/** @nocollapse */
CovalentFileModule.ctorParameters = function () { return []; };

exports.CovalentFileModule = CovalentFileModule;
exports.TdFileDropBase = TdFileDropBase;
exports._TdFileDropMixinBase = _TdFileDropMixinBase;
exports.TdFileDropDirective = TdFileDropDirective;
exports.TdFileSelectDirective = TdFileSelectDirective;
exports.TdFileInputLabelDirective = TdFileInputLabelDirective;
exports.TdFileInputBase = TdFileInputBase;
exports._TdFileInputMixinBase = _TdFileInputMixinBase;
exports.TdFileInputComponent = TdFileInputComponent;
exports.TdFileUploadBase = TdFileUploadBase;
exports._TdFileUploadMixinBase = _TdFileUploadMixinBase;
exports.TdFileUploadComponent = TdFileUploadComponent;
exports.TdFileService = TdFileService;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-file.umd.js.map
