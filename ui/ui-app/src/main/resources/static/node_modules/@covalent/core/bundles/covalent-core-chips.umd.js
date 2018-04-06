(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/platform-browser'), require('@angular/forms'), require('@angular/cdk/portal'), require('@angular/cdk/coercion'), require('@angular/cdk/keycodes'), require('@angular/material/chips'), require('@angular/material/input'), require('@angular/material/core'), require('@angular/material/autocomplete'), require('rxjs/observable/timer'), require('rxjs/observable/merge'), require('rxjs/operator/toPromise'), require('rxjs/observable/fromEvent'), require('rxjs/operators/filter'), require('rxjs/operators/debounceTime'), require('@covalent/core/common'), require('@angular/common'), require('@angular/material/icon')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/platform-browser', '@angular/forms', '@angular/cdk/portal', '@angular/cdk/coercion', '@angular/cdk/keycodes', '@angular/material/chips', '@angular/material/input', '@angular/material/core', '@angular/material/autocomplete', 'rxjs/observable/timer', 'rxjs/observable/merge', 'rxjs/operator/toPromise', 'rxjs/observable/fromEvent', 'rxjs/operators/filter', 'rxjs/operators/debounceTime', '@covalent/core/common', '@angular/common', '@angular/material/icon'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core.chips = {}),global.ng.core,global.ng.platformBrowser,global.ng.forms,global.ng.cdk.portal,global.ng.cdk.coercion,global.ng.cdk.keycodes,global.ng.material.chips,global.ng.material.input,global.ng.material.core,global.ng.material.autocomplete,global.Rx.Observable,global.Rx.Observable,global.Rx.Observable.prototype,global.Rx.Observable,global.Rx.Observable.prototype,global.Rx.Observable.prototype,global.covalent.core.common,global.ng.common,global.ng.material.icon));
}(this, (function (exports,core,platformBrowser,forms,portal,coercion,keycodes,chips,input,core$1,autocomplete,timer,merge,toPromise,fromEvent,filter,debounceTime,common,common$1,icon) { 'use strict';

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
var TdChipDirective = /** @class */ (function (_super) {
    __extends(TdChipDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdChipDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdChipDirective;
}(portal.TemplatePortalDirective));
TdChipDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-chip]ng-template',
            },] },
];
/** @nocollapse */
TdChipDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdAutocompleteOptionDirective = /** @class */ (function (_super) {
    __extends(TdAutocompleteOptionDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdAutocompleteOptionDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdAutocompleteOptionDirective;
}(portal.TemplatePortalDirective));
TdAutocompleteOptionDirective.decorators = [
    { type: core.Directive, args: [{
                selector: '[td-autocomplete-option]ng-template',
            },] },
];
/** @nocollapse */
TdAutocompleteOptionDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
var TdChipsBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdChipsBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdChipsBase;
}());
/* tslint:disable-next-line */
var _TdChipsMixinBase = common.mixinControlValueAccessor(common.mixinDisabled(TdChipsBase), []);
var TdChipsComponent = /** @class */ (function (_super) {
    __extends(TdChipsComponent, _super);
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     * @param {?} _document
     * @param {?} _changeDetectorRef
     */
    function TdChipsComponent(_elementRef, _renderer, _document, _changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._elementRef = _elementRef;
        _this._renderer = _renderer;
        _this._document = _document;
        _this._isMousedown = false;
        _this._length = 0;
        _this._stacked = false;
        _this._requireMatch = false;
        _this._color = 'primary';
        _this._inputPosition = 'after';
        _this._chipAddition = true;
        _this._chipRemoval = true;
        _this._focused = false;
        _this._tabIndex = 0;
        _this._internalClick = false;
        _this._internalActivateOption = false;
        /**
         * FormControl for the matInput element.
         */
        _this.inputControl = new forms.FormControl();
        /**
         * debounce?: number
         * Debounce timeout between keypresses. Defaults to 200.
         */
        _this.debounce = 200;
        /**
         * add?: function
         * Method to be executed when a chip is added.
         * Sends chip value as event.
         */
        _this.onAdd = new core.EventEmitter();
        /**
         * remove?: function
         * Method to be executed when a chip is removed.
         * Sends chip value as event.
         */
        _this.onRemove = new core.EventEmitter();
        /**
         * inputChange?: function
         * Method to be executed when the value in the autocomplete input changes.
         * Sends string value as event.
         */
        _this.onInputChange = new core.EventEmitter();
        /**
         * chipFocus?: function
         * Method to be executed when a chip is focused.
         * Sends chip value as event.
         */
        _this.onChipFocus = new core.EventEmitter();
        /**
         * blur?: function
         * Method to be executed when a chip is blurred.
         * Sends chip value as event.
         */
        _this.onChipBlur = new core.EventEmitter();
        _this._renderer.addClass(_this._elementRef.nativeElement, 'mat-' + _this._color);
        return _this;
    }
    Object.defineProperty(TdChipsComponent.prototype, "focused", {
        /**
         * Flag that is true when autocomplete is focused.
         * @return {?}
         */
        get: function () {
            return this._focused;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "items", {
        /**
         * @return {?}
         */
        get: function () {
            return this._items;
        },
        /**
         * items?: any[]
         * Renders the `mat-autocomplete` with the provided list to display as options.
         * @param {?} items
         * @return {?}
         */
        set: function (items) {
            this._items = items;
            this._setFirstOptionActive();
            this._changeDetectorRef.markForCheck();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "stacked", {
        /**
         * @return {?}
         */
        get: function () {
            return this._stacked;
        },
        /**
         * stacked?: boolean
         * Set stacked or horizontal chips depending on value.
         * Defaults to false.
         * @param {?} stacked
         * @return {?}
         */
        set: function (stacked) {
            this._stacked = coercion.coerceBooleanProperty(stacked);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "inputPosition", {
        /**
         * @return {?}
         */
        get: function () {
            return this._inputPosition;
        },
        /**
         * inputPosition?: 'before' | 'after'
         * Set input position before or after the chips.
         * Defaults to 'after'.
         * @param {?} inputPosition
         * @return {?}
         */
        set: function (inputPosition) {
            this._inputPosition = inputPosition;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "requireMatch", {
        /**
         * @return {?}
         */
        get: function () {
            return this._requireMatch;
        },
        /**
         * requireMatch?: boolean
         * Blocks custom inputs and only allows selections from the autocomplete list.
         * @param {?} requireMatch
         * @return {?}
         */
        set: function (requireMatch) {
            this._requireMatch = coercion.coerceBooleanProperty(requireMatch);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "chipAddition", {
        /**
         * @return {?}
         */
        get: function () {
            return this._chipAddition;
        },
        /**
         * chipAddition?: boolean
         * Disables the ability to add chips. When setting disabled as true, this will be overriden.
         * Defaults to true.
         * @param {?} chipAddition
         * @return {?}
         */
        set: function (chipAddition) {
            this._chipAddition = chipAddition;
            this._toggleInput();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "canAddChip", {
        /**
         * Checks if not in disabled state and if chipAddition is set to 'true'
         * States if a chip can be added and if the input is available
         * @return {?}
         */
        get: function () {
            return this.chipAddition && !this.disabled;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "chipRemoval", {
        /**
         * @return {?}
         */
        get: function () {
            return this._chipRemoval;
        },
        /**
         * chipRemoval?: boolean
         * Disables the ability to remove chips. If it doesn't exist chip remmoval defaults to true.
         * When setting disabled as true, this will be overriden to false.
         * @param {?} chipRemoval
         * @return {?}
         */
        set: function (chipRemoval) {
            this._chipRemoval = chipRemoval;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "canRemoveChip", {
        /**
         * Checks if not in disabled state and if chipRemoval is set to 'true'
         * States if a chip can be removed
         * @return {?}
         */
        get: function () {
            return this.chipRemoval && !this.disabled;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "color", {
        /**
         * @return {?}
         */
        get: function () {
            return this._color;
        },
        /**
         * color?: 'primary' | 'accent' | 'warn'
         * Sets the color for the input and focus/selected state of the chips.
         * Defaults to 'primary'
         * @param {?} color
         * @return {?}
         */
        set: function (color) {
            if (color) {
                this._renderer.removeClass(this._elementRef.nativeElement, 'mat-' + this._color);
                this._color = color;
                this._renderer.addClass(this._elementRef.nativeElement, 'mat-' + this._color);
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdChipsComponent.prototype, "tabIndex", {
        /**
         * Hostbinding to set the a11y of the TdChipsComponent depending on its state
         * @return {?}
         */
        get: function () {
            return this.disabled ? -1 : this._tabIndex;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Listens to host focus event to act on it
     * @param {?} event
     * @return {?}
     */
    TdChipsComponent.prototype.focusListener = function (event) {
        // should only focus if its not via mousedown to prevent clashing with autocomplete
        if (!this._isMousedown) {
            this.focus();
        }
        event.preventDefault();
    };
    /**
     * Listens to host mousedown event to act on it
     * @param {?} event
     * @return {?}
     */
    TdChipsComponent.prototype.mousedownListener = function (event) {
        var _this = this;
        // sets a flag to know if there was a mousedown and then it returns it back to false
        this._isMousedown = true;
        toPromise.toPromise.call(timer.timer()).then(function () {
            _this._isMousedown = false;
        });
    };
    /**
     * If clicking on :host or `td-chips-wrapper`, then we stop the click propagation so the autocomplete
     * doesnt close automatically.
     * @param {?} event
     * @return {?}
     */
    TdChipsComponent.prototype.clickListener = function (event) {
        var /** @type {?} */ clickTarget = (event.target);
        if (clickTarget === this._elementRef.nativeElement ||
            clickTarget.className.indexOf('td-chips-wrapper') > -1) {
            this.focus();
            event.preventDefault();
            event.stopPropagation();
        }
    };
    /**
     * Listens to host keydown event to act on it depending on the keypress
     * @param {?} event
     * @return {?}
     */
    TdChipsComponent.prototype.keydownListener = function (event) {
        var _this = this;
        switch (event.keyCode) {
            case keycodes.TAB:
                // if tabing out, then unfocus the component
                toPromise.toPromise.call(timer.timer()).then(function () {
                    _this.removeFocusedState();
                });
                break;
            case keycodes.ESCAPE:
                if (this._inputChild.focused) {
                    this._nativeInput.nativeElement.blur();
                    this.removeFocusedState();
                    this._closeAutocomplete();
                }
                else {
                    this.focus();
                }
                break;
            default:
        }
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.inputControl.valueChanges.pipe(debounceTime.debounceTime(this.debounce)).subscribe(function (value) {
            _this.onInputChange.emit(value ? value : '');
        });
        this._changeDetectorRef.markForCheck();
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype.ngAfterViewInit = function () {
        this._watchOutsideClick();
        this._changeDetectorRef.markForCheck();
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype.ngDoCheck = function () {
        // Throw onChange event only if array changes size.
        if (this.value && this.value.length !== this._length) {
            this._length = this.value.length;
            this.onChange(this.value);
        }
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype.ngOnDestroy = function () {
        if (this._outsideClickSubs) {
            this._outsideClickSubs.unsubscribe();
            this._outsideClickSubs = undefined;
        }
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype._setInternalClick = function () {
        this._internalClick = true;
    };
    /**
     * Method executed when the disabled value changes
     * @param {?} v
     * @return {?}
     */
    TdChipsComponent.prototype.onDisabledChange = function (v) {
        this._toggleInput();
    };
    /**
     * Method that is executed when trying to create a new chip from the autocomplete.
     * It check if [requireMatch] is enabled, and tries to add the first active option
     * else if just adds the value thats on the input
     * returns 'true' if successful, 'false' if it fails.
     * @return {?}
     */
    TdChipsComponent.prototype._handleAddChip = function () {
        var /** @type {?} */ value;
        if (this.requireMatch) {
            var /** @type {?} */ selectedOptions = this._options.toArray().filter(function (option) {
                return option.active;
            });
            if (selectedOptions.length > 0) {
                value = selectedOptions[0].value;
                selectedOptions[0].setInactiveStyles();
            }
            if (!value) {
                return false;
            }
        }
        else {
            // if there is a selection, then use that
            // else use the input value as chip
            if (this._autocompleteTrigger.activeOption) {
                value = this._autocompleteTrigger.activeOption.value;
                this._autocompleteTrigger.activeOption.setInactiveStyles();
            }
            else {
                value = this._inputChild.value;
                if (value.trim() === '') {
                    return false;
                }
            }
        }
        return this.addChip(value);
    };
    /**
     * Method thats exectuted when trying to add a value as chip
     * returns 'true' if successful, 'false' if it fails.
     * @param {?} value
     * @return {?}
     */
    TdChipsComponent.prototype.addChip = function (value) {
        var _this = this;
        /**
             * add a debounce ms delay when reopening the autocomplete to give it time
             * to rerender the next list and at the correct spot
             */
        this._closeAutocomplete();
        toPromise.toPromise.call(timer.timer(this.debounce)).then(function () {
            _this.setFocusedState();
            _this._setFirstOptionActive();
            _this._openAutocomplete();
        });
        this.inputControl.setValue('');
        // check if value is already part of the model
        if (this.value.indexOf(value) > -1) {
            return false;
        }
        this.value.push(value);
        this.onAdd.emit(value);
        this.onChange(this.value);
        this._changeDetectorRef.markForCheck();
        return true;
    };
    /**
     * Method that is executed when trying to remove a chip.
     * returns 'true' if successful, 'false' if it fails.
     * @param {?} index
     * @return {?}
     */
    TdChipsComponent.prototype.removeChip = function (index) {
        var /** @type {?} */ removedValues = this.value.splice(index, 1);
        if (removedValues.length === 0) {
            return false;
        }
        /**
             * Checks if deleting last single chip, to focus input afterwards
             * Else check if its not the last chip of the list to focus the next one.
             */
        if (index === (this._totalChips - 1) && index === 0) {
            this._inputChild.focus();
        }
        else if (index < (this._totalChips - 1)) {
            this._focusChip(index + 1);
        }
        else if (index > 0) {
            this._focusChip(index - 1);
        }
        this.onRemove.emit(removedValues[0]);
        this.onChange(this.value);
        this.inputControl.setValue('');
        this._changeDetectorRef.markForCheck();
        return true;
    };
    /**
     * Sets blur of chip and sends out event
     * @param {?} event
     * @param {?} value
     * @return {?}
     */
    TdChipsComponent.prototype._handleChipBlur = function (event, value) {
        this.onChipBlur.emit(value);
    };
    /**
     * Sets focus of chip and sends out event
     * @param {?} event
     * @param {?} value
     * @return {?}
     */
    TdChipsComponent.prototype._handleChipFocus = function (event, value) {
        this.setFocusedState();
        this.onChipFocus.emit(value);
    };
    /**
     * @return {?}
     */
    TdChipsComponent.prototype._handleFocus = function () {
        this.setFocusedState();
        this._setFirstOptionActive();
        return true;
    };
    /**
     * Sets focus state of the component
     * @return {?}
     */
    TdChipsComponent.prototype.setFocusedState = function () {
        if (!this.disabled) {
            this._focused = true;
            this._tabIndex = -1;
            this._changeDetectorRef.markForCheck();
        }
    };
    /**
     * Removes focus state of the component
     * @return {?}
     */
    TdChipsComponent.prototype.removeFocusedState = function () {
        this._focused = false;
        this._tabIndex = 0;
        this._changeDetectorRef.markForCheck();
    };
    /**
     * Programmatically focus the input or first chip. Since its the component entry point
     * depending if a user can add or remove chips
     * @return {?}
     */
    TdChipsComponent.prototype.focus = function () {
        if (this.canAddChip) {
            this._inputChild.focus();
        }
        else if (!this.disabled) {
            this._focusFirstChip();
        }
    };
    /**
     * Passes relevant input key presses.
     * @param {?} event
     * @return {?}
     */
    TdChipsComponent.prototype._inputKeydown = function (event) {
        switch (event.keyCode) {
            case keycodes.UP_ARROW:
                /**
                         * Since the first item is highlighted on [requireMatch], we need to inactivate it
                         * when pressing the up key
                         */
                if (this.requireMatch) {
                    var /** @type {?} */ length = this._options.length;
                    if (length > 1 && this._options.toArray()[0].active && this._internalActivateOption) {
                        this._options.toArray()[0].setInactiveStyles();
                        this._internalActivateOption = false;
                        // prevent default window scrolling
                        event.preventDefault();
                    }
                }
                break;
            case keycodes.LEFT_ARROW:
            case keycodes.DELETE:
            case keycodes.BACKSPACE:
                this._closeAutocomplete();
                /** Check to see if input is empty when pressing left arrow to move to the last chip */
                if (!this._inputChild.value) {
                    this._focusLastChip();
                    // prevent default window scrolling
                    event.preventDefault();
                }
                break;
            case keycodes.RIGHT_ARROW:
                this._closeAutocomplete();
                /** Check to see if input is empty when pressing right arrow to move to the first chip */
                if (!this._inputChild.value) {
                    this._focusFirstChip();
                    // prevent default window scrolling
                    event.preventDefault();
                }
                break;
            default:
        }
    };
    /**
     * Passes relevant chip key presses.
     * @param {?} event
     * @param {?} index
     * @return {?}
     */
    TdChipsComponent.prototype._chipKeydown = function (event, index) {
        switch (event.keyCode) {
            case keycodes.DELETE:
            case keycodes.BACKSPACE:
                /** Check to see if we can delete a chip */
                if (this.canRemoveChip) {
                    this.removeChip(index);
                }
                break;
            case keycodes.UP_ARROW:
            case keycodes.LEFT_ARROW:
                /**
                         * Check to see if left/down arrow was pressed while focusing the first chip to focus input next
                         * Also check if input should be focused
                         */
                if (index === 0) {
                    // only try to target input if pressing left
                    if (this.canAddChip && event.keyCode === keycodes.LEFT_ARROW) {
                        this._inputChild.focus();
                    }
                    else {
                        this._focusLastChip();
                    }
                }
                else if (index > 0) {
                    this._focusChip(index - 1);
                }
                // prevent default window scrolling
                event.preventDefault();
                break;
            case keycodes.DOWN_ARROW:
            case keycodes.RIGHT_ARROW:
                /**
                         * Check to see if right/up arrow was pressed while focusing the last chip to focus input next
                         * Also check if input should be focused
                         */
                if (index === (this._totalChips - 1)) {
                    // only try to target input if pressing right
                    if (this.canAddChip && event.keyCode === keycodes.RIGHT_ARROW) {
                        this._inputChild.focus();
                    }
                    else {
                        this._focusFirstChip();
                    }
                }
                else if (index < (this._totalChips - 1)) {
                    this._focusChip(index + 1);
                }
                // prevent default window scrolling
                event.preventDefault();
                break;
            default:
        }
    };
    /**
     * Method to remove from display the value added from the autocomplete since it goes directly as chip.
     * @return {?}
     */
    TdChipsComponent.prototype._removeInputDisplay = function () {
        return '';
    };
    /**
     * Method to open the autocomplete manually if its not already opened
     * @return {?}
     */
    TdChipsComponent.prototype._openAutocomplete = function () {
        if (!this._autocompleteTrigger.panelOpen) {
            this._autocompleteTrigger.openPanel();
            this._changeDetectorRef.markForCheck();
        }
    };
    /**
     * Method to close the autocomplete manually if its not already closed
     * @return {?}
     */
    TdChipsComponent.prototype._closeAutocomplete = function () {
        if (this._autocompleteTrigger.panelOpen) {
            this._autocompleteTrigger.closePanel();
            this._changeDetectorRef.markForCheck();
        }
    };
    Object.defineProperty(TdChipsComponent.prototype, "_totalChips", {
        /**
         * Get total of chips
         * @return {?}
         */
        get: function () {
            var /** @type {?} */ chips$$1 = this._chipsChildren.toArray();
            return chips$$1.length;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Method to focus a desired chip by index
     * @param {?} index
     * @return {?}
     */
    TdChipsComponent.prototype._focusChip = function (index) {
        /** check to see if index exists in the array before focusing */
        if (index > -1 && this._totalChips > index) {
            this._chipsChildren.toArray()[index].focus();
        }
    };
    /**
     * Method to focus first chip
     * @return {?}
     */
    TdChipsComponent.prototype._focusFirstChip = function () {
        this._focusChip(0);
    };
    /**
     * Method to focus last chip
     * @return {?}
     */
    TdChipsComponent.prototype._focusLastChip = function () {
        this._focusChip(this._totalChips - 1);
    };
    /**
     * Method to toggle the disable state of input
     * Checks if not in disabled state and if chipAddition is set to 'true'
     * @return {?}
     */
    TdChipsComponent.prototype._toggleInput = function () {
        if (this.canAddChip) {
            this.inputControl.enable();
        }
        else {
            this.inputControl.disable();
        }
        this._changeDetectorRef.markForCheck();
    };
    /**
     * Sets first option as active to let the user know which one will be added when pressing enter
     * Only if [requireMatch] has been set
     * @return {?}
     */
    TdChipsComponent.prototype._setFirstOptionActive = function () {
        var _this = this;
        if (this.requireMatch) {
            // need to use a timer here to wait until the autocomplete has been opened (end of queue)
            toPromise.toPromise.call(timer.timer()).then(function () {
                if (_this.focused && _this._options && _this._options.length > 0) {
                    // clean up of previously active options
                    _this._options.toArray().forEach(function (option) {
                        option.setInactiveStyles();
                    });
                    // set the first one as active
                    _this._options.toArray()[0].setActiveStyles();
                    _this._internalActivateOption = true;
                    _this._changeDetectorRef.markForCheck();
                }
            });
        }
    };
    /**
     * Watches clicks outside of the component to remove the focus
     * The autocomplete panel is considered inside the component so we
     * need to use a flag to find out when its clicked.
     * @return {?}
     */
    TdChipsComponent.prototype._watchOutsideClick = function () {
        var _this = this;
        if (this._document) {
            merge.merge(fromEvent.fromEvent(this._document, 'click'), fromEvent.fromEvent(this._document, 'touchend')).pipe(filter.filter(function (event) {
                var /** @type {?} */ clickTarget = (event.target);
                setTimeout(function () {
                    _this._internalClick = false;
                });
                return _this.focused &&
                    (clickTarget !== _this._elementRef.nativeElement) &&
                    !_this._elementRef.nativeElement.contains(clickTarget) && !_this._internalClick;
            })).subscribe(function () {
                if (_this.focused) {
                    _this._autocompleteTrigger.closePanel();
                    _this.removeFocusedState();
                    _this.onTouched();
                    _this._changeDetectorRef.markForCheck();
                }
            });
        }
        return undefined;
    };
    return TdChipsComponent;
}(_TdChipsMixinBase));
TdChipsComponent.decorators = [
    { type: core.Component, args: [{
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdChipsComponent; }),
                        multi: true,
                    }],
                selector: 'td-chips',
                inputs: ['disabled', 'value'],
                styles: [":host{\n  display:block;\n  padding:0 5px;\n  min-height:48px; }\n  :host .td-chips-wrapper{\n    min-height:42px;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex;\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    -ms-flex-wrap:wrap;\n        flex-wrap:wrap;\n    -webkit-box-align:start;\n        -ms-flex-align:start;\n            align-items:flex-start; }\n    :host .td-chips-wrapper.td-chips-stacked .mat-basic-chip,\n    :host .td-chips-wrapper.td-chips-stacked .td-chips-form-field{\n      width:100%; }\n    :host .td-chips-wrapper.td-chips-input-before-position .td-chips-form-field{\n      -webkit-box-ordinal-group:0;\n          -ms-flex-order:-1;\n              order:-1; }\n  :host .td-chip, :host .td-chip > .td-chip-content{\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex;\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    max-width:100%;\n    -webkit-box-align:center;\n        -ms-flex-align:center;\n            align-items:center;\n    -ms-flex-line-pack:center;\n        align-content:center;\n    -webkit-box-pack:start;\n        -ms-flex-pack:start;\n            justify-content:start; }\n    :host .td-chip.td-chip-stacked, :host .td-chip > .td-chip-content.td-chip-stacked{\n      -webkit-box-pack:justify;\n          -ms-flex-pack:justify;\n              justify-content:space-between; }\n  :host ::ng-deep{ }\n    :host ::ng-deep .mat-form-field-wrapper{\n      padding-bottom:2px; }\n    :host ::ng-deep .mat-basic-chip{\n      display:inline-block;\n      cursor:default;\n      border-radius:16px;\n      margin:8px 8px 0 0;\n      -webkit-box-sizing:border-box;\n              box-sizing:border-box;\n      max-width:100%;\n      position:relative; }\n      html[dir=rtl] :host ::ng-deep .mat-basic-chip{\n        margin:8px 0 0 8px;\n        unicode-bidi:embed; }\n      body[dir=rtl] :host ::ng-deep .mat-basic-chip{\n        margin:8px 0 0 8px;\n        unicode-bidi:embed; }\n      [dir=rtl] :host ::ng-deep .mat-basic-chip{\n        margin:8px 0 0 8px;\n        unicode-bidi:embed; }\n      :host ::ng-deep .mat-basic-chip bdo[dir=rtl]{\n        direction:rtl;\n        unicode-bidi:bidi-override; }\n      :host ::ng-deep .mat-basic-chip bdo[dir=ltr]{\n        direction:ltr;\n        unicode-bidi:bidi-override; }\n      :host ::ng-deep .mat-basic-chip .td-chip{\n        min-height:32px;\n        line-height:32px;\n        font-size:13px;\n        padding:0 0 0 12px; }\n        html[dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip{\n          padding:0 12px 0 0;\n          unicode-bidi:embed; }\n        body[dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip{\n          padding:0 12px 0 0;\n          unicode-bidi:embed; }\n        [dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip{\n          padding:0 12px 0 0;\n          unicode-bidi:embed; }\n        :host ::ng-deep .mat-basic-chip .td-chip bdo[dir=rtl]{\n          direction:rtl;\n          unicode-bidi:bidi-override; }\n        :host ::ng-deep .mat-basic-chip .td-chip bdo[dir=ltr]{\n          direction:ltr;\n          unicode-bidi:bidi-override; }\n        :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar]{\n          display:inline-block;\n          -webkit-box-ordinal-group:-19;\n              -ms-flex-order:-20;\n                  order:-20;\n          -webkit-box-pack:center;\n              -ms-flex-pack:center;\n                  justify-content:center;\n          -webkit-box-align:center;\n              -ms-flex-align:center;\n                  align-items:center;\n          text-align:center;\n          height:32px;\n          width:32px;\n          margin:0 8px 0 -12px;\n          border-radius:50%; }\n          html[dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar]{\n            margin:0 -12px 0 8px;\n            unicode-bidi:embed; }\n          body[dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar]{\n            margin:0 -12px 0 8px;\n            unicode-bidi:embed; }\n          [dir=rtl] :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar]{\n            margin:0 -12px 0 8px;\n            unicode-bidi:embed; }\n          :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar] bdo[dir=rtl]{\n            direction:rtl;\n            unicode-bidi:bidi-override; }\n          :host ::ng-deep .mat-basic-chip .td-chip [td-chip-avatar] bdo[dir=ltr]{\n            direction:ltr;\n            unicode-bidi:bidi-override; }\n      :host ::ng-deep .mat-basic-chip.td-chip-after-pad{\n        padding:0 12px 0 0; }\n        html[dir=rtl] :host ::ng-deep .mat-basic-chip.td-chip-after-pad{\n          padding:0 0 0 12px;\n          unicode-bidi:embed; }\n        body[dir=rtl] :host ::ng-deep .mat-basic-chip.td-chip-after-pad{\n          padding:0 0 0 12px;\n          unicode-bidi:embed; }\n        [dir=rtl] :host ::ng-deep .mat-basic-chip.td-chip-after-pad{\n          padding:0 0 0 12px;\n          unicode-bidi:embed; }\n        :host ::ng-deep .mat-basic-chip.td-chip-after-pad bdo[dir=rtl]{\n          direction:rtl;\n          unicode-bidi:bidi-override; }\n        :host ::ng-deep .mat-basic-chip.td-chip-after-pad bdo[dir=ltr]{\n          direction:ltr;\n          unicode-bidi:bidi-override; }\n      :host ::ng-deep .mat-basic-chip mat-icon.td-chip-removal{\n        margin:0 4px;\n        font-size:21px;\n        line-height:22px; }\n        :host ::ng-deep .mat-basic-chip mat-icon.td-chip-removal:hover{\n          cursor:pointer; }\n    :host ::ng-deep .td-chips-stacked .mat-basic-chip{\n      margin:4px 0; }\n      :host ::ng-deep .td-chips-stacked .mat-basic-chip:first-of-type{\n        margin:8px 0 4px; }\n      :host ::ng-deep .td-chips-stacked .mat-basic-chip:last-of-type{\n        margin:4px 0 8px; }\n  :host .mat-form-field-underline{\n    position:relative;\n    height:1px;\n    width:100%;\n    bottom:0; }\n    :host .mat-form-field-underline.mat-disabled{\n      background-position:0;\n      bottom:-4px;\n      background-color:transparent; }\n    :host .mat-form-field-underline .mat-form-field-ripple{\n      position:absolute;\n      height:2px;\n      top:0;\n      width:100%;\n      -webkit-transform-origin:50%;\n              transform-origin:50%;\n      -webkit-transform:scaleX(0.5);\n              transform:scaleX(0.5);\n      visibility:hidden;\n      opacity:0;\n      -webkit-transition:background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2);\n      transition:background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2); }\n      :host .mat-form-field-underline .mat-form-field-ripple.mat-focused{\n        visibility:visible;\n        opacity:1;\n        -webkit-transform:scaleX(1);\n                transform:scaleX(1);\n        -webkit-transition:background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2), -webkit-transform 150ms linear;\n        transition:background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2), -webkit-transform 150ms linear;\n        transition:transform 150ms linear, background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2);\n        transition:transform 150ms linear, background-color 0.3s cubic-bezier(0.55, 0, 0.55, 0.2), -webkit-transform 150ms linear; }\n  :host ::ng-deep mat-form-field .mat-form-field-underline{\n    display:none; }\n"],
                template: "<div class=\"td-chips-wrapper\"\n     [class.td-chips-stacked]=\"stacked\"\n     [class.td-chips-input-before-position]=\"inputPosition === 'before'\">\n  <ng-template let-chip let-first=\"first\" let-index=\"index\" ngFor [ngForOf]=\"value\">\n    <mat-basic-chip [class.td-chip-disabled]=\"disabled\"\n                   [class.td-chip-after-pad]=\"!canRemoveChip\"\n                   [color]=\"color\"\n                   (keydown)=\"_chipKeydown($event, index)\"\n                   (blur)=\"_handleChipBlur($event, chip)\"\n                   (focus)=\"_handleChipFocus($event, chip)\">\n      <div class=\"td-chip\" [class.td-chip-stacked]=\"stacked\">\n        <span class=\"td-chip-content\">\n          <span *ngIf=\"!_chipTemplate?.templateRef\">{{chip}}</span>\n          <ng-template\n            *ngIf=\"_chipTemplate?.templateRef\"\n            [ngTemplateOutlet]=\"_chipTemplate?.templateRef\"\n            [ngTemplateOutletContext]=\"{ chip: chip }\">\n          </ng-template>\n        </span>\n        <mat-icon *ngIf=\"canRemoveChip\" class=\"td-chip-removal\" (click)=\"_internalClick = removeChip(index)\">\n          cancel\n        </mat-icon>\n      </div>\n    </mat-basic-chip>\n  </ng-template>\n  <mat-form-field floatPlaceholder=\"never\"\n                  class=\"td-chips-form-field\"\n                  [style.width.px]=\"canAddChip ? null : 0\"\n                  [style.height.px]=\"canAddChip ? null : 0\"\n                  [color]=\"color\">\n    <input matInput\n            #input\n            [tabIndex]=\"-1\"\n            [matAutocomplete]=\"autocomplete\"\n            [formControl]=\"inputControl\"\n            [placeholder]=\"canAddChip? placeholder : ''\"\n            (keydown)=\"_inputKeydown($event)\"\n            (keyup.enter)=\"_handleAddChip()\"\n            (focus)=\"_handleFocus()\">\n  </mat-form-field>\n  <mat-autocomplete #autocomplete=\"matAutocomplete\"\n                   [displayWith]=\"_removeInputDisplay\"\n                   (optionSelected)=\"addChip($event.option.value)\">\n    <ng-template let-item let-first=\"first\" ngFor [ngForOf]=\"items\">\n      <mat-option (click)=\"_setInternalClick()\" [value]=\"item\">\n        <span *ngIf=\"!_autocompleteOptionTemplate?.templateRef\">{{item}}</span>\n        <ng-template\n          *ngIf=\"_autocompleteOptionTemplate?.templateRef\"\n          [ngTemplateOutlet]=\"_autocompleteOptionTemplate?.templateRef\"\n          [ngTemplateOutletContext]=\"{ option: item }\">\n        </ng-template>\n      </mat-option>\n    </ng-template>\n  </mat-autocomplete>\n</div>\n<div *ngIf=\"chipAddition\" class=\"mat-form-field-underline\"\n      [class.mat-disabled]=\"disabled\">\n  <span class=\"mat-form-field-ripple\"\n        [class.mat-focused]=\"focused\"></span>\n</div>\n<ng-content></ng-content>",
                changeDetection: core.ChangeDetectionStrategy.OnPush,
            },] },
];
/** @nocollapse */
TdChipsComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
    { type: undefined, decorators: [{ type: core.Optional }, { type: core.Inject, args: [platformBrowser.DOCUMENT,] },] },
    { type: core.ChangeDetectorRef, },
]; };
TdChipsComponent.propDecorators = {
    "_nativeInput": [{ type: core.ViewChild, args: ['input',] },],
    "_inputChild": [{ type: core.ViewChild, args: [input.MatInput,] },],
    "_autocompleteTrigger": [{ type: core.ViewChild, args: [autocomplete.MatAutocompleteTrigger,] },],
    "_chipsChildren": [{ type: core.ViewChildren, args: [chips.MatChip,] },],
    "_chipTemplate": [{ type: core.ContentChild, args: [TdChipDirective,] },],
    "_autocompleteOptionTemplate": [{ type: core.ContentChild, args: [TdAutocompleteOptionDirective,] },],
    "_options": [{ type: core.ViewChildren, args: [core$1.MatOption,] },],
    "items": [{ type: core.Input, args: ['items',] },],
    "stacked": [{ type: core.Input, args: ['stacked',] },],
    "inputPosition": [{ type: core.Input, args: ['inputPosition',] },],
    "requireMatch": [{ type: core.Input, args: ['requireMatch',] },],
    "chipAddition": [{ type: core.Input, args: ['chipAddition',] },],
    "chipRemoval": [{ type: core.Input, args: ['chipRemoval',] },],
    "placeholder": [{ type: core.Input, args: ['placeholder',] },],
    "debounce": [{ type: core.Input, args: ['debounce',] },],
    "color": [{ type: core.Input, args: ['color',] },],
    "onAdd": [{ type: core.Output, args: ['add',] },],
    "onRemove": [{ type: core.Output, args: ['remove',] },],
    "onInputChange": [{ type: core.Output, args: ['inputChange',] },],
    "onChipFocus": [{ type: core.Output, args: ['chipFocus',] },],
    "onChipBlur": [{ type: core.Output, args: ['chipBlur',] },],
    "tabIndex": [{ type: core.HostBinding, args: ['attr.tabindex',] },],
    "focusListener": [{ type: core.HostListener, args: ['focus', ['$event'],] },],
    "mousedownListener": [{ type: core.HostListener, args: ['mousedown', ['$event'],] },],
    "clickListener": [{ type: core.HostListener, args: ['click', ['$event'],] },],
    "keydownListener": [{ type: core.HostListener, args: ['keydown', ['$event'],] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var CovalentChipsModule = /** @class */ (function () {
    function CovalentChipsModule() {
    }
    return CovalentChipsModule;
}());
CovalentChipsModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    forms.ReactiveFormsModule,
                    common$1.CommonModule,
                    input.MatInputModule,
                    icon.MatIconModule,
                    chips.MatChipsModule,
                    autocomplete.MatAutocompleteModule,
                ],
                declarations: [
                    TdChipsComponent,
                    TdChipDirective,
                    TdAutocompleteOptionDirective,
                ],
                exports: [
                    TdChipsComponent,
                    TdChipDirective,
                    TdAutocompleteOptionDirective,
                ],
            },] },
];
/** @nocollapse */
CovalentChipsModule.ctorParameters = function () { return []; };

exports.CovalentChipsModule = CovalentChipsModule;
exports.TdChipDirective = TdChipDirective;
exports.TdAutocompleteOptionDirective = TdAutocompleteOptionDirective;
exports.TdChipsBase = TdChipsBase;
exports._TdChipsMixinBase = _TdChipsMixinBase;
exports.TdChipsComponent = TdChipsComponent;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-chips.umd.js.map
