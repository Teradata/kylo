"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// Imports
var core_1 = require("@angular/core");
var forms_1 = require("@angular/forms");
var CodeMirror = require("codemirror");
/**
 * CodeMirror component
 * Usage :
 * <codemirror [(ngModel)]="data" [config]="{...}"></codemirror>
 */
var CodemirrorComponent = (function () {
    /**
     * Constructor
     */
    function CodemirrorComponent() {
        this.change = new core_1.EventEmitter();
        this.focus = new core_1.EventEmitter();
        this.blur = new core_1.EventEmitter();
        this.cursorActivity = new core_1.EventEmitter();
        this.instance = null;
        this._value = '';
    }
    Object.defineProperty(CodemirrorComponent.prototype, "value", {
        get: function () { return this._value; },
        set: function (v) {
            if (v !== this._value) {
                this._value = v;
                this.onChange(v);
            }
        },
        enumerable: true,
        configurable: true
    });
    /**
     * On component destroy
     */
    CodemirrorComponent.prototype.ngOnDestroy = function () {
    };
    /**
     * On component view init
     */
    CodemirrorComponent.prototype.ngAfterViewInit = function () {
        this.config = this.config || {};
        this.codemirrorInit(this.config);
    };
    /**
     * Initialize codemirror
     */
    CodemirrorComponent.prototype.codemirrorInit = function (config) {
        var _this = this;
        this.instance = CodeMirror.fromTextArea(this.host.nativeElement, config);
        this.instance.setValue(this._value);
        this.instance.on('change', function () {
            _this.updateValue(_this.instance.getValue());
        });
        this.instance.on('focus', function (instance, event) {
            _this.focus.emit({ instance: instance, event: event });
        });
        this.instance.on('cursorActivity', function (instance) {
            _this.cursorActivity.emit({ instance: instance });
        });
        this.instance.on('blur', function (instance, event) {
            _this.blur.emit({ instance: instance, event: event });
        });
    };
    /**
     * Value update process
     */
    CodemirrorComponent.prototype.updateValue = function (value) {
        this.value = value;
        this.onTouched();
        this.change.emit(value);
    };
    /**
     * Implements ControlValueAccessor
     */
    CodemirrorComponent.prototype.writeValue = function (value) {
        this._value = value || '';
        if (this.instance) {
            this.instance.setValue(this._value);
        }
    };
    CodemirrorComponent.prototype.onChange = function (_) { };
    CodemirrorComponent.prototype.onTouched = function () { };
    CodemirrorComponent.prototype.registerOnChange = function (fn) { this.onChange = fn; };
    CodemirrorComponent.prototype.registerOnTouched = function (fn) { this.onTouched = fn; };
    return CodemirrorComponent;
}());
CodemirrorComponent.decorators = [
    { type: core_1.Component, args: [{
                selector: 'codemirror',
                providers: [
                    {
                        provide: forms_1.NG_VALUE_ACCESSOR,
                        useExisting: core_1.forwardRef(function () { return CodemirrorComponent; }),
                        multi: true
                    }
                ],
                template: "<textarea #host></textarea>",
            },] },
];
/** @nocollapse */
CodemirrorComponent.ctorParameters = function () { return []; };
CodemirrorComponent.propDecorators = {
    'config': [{ type: core_1.Input },],
    'change': [{ type: core_1.Output },],
    'focus': [{ type: core_1.Output },],
    'blur': [{ type: core_1.Output },],
    'cursorActivity': [{ type: core_1.Output },],
    'host': [{ type: core_1.ViewChild, args: ['host',] },],
    'instance': [{ type: core_1.Output },],
    'value': [{ type: core_1.Input },],
};
exports.CodemirrorComponent = CodemirrorComponent;
//# sourceMappingURL=codemirror.component.js.map