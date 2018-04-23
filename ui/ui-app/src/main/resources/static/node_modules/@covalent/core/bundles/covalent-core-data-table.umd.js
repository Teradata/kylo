(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('@angular/core'), require('@angular/cdk/portal'), require('@angular/platform-browser'), require('@angular/forms'), require('@angular/cdk/coercion'), require('@angular/cdk/keycodes'), require('rxjs/Subject'), require('rxjs/operators/debounceTime'), require('@covalent/core/common'), require('@angular/common'), require('@angular/material/checkbox'), require('@angular/material/tooltip'), require('@angular/material/icon'), require('@angular/material/core')) :
	typeof define === 'function' && define.amd ? define(['exports', '@angular/core', '@angular/cdk/portal', '@angular/platform-browser', '@angular/forms', '@angular/cdk/coercion', '@angular/cdk/keycodes', 'rxjs/Subject', 'rxjs/operators/debounceTime', '@covalent/core/common', '@angular/common', '@angular/material/checkbox', '@angular/material/tooltip', '@angular/material/icon', '@angular/material/core'], factory) :
	(factory((global.covalent = global.covalent || {}, global.covalent.core = global.covalent.core || {}, global.covalent.core['data-table'] = {}),global.ng.core,global.ng.cdk.portal,global.ng.platformBrowser,global.ng.forms,global.ng.cdk.coercion,global.ng.cdk.keycodes,global.Rx,global.Rx.Observable.prototype,global.covalent.core.common,global.ng.common,global.ng.material.checkbox,global.ng.material.tooltip,global.ng.material.icon,global.ng.material.core));
}(this, (function (exports,core,portal,platformBrowser,forms,coercion,keycodes,Subject,debounceTime,common,common$1,checkbox,tooltip,icon,core$1) { 'use strict';

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








function __values(o) {
    var m = typeof Symbol === "function" && o[Symbol.iterator], i = 0;
    if (m) return m.call(o);
    return {
        next: function () {
            if (o && i >= o.length) o = void 0;
            return { value: o && o[i++], done: !o };
        }
    };
}

/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDataTableColumnRowComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     */
    function TdDataTableColumnRowComponent(_elementRef, _renderer) {
        this._elementRef = _elementRef;
        this._renderer = _renderer;
        this._renderer.addClass(this._elementRef.nativeElement, 'td-data-table-column-row');
    }
    return TdDataTableColumnRowComponent;
}());
TdDataTableColumnRowComponent.decorators = [
    { type: core.Component, args: [{
                /* tslint:disable-next-line */
                selector: 'tr[td-data-table-column-row]',
                styles: [":host{\n  border-bottom-style:solid;\n  border-bottom-width:1px; }\n:host.td-data-table-row{\n  height:48px; }\n:host.td-data-table-column-row{\n  height:56px; }\n"],
                template: "<ng-content></ng-content>",
            },] },
];
/** @nocollapse */
TdDataTableColumnRowComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
]; };
var TdDataTableRowComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     */
    function TdDataTableRowComponent(_elementRef, _renderer) {
        this._elementRef = _elementRef;
        this._renderer = _renderer;
        this._selected = false;
        this._renderer.addClass(this._elementRef.nativeElement, 'td-data-table-row');
    }
    Object.defineProperty(TdDataTableRowComponent.prototype, "selected", {
        /**
         * @return {?}
         */
        get: function () {
            return this._selected;
        },
        /**
         * @param {?} selected
         * @return {?}
         */
        set: function (selected) {
            if (selected) {
                this._renderer.addClass(this._elementRef.nativeElement, 'td-selected');
            }
            else {
                this._renderer.removeClass(this._elementRef.nativeElement, 'td-selected');
            }
            this._selected = selected;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableRowComponent.prototype, "height", {
        /**
         * @return {?}
         */
        get: function () {
            var /** @type {?} */ height = 48;
            if (this._elementRef.nativeElement) {
                height = ((this._elementRef.nativeElement)).getBoundingClientRect().height;
            }
            return height;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Listening to click event to explicitly focus the row element.
     * @return {?}
     */
    TdDataTableRowComponent.prototype.clickListener = function () {
        this.focus();
    };
    /**
     * @return {?}
     */
    TdDataTableRowComponent.prototype.focus = function () {
        this._elementRef.nativeElement.focus();
    };
    return TdDataTableRowComponent;
}());
TdDataTableRowComponent.decorators = [
    { type: core.Component, args: [{
                /* tslint:disable-next-line */
                selector: 'tr[td-data-table-row]',
                styles: [":host{\n  border-bottom-style:solid;\n  border-bottom-width:1px; }\n:host.td-data-table-row{\n  height:48px; }\n:host.td-data-table-column-row{\n  height:56px; }\n"],
                template: "<ng-content></ng-content>",
            },] },
];
/** @nocollapse */
TdDataTableRowComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
]; };
TdDataTableRowComponent.propDecorators = {
    "selected": [{ type: core.Input, args: ['selected',] },],
    "clickListener": [{ type: core.HostListener, args: ['click',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDataTableTemplateDirective = /** @class */ (function (_super) {
    __extends(TdDataTableTemplateDirective, _super);
    /**
     * @param {?} templateRef
     * @param {?} viewContainerRef
     */
    function TdDataTableTemplateDirective(templateRef, viewContainerRef) {
        return _super.call(this, templateRef, viewContainerRef) || this;
    }
    return TdDataTableTemplateDirective;
}(portal.TemplatePortalDirective));
TdDataTableTemplateDirective.decorators = [
    { type: core.Directive, args: [{ selector: '[tdDataTableTemplate]ng-template' },] },
];
/** @nocollapse */
TdDataTableTemplateDirective.ctorParameters = function () { return [
    { type: core.TemplateRef, },
    { type: core.ViewContainerRef, },
]; };
TdDataTableTemplateDirective.propDecorators = {
    "tdDataTableTemplate": [{ type: core.Input },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/** @enum {string} */
var TdDataTableSortingOrder = {
    Ascending: 'ASC',
    Descending: 'DESC',
};
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
/**
 * @record
 */
/**
 * @record
 */
/**
 * Constant to set the rows offset before and after the viewport
 */
var TD_VIRTUAL_OFFSET = 2;
/**
 * Constant to set default row height if none is provided
 */
var TD_VIRTUAL_DEFAULT_ROW_HEIGHT = 48;
var TdDataTableBase = /** @class */ (function () {
    /**
     * @param {?} _changeDetectorRef
     */
    function TdDataTableBase(_changeDetectorRef) {
        this._changeDetectorRef = _changeDetectorRef;
    }
    return TdDataTableBase;
}());
/* tslint:disable-next-line */
var _TdDataTableMixinBase = common.mixinControlValueAccessor(TdDataTableBase, []);
var TdDataTableComponent = /** @class */ (function (_super) {
    __extends(TdDataTableComponent, _super);
    /**
     * @param {?} _document
     * @param {?} _elementRef
     * @param {?} _domSanitizer
     * @param {?} _changeDetectorRef
     */
    function TdDataTableComponent(_document, _elementRef, _domSanitizer, _changeDetectorRef) {
        var _this = _super.call(this, _changeDetectorRef) || this;
        _this._document = _document;
        _this._elementRef = _elementRef;
        _this._domSanitizer = _domSanitizer;
        _this._hostWidth = 0;
        _this._widths = [];
        _this._onResize = new Subject.Subject();
        _this._scrollHorizontalOffset = 0;
        _this._onHorizontalScroll = new Subject.Subject();
        _this._onVerticalScroll = new Subject.Subject();
        _this._rowHeightCache = [];
        _this._totalHeight = 0;
        _this._hostHeight = 0;
        _this._scrollVerticalOffset = 0;
        _this._fromRow = 0;
        _this._toRow = 0;
        _this._selectable = false;
        _this._clickable = false;
        _this._multiple = true;
        _this._allSelected = false;
        _this._indeterminate = false;
        /**
         * sorting
         */
        _this._sortable = false;
        _this._sortOrder = TdDataTableSortingOrder.Ascending;
        /**
         * shift select
         */
        _this._shiftPreviouslyPressed = false;
        _this._lastSelectedIndex = -1;
        _this._firstSelectedIndex = -1;
        _this._firstCheckboxValue = false;
        /**
         * template fetching support
         */
        _this._templateMap = new Map();
        /**
         * sortChange?: function
         * Event emitted when the column headers are clicked. [sortable] needs to be enabled.
         * Emits an [ITdDataTableSortChangeEvent] implemented object.
         */
        _this.onSortChange = new core.EventEmitter();
        /**
         * rowSelect?: function
         * Event emitted when a row is selected/deselected. [selectable] needs to be enabled.
         * Emits an [ITdDataTableSelectEvent] implemented object.
         */
        _this.onRowSelect = new core.EventEmitter();
        /**
         * rowClick?: function
         * Event emitted when a row is clicked.
         * Emits an [ITdDataTableRowClickEvent] implemented object.
         */
        _this.onRowClick = new core.EventEmitter();
        /**
         * selectAll?: function
         * Event emitted when all rows are selected/deselected by the all checkbox. [selectable] needs to be enabled.
         * Emits an [ITdDataTableSelectAllEvent] implemented object.
         */
        _this.onSelectAll = new core.EventEmitter();
        /**
         * compareWith?: function(row, model): boolean
         * Allows custom comparison between row and model to see if row is selected or not
         * Default comparation is by reference
         */
        _this.compareWith = function (row, model) {
            return row === model;
        };
        return _this;
    }
    Object.defineProperty(TdDataTableComponent.prototype, "hostWidth", {
        /**
         * @return {?}
         */
        get: function () {
            // if the checkboxes are rendered, we need to remove their width
            // from the total width to calculate properly
            if (this.selectable) {
                return this._hostWidth - 42;
            }
            return this._hostWidth;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "offsetTransform", {
        /**
         * Returns the offset style with a proper calculation on how much it should move
         * over the y axis of the total height
         * @return {?}
         */
        get: function () {
            return this._offsetTransform;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "totalHeight", {
        /**
         * Returns the assumed total height of the rows
         * @return {?}
         */
        get: function () {
            return this._totalHeight;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "fromRow", {
        /**
         * Returns the initial row to render in the viewport
         * @return {?}
         */
        get: function () {
            return this._fromRow;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "toRow", {
        /**
         * Returns the last row to render in the viewport
         * @return {?}
         */
        get: function () {
            return this._toRow;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "columnsLeftScroll", {
        /**
         * Returns scroll position to reposition column headers
         * @return {?}
         */
        get: function () {
            return this._scrollHorizontalOffset * -1;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "allSelected", {
        /**
         * Returns true if all values are selected.
         * @return {?}
         */
        get: function () {
            return this._allSelected;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "indeterminate", {
        /**
         * Returns true if all values are not deselected
         * and at least one is.
         * @return {?}
         */
        get: function () {
            return this._indeterminate;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "data", {
        /**
         * @return {?}
         */
        get: function () {
            return this._data;
        },
        /**
         * data?: {[key: string]: any}[]
         * Sets the data to be rendered as rows.
         * @param {?} data
         * @return {?}
         */
        set: function (data) {
            var _this = this;
            this._data = data;
            this._rowHeightCache = [];
            Promise.resolve().then(function () {
                _this.refresh();
                // scroll back to the top if the data has changed
                _this._scrollableDiv.nativeElement.scrollTop = 0;
            });
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "virtualData", {
        /**
         * @return {?}
         */
        get: function () {
            return this._virtualData;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "columns", {
        /**
         * @return {?}
         */
        get: function () {
            var _this = this;
            if (this._columns) {
                return this._columns;
            }
            if (this.hasData) {
                this._columns = [];
                // if columns is undefined, use key in [data] rows as name and label for column headers.
                var /** @type {?} */ row = this._data[0];
                Object.keys(row).forEach(function (k) {
                    if (!_this._columns.find(function (c) { return c.name === k; })) {
                        _this._columns.push({ name: k, label: k });
                    }
                });
                return this._columns;
            }
            else {
                return [];
            }
        },
        /**
         * columns?: ITdDataTableColumn[]
         * Sets additional column configuration. [ITdDataTableColumn.name] has to exist in [data] as key.
         * Defaults to [data] keys.
         * @param {?} cols
         * @return {?}
         */
        set: function (cols) {
            this._columns = cols;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "selectable", {
        /**
         * @return {?}
         */
        get: function () {
            return this._selectable;
        },
        /**
         * selectable?: boolean
         * Enables row selection events, hover and selected row states.
         * Defaults to 'false'
         * @param {?} selectable
         * @return {?}
         */
        set: function (selectable) {
            this._selectable = coercion.coerceBooleanProperty(selectable);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "clickable", {
        /**
         * @return {?}
         */
        get: function () {
            return this._clickable;
        },
        /**
         * clickable?: boolean
         * Enables row click events, hover.
         * Defaults to 'false'
         * @param {?} clickable
         * @return {?}
         */
        set: function (clickable) {
            this._clickable = coercion.coerceBooleanProperty(clickable);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "multiple", {
        /**
         * @return {?}
         */
        get: function () {
            return this._multiple;
        },
        /**
         * multiple?: boolean
         * Enables multiple row selection. [selectable] needs to be enabled.
         * Defaults to 'false'
         * @param {?} multiple
         * @return {?}
         */
        set: function (multiple) {
            this._multiple = coercion.coerceBooleanProperty(multiple);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "sortable", {
        /**
         * @return {?}
         */
        get: function () {
            return this._sortable;
        },
        /**
         * sortable?: boolean
         * Enables sorting events, sort icons and active column states.
         * Defaults to 'false'
         * @param {?} sortable
         * @return {?}
         */
        set: function (sortable) {
            this._sortable = coercion.coerceBooleanProperty(sortable);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "sortBy", {
        /**
         * sortBy?: string
         * Sets the active sort column. [sortable] needs to be enabled.
         * @param {?} columnName
         * @return {?}
         */
        set: function (columnName) {
            if (!columnName) {
                return;
            }
            var /** @type {?} */ column = this.columns.find(function (c) { return c.name === columnName; });
            if (!column) {
                throw new Error('[sortBy] must be a valid column name');
            }
            this._sortBy = column;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "sortByColumn", {
        /**
         * @return {?}
         */
        get: function () {
            return this._sortBy;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "sortOrder", {
        /**
         * sortOrder?: ['ASC' | 'DESC'] or TdDataTableSortingOrder
         * Sets the sort order of the [sortBy] column. [sortable] needs to be enabled.
         * Defaults to 'ASC' or TdDataTableSortingOrder.Ascending
         * @param {?} order
         * @return {?}
         */
        set: function (order) {
            var /** @type {?} */ sortOrder = order ? order.toUpperCase() : 'ASC';
            if (sortOrder !== 'DESC' && sortOrder !== 'ASC') {
                throw new Error('[sortOrder] must be empty, ASC or DESC');
            }
            this._sortOrder = sortOrder === 'ASC' ?
                TdDataTableSortingOrder.Ascending : TdDataTableSortingOrder.Descending;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "sortOrderEnum", {
        /**
         * @return {?}
         */
        get: function () {
            return this._sortOrder;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableComponent.prototype, "hasData", {
        /**
         * @return {?}
         */
        get: function () {
            return this._data && this._data.length > 0;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Initialize observable for resize and scroll events
     * @return {?}
     */
    TdDataTableComponent.prototype.ngOnInit = function () {
        var _this = this;
        // initialize observable for resize calculations
        this._resizeSubs = this._onResize.asObservable().subscribe(function () {
            if (_this._rows) {
                _this._rows.toArray().forEach(function (row, index) {
                    _this._rowHeightCache[_this.fromRow + index] = row.height + 1;
                });
            }
            _this._calculateWidths();
            _this._calculateVirtualRows();
        });
        // initialize observable for scroll column header reposition
        this._horizontalScrollSubs = this._onHorizontalScroll.asObservable()
            .subscribe(function (horizontalScroll) {
            _this._scrollHorizontalOffset = horizontalScroll;
            _this._changeDetectorRef.markForCheck();
        });
        // initialize observable for virtual scroll rendering
        this._verticalScrollSubs = this._onVerticalScroll.asObservable()
            .subscribe(function (verticalScroll) {
            _this._scrollVerticalOffset = verticalScroll;
            _this._calculateVirtualRows();
            _this._changeDetectorRef.markForCheck();
        });
        this._valueChangesSubs = this.valueChanges.subscribe(function (value) {
            _this.refresh();
        });
    };
    /**
     * Loads templates and sets them in a map for faster access.
     * @return {?}
     */
    TdDataTableComponent.prototype.ngAfterContentInit = function () {
        for (var /** @type {?} */ i = 0; i < this._templates.toArray().length; i++) {
            this._templateMap.set(this._templates.toArray()[i].tdDataTableTemplate, this._templates.toArray()[i].templateRef);
        }
    };
    /**
     * Checks hosts native elements widths to see if it has changed (resize check)
     * @return {?}
     */
    TdDataTableComponent.prototype.ngAfterContentChecked = function () {
        if (this._elementRef.nativeElement) {
            var /** @type {?} */ newHostWidth = this._elementRef.nativeElement.getBoundingClientRect().width;
            // if the width has changed then we throw a resize event.
            if (this._hostWidth !== newHostWidth) {
                this._hostWidth = newHostWidth;
                this._onResize.next();
            }
        }
        if (this._scrollableDiv.nativeElement) {
            var /** @type {?} */ newHostHeight = this._scrollableDiv.nativeElement.getBoundingClientRect().height;
            // if the height of the viewport has changed, then we mark for check
            if (this._hostHeight !== newHostHeight) {
                this._hostHeight = newHostHeight;
                this._calculateVirtualRows();
                this._changeDetectorRef.markForCheck();
            }
        }
    };
    /**
     * Registers to an observable that checks if all rows have been rendered
     * so we can start calculating the widths
     * @return {?}
     */
    TdDataTableComponent.prototype.ngAfterViewInit = function () {
        var _this = this;
        this._rowsChangedSubs = this._rows.changes.pipe(debounceTime.debounceTime(0)).subscribe(function () {
            _this._onResize.next();
        });
        this._calculateVirtualRows();
    };
    /**
     * Unsubscribes observables when data table is destroyed
     * @return {?}
     */
    TdDataTableComponent.prototype.ngOnDestroy = function () {
        if (this._resizeSubs) {
            this._resizeSubs.unsubscribe();
        }
        if (this._horizontalScrollSubs) {
            this._horizontalScrollSubs.unsubscribe();
        }
        if (this._verticalScrollSubs) {
            this._verticalScrollSubs.unsubscribe();
        }
        if (this._rowsChangedSubs) {
            this._rowsChangedSubs.unsubscribe();
        }
        if (this._valueChangesSubs) {
            this._valueChangesSubs.unsubscribe();
        }
    };
    /**
     * Method that gets executed every time there is a scroll event
     * Calls the scroll observable
     * @param {?} event
     * @return {?}
     */
    TdDataTableComponent.prototype.handleScroll = function (event) {
        var /** @type {?} */ element = ((event.target));
        if (element) {
            var /** @type {?} */ horizontalScroll = element.scrollLeft;
            if (this._scrollHorizontalOffset !== horizontalScroll) {
                this._onHorizontalScroll.next(horizontalScroll);
            }
            var /** @type {?} */ verticalScroll = element.scrollTop;
            if (this._scrollVerticalOffset !== verticalScroll) {
                this._onVerticalScroll.next(verticalScroll);
            }
        }
    };
    /**
     * Returns the width needed for the columns via index
     * @param {?} index
     * @return {?}
     */
    TdDataTableComponent.prototype.getColumnWidth = function (index) {
        if (this._widths[index]) {
            return this._widths[index].value;
        }
        return undefined;
    };
    /**
     * @param {?} column
     * @param {?} value
     * @return {?}
     */
    TdDataTableComponent.prototype.getCellValue = function (column, value) {
        if (column.nested === undefined || column.nested) {
            return this._getNestedValue(column.name, value);
        }
        return value[column.name];
    };
    /**
     * Getter method for template references
     * @param {?} name
     * @return {?}
     */
    TdDataTableComponent.prototype.getTemplateRef = function (name) {
        return this._templateMap.get(name);
    };
    /**
     * Clears model (ngModel) of component by removing all values in array.
     * @return {?}
     */
    TdDataTableComponent.prototype.clearModel = function () {
        this.value.splice(0, this.value.length);
    };
    /**
     * Refreshes data table and rerenders [data] and [columns]
     * @return {?}
     */
    TdDataTableComponent.prototype.refresh = function () {
        this._calculateVirtualRows();
        this._calculateWidths();
        this._calculateCheckboxState();
        this._changeDetectorRef.markForCheck();
    };
    /**
     * Selects or clears all rows depending on 'checked' value.
     * @param {?} checked
     * @return {?}
     */
    TdDataTableComponent.prototype.selectAll = function (checked) {
        var _this = this;
        var /** @type {?} */ toggledRows = [];
        if (checked) {
            this._data.forEach(function (row) {
                // skiping already selected rows
                if (!_this.isRowSelected(row)) {
                    _this.value.push(row);
                    // checking which ones are being toggled
                    toggledRows.push(row);
                }
            });
            this._allSelected = true;
            this._indeterminate = true;
        }
        else {
            this._data.forEach(function (row) {
                // checking which ones are being toggled
                if (_this.isRowSelected(row)) {
                    toggledRows.push(row);
                    var /** @type {?} */ modelRow = _this.value.filter(function (val) {
                        return _this.compareWith(row, val);
                    })[0];
                    var /** @type {?} */ index = _this.value.indexOf(modelRow);
                    if (index > -1) {
                        _this.value.splice(index, 1);
                    }
                }
            });
            this._allSelected = false;
            this._indeterminate = false;
        }
        this.onSelectAll.emit({ rows: toggledRows, selected: checked });
    };
    /**
     * Checks if row is selected
     * @param {?} row
     * @return {?}
     */
    TdDataTableComponent.prototype.isRowSelected = function (row) {
        var _this = this;
        // compare items by [compareWith] function
        return this.value ? this.value.filter(function (val) {
            return _this.compareWith(row, val);
        }).length > 0 : false;
    };
    /**
     * Selects or clears a row depending on 'checked' value if the row 'isSelectable'
     * handles cntrl clicks and shift clicks for multi-select
     * @param {?} row
     * @param {?} event
     * @param {?} currentSelected
     * @return {?}
     */
    TdDataTableComponent.prototype.select = function (row, event, currentSelected) {
        if (this.selectable) {
            this.blockEvent(event);
            // Check to see if Shift key is selected and need to select everything in between
            var /** @type {?} */ mouseEvent = (event);
            if (this.multiple && mouseEvent && mouseEvent.shiftKey && this._lastSelectedIndex > -1) {
                var /** @type {?} */ firstIndex = currentSelected;
                var /** @type {?} */ lastIndex = this._lastSelectedIndex;
                if (currentSelected > this._lastSelectedIndex) {
                    firstIndex = this._lastSelectedIndex;
                    lastIndex = currentSelected;
                }
                // if clicking a checkbox behind the initial check, then toggle all selections expect the initial checkbox
                // else the checkboxes clicked are all after the initial one
                if ((this._firstSelectedIndex >= currentSelected && this._lastSelectedIndex > this._firstSelectedIndex) ||
                    (this._firstSelectedIndex <= currentSelected && this._lastSelectedIndex < this._firstSelectedIndex)) {
                    for (var /** @type {?} */ i = firstIndex; i <= lastIndex; i++) {
                        if (this._firstSelectedIndex !== i) {
                            this._doSelection(this._data[i], i);
                        }
                    }
                }
                else if ((this._firstSelectedIndex > currentSelected) || (this._firstSelectedIndex < currentSelected)) {
                    // change indexes depending on where the next checkbox is selected (before or after)
                    if (this._firstSelectedIndex > currentSelected) {
                        lastIndex--;
                    }
                    else if (this._firstSelectedIndex < currentSelected) {
                        firstIndex++;
                    }
                    for (var /** @type {?} */ i = firstIndex; i <= lastIndex; i++) {
                        var /** @type {?} */ rowSelected = this.isRowSelected(this._data[i]);
                        // if row is selected and first checkbox was selected
                        // or if row was unselected and first checkbox was unselected
                        // we ignore the toggle
                        if ((this._firstCheckboxValue && !rowSelected) ||
                            (!this._firstCheckboxValue && rowSelected)) {
                            this._doSelection(this._data[i], i);
                        }
                        else if (this._shiftPreviouslyPressed) {
                            // else if the checkbox selected was in the middle of the last selection and the first selection
                            // then we undo the selections
                            if ((currentSelected >= this._firstSelectedIndex && currentSelected <= this._lastSelectedIndex) ||
                                (currentSelected <= this._firstSelectedIndex && currentSelected >= this._lastSelectedIndex)) {
                                this._doSelection(this._data[i], i);
                            }
                        }
                    }
                }
                this._shiftPreviouslyPressed = true;
                // if shift wasnt pressed, then we take the element checked as the first row
                // incase the next click uses shift
            }
            else if (mouseEvent && !mouseEvent.shiftKey) {
                this._firstCheckboxValue = this._doSelection(row, currentSelected);
                this._shiftPreviouslyPressed = false;
                this._firstSelectedIndex = currentSelected;
            }
            this._lastSelectedIndex = currentSelected;
        }
    };
    /**
     * Overrides the onselectstart method of the document so other text on the page
     * doesn't get selected when doing shift selections.
     * @return {?}
     */
    TdDataTableComponent.prototype.disableTextSelection = function () {
        if (this._document) {
            this._document.onselectstart = function () {
                return false;
            };
        }
    };
    /**
     * Resets the original onselectstart method.
     * @return {?}
     */
    TdDataTableComponent.prototype.enableTextSelection = function () {
        if (this._document) {
            this._document.onselectstart = undefined;
        }
    };
    /**
     * emits the onRowClickEvent when a row is clicked
     * if clickable is true and selectable is false then select the row
     * @param {?} row
     * @param {?} index
     * @param {?} event
     * @return {?}
     */
    TdDataTableComponent.prototype.handleRowClick = function (row, index, event) {
        if (this.clickable) {
            // ignoring linting rules here because attribute it actually null or not there
            // can't check for undefined
            var /** @type {?} */ srcElement = event.srcElement || event.currentTarget;
            /* tslint:disable-next-line */
            if (srcElement.getAttribute('stopRowClick') === null) {
                this.onRowClick.emit({
                    row: row,
                    index: index,
                });
            }
        }
    };
    /**
     * Method handle for sort click event in column headers.
     * @param {?} column
     * @return {?}
     */
    TdDataTableComponent.prototype.handleSort = function (column) {
        if (this._sortBy === column) {
            this._sortOrder = this._sortOrder === TdDataTableSortingOrder.Ascending ?
                TdDataTableSortingOrder.Descending : TdDataTableSortingOrder.Ascending;
        }
        else {
            this._sortBy = column;
            this._sortOrder = TdDataTableSortingOrder.Ascending;
        }
        this.onSortChange.next({ name: this._sortBy.name, order: this._sortOrder });
    };
    /**
     * Handle all keyup events when focusing a data table row
     * @param {?} event
     * @param {?} row
     * @param {?} index
     * @return {?}
     */
    TdDataTableComponent.prototype._rowKeyup = function (event, row, index) {
        switch (event.keyCode) {
            case keycodes.ENTER:
            case keycodes.SPACE:
                /** if user presses enter or space, the row should be selected */
                if (this.selectable) {
                    this._doSelection(this._data[this.fromRow + index], this.fromRow + index);
                }
                break;
            case keycodes.UP_ARROW:
                /**
                         * if users presses the up arrow, we focus the prev row
                         * unless its the first row
                         */
                if (index > 0) {
                    this._rows.toArray()[index - 1].focus();
                }
                this.blockEvent(event);
                if (this.selectable && this.multiple && event.shiftKey && this.fromRow + index >= 0) {
                    this._doSelection(this._data[this.fromRow + index], this.fromRow + index);
                }
                break;
            case keycodes.DOWN_ARROW:
                /**
                         * if users presses the down arrow, we focus the next row
                         * unless its the last row
                         */
                if (index < (this._rows.toArray().length - 1)) {
                    this._rows.toArray()[index + 1].focus();
                }
                this.blockEvent(event);
                if (this.selectable && this.multiple && event.shiftKey && this.fromRow + index < this._data.length) {
                    this._doSelection(this._data[this.fromRow + index], this.fromRow + index);
                }
                break;
            default:
        }
    };
    /**
     * Method to prevent the default events
     * @param {?} event
     * @return {?}
     */
    TdDataTableComponent.prototype.blockEvent = function (event) {
        event.preventDefault();
    };
    /**
     * @param {?} name
     * @param {?} value
     * @return {?}
     */
    TdDataTableComponent.prototype._getNestedValue = function (name, value) {
        if (!(value instanceof Object) || !name) {
            return value;
        }
        if (name.indexOf('.') > -1) {
            var /** @type {?} */ splitName = name.split(/\.(.+)/, 2);
            return this._getNestedValue(splitName[1], value[splitName[0]]);
        }
        else {
            return value[name];
        }
    };
    /**
     * Does the actual Row Selection
     * @param {?} row
     * @param {?} rowIndex
     * @return {?}
     */
    TdDataTableComponent.prototype._doSelection = function (row, rowIndex) {
        var _this = this;
        var /** @type {?} */ wasSelected = this.isRowSelected(row);
        if (!wasSelected) {
            if (!this._multiple) {
                this.clearModel();
            }
            this.value.push(row);
        }
        else {
            // compare items by [compareWith] function
            row = this.value.filter(function (val) {
                return _this.compareWith(row, val);
            })[0];
            var /** @type {?} */ index = this.value.indexOf(row);
            if (index > -1) {
                this.value.splice(index, 1);
            }
        }
        this._calculateCheckboxState();
        this.onRowSelect.emit({ row: row, index: rowIndex, selected: !wasSelected });
        this.onChange(this.value);
        return !wasSelected;
    };
    /**
     * Calculate all the state of all checkboxes
     * @return {?}
     */
    TdDataTableComponent.prototype._calculateCheckboxState = function () {
        var _this = this;
        if (this._data) {
            this._allSelected = typeof this._data.find(function (d) { return !_this.isRowSelected(d); }) === 'undefined';
            this._indeterminate = false;
            try {
                for (var _a = __values(this._data), _b = _a.next(); !_b.done; _b = _a.next()) {
                    var row = _b.value;
                    if (!this.isRowSelected(row)) {
                        continue;
                    }
                    this._indeterminate = true;
                    break;
                }
            }
            catch (e_1_1) { e_1 = { error: e_1_1 }; }
            finally {
                try {
                    if (_b && !_b.done && (_c = _a.return)) _c.call(_a);
                }
                finally { if (e_1) throw e_1.error; }
            }
        }
        var e_1, _c;
    };
    /**
     * Calculates the widths for columns and cells depending on content
     * @return {?}
     */
    TdDataTableComponent.prototype._calculateWidths = function () {
        var _this = this;
        if (this._colElements && this._colElements.length) {
            this._widths = [];
            this._colElements.forEach(function (col, index) {
                _this._adjustColumnWidth(index, _this._calculateWidth());
            });
            this._adjustColumnWidhts();
            this._changeDetectorRef.markForCheck();
        }
    };
    /**
     * Adjusts columns after calculation to see if they need to be recalculated.
     * @return {?}
     */
    TdDataTableComponent.prototype._adjustColumnWidhts = function () {
        var _this = this;
        var /** @type {?} */ fixedTotalWidth = 0;
        // get the number of total columns that have flexible widths (not fixed or hidden)
        var /** @type {?} */ flexibleWidths = this._widths.filter(function (width, index) {
            if (_this.columns[index].hidden) {
                return false;
            }
            if (width.limit || width.max || width.min) {
                fixedTotalWidth += width.value;
            }
            return !width.limit && !width.max && !width.min;
        }).length;
        // calculate how much pixes are left that could be spread across
        // the flexible columns
        var /** @type {?} */ recalculateHostWidth = 0;
        if (fixedTotalWidth < this.hostWidth) {
            recalculateHostWidth = this.hostWidth - fixedTotalWidth;
        }
        // if we have flexible columns and pixels to spare on them
        // we try and spread the pixels across them
        if (flexibleWidths && recalculateHostWidth) {
            var /** @type {?} */ newValue_1 = Math.floor(recalculateHostWidth / flexibleWidths);
            var /** @type {?} */ adjustedNumber_1 = 0;
            // adjust the column widths with the spread pixels
            this._widths.forEach(function (colWidth) {
                if (_this._widths[colWidth.index].max && _this._widths[colWidth.index].value > newValue_1 ||
                    _this._widths[colWidth.index].min && _this._widths[colWidth.index].value < newValue_1 ||
                    !_this._widths[colWidth.index].limit) {
                    _this._adjustColumnWidth(colWidth.index, newValue_1);
                    adjustedNumber_1++;
                }
            });
            // if there are still columns that need to be recalculated, we start over
            var /** @type {?} */ newFlexibleWidths = this._widths.filter(function (width) {
                return !width.limit && !width.max;
            }).length;
            if (newFlexibleWidths !== adjustedNumber_1 && newFlexibleWidths !== flexibleWidths) {
                this._adjustColumnWidhts();
            }
        }
    };
    /**
     * Adjusts a single column to see if it can be recalculated
     * @param {?} index
     * @param {?} value
     * @return {?}
     */
    TdDataTableComponent.prototype._adjustColumnWidth = function (index, value) {
        this._widths[index] = {
            value: value,
            index: index,
            limit: false,
            min: false,
            max: false,
        };
        // flag to see if we need to skip the min width projection
        // depending if a width or min width has been provided
        var /** @type {?} */ skipMinWidthProjection = false;
        if (this.columns[index]) {
            // if the provided width has min/max, then we check to see if we need to set it
            if (typeof this.columns[index].width === 'object') {
                var /** @type {?} */ widthOpts = (this.columns[index].width);
                // if the column width is less than the configured min, we override it
                skipMinWidthProjection = (widthOpts && !!widthOpts.min);
                if (widthOpts && widthOpts.min >= this._widths[index].value) {
                    this._widths[index].value = widthOpts.min;
                    this._widths[index].min = true;
                    // if the column width is more than the configured max, we override it
                }
                else if (widthOpts && widthOpts.max <= this._widths[index].value) {
                    this._widths[index].value = widthOpts.max;
                    this._widths[index].max = true;
                }
                // if it has a fixed width, then we just set it
            }
            else if (typeof this.columns[index].width === 'number') {
                this._widths[index].value = /** @type {?} */ (this.columns[index].width);
                skipMinWidthProjection = this._widths[index].limit = true;
            }
        }
        // if there wasn't any width or min width provided, we set a min to what the column width min should be
        if (!skipMinWidthProjection &&
            this._widths[index].value < this._colElements.toArray()[index].projectedWidth) {
            this._widths[index].value = this._colElements.toArray()[index].projectedWidth;
            this._widths[index].min = true;
            this._widths[index].limit = false;
        }
    };
    /**
     * Generic method to calculate column width
     * @return {?}
     */
    TdDataTableComponent.prototype._calculateWidth = function () {
        var /** @type {?} */ renderedColumns = this.columns.filter(function (col) { return !col.hidden; });
        return Math.floor(this.hostWidth / renderedColumns.length);
    };
    /**
     * Method to calculate the rows to be rendered in the viewport
     * @return {?}
     */
    TdDataTableComponent.prototype._calculateVirtualRows = function () {
        var _this = this;
        var /** @type {?} */ scrolledRows = 0;
        if (this._data) {
            this._totalHeight = 0;
            var /** @type {?} */ rowHeightSum_1 = 0;
            // loop through all rows to see if we have their height cached
            // and sum them all to calculate the total height
            this._data.forEach(function (d, i) {
                // iterate through all rows at first and assume all
                // rows are the same height as the first one
                if (!_this._rowHeightCache[i]) {
                    _this._rowHeightCache[i] = _this._rowHeightCache[0] || TD_VIRTUAL_DEFAULT_ROW_HEIGHT;
                }
                rowHeightSum_1 += _this._rowHeightCache[i];
                // check how many rows have been scrolled
                if (_this._scrollVerticalOffset - rowHeightSum_1 > 0) {
                    scrolledRows++;
                }
            });
            this._totalHeight = rowHeightSum_1;
            // set the initial row to be rendered taking into account the row offset
            var /** @type {?} */ fromRow = scrolledRows - TD_VIRTUAL_OFFSET;
            this._fromRow = fromRow > 0 ? fromRow : 0;
            var /** @type {?} */ hostHeight = this._hostHeight;
            var /** @type {?} */ index = 0;
            // calculate how many rows can fit in the viewport
            while (hostHeight > 0) {
                hostHeight -= this._rowHeightCache[this.fromRow + index];
                index++;
            }
            // set the last row to be rendered taking into account the row offset
            var /** @type {?} */ range = (index - 1) + (TD_VIRTUAL_OFFSET * 2);
            var /** @type {?} */ toRow = range + this.fromRow;
            // if last row is greater than the total length, then we use the total length
            if (isFinite(toRow) && toRow > this._data.length) {
                toRow = this._data.length;
            }
            else if (!isFinite(toRow)) {
                toRow = TD_VIRTUAL_OFFSET;
            }
            this._toRow = toRow;
        }
        else {
            this._totalHeight = 0;
            this._fromRow = 0;
            this._toRow = 0;
        }
        var /** @type {?} */ offset = 0;
        // calculate the proper offset depending on how many rows have been scrolled
        if (scrolledRows > TD_VIRTUAL_OFFSET) {
            for (var /** @type {?} */ index = 0; index < this.fromRow; index++) {
                offset += this._rowHeightCache[index];
            }
        }
        this._offsetTransform = this._domSanitizer.bypassSecurityTrustStyle('translateY(' + (offset - this.totalHeight) + 'px)');
        if (this._data) {
            this._virtualData = this.data.slice(this.fromRow, this.toRow);
        }
        // mark for check at the end of the queue so we are sure
        // that the changes will be marked
        Promise.resolve().then(function () {
            _this._changeDetectorRef.markForCheck();
        });
    };
    return TdDataTableComponent;
}(_TdDataTableMixinBase));
TdDataTableComponent.decorators = [
    { type: core.Component, args: [{
                providers: [{
                        provide: forms.NG_VALUE_ACCESSOR,
                        useExisting: core.forwardRef(function () { return TdDataTableComponent; }),
                        multi: true,
                    }],
                selector: 'td-data-table',
                styles: [":host{\n  display:block;\n  overflow:hidden; }\n  :host .td-data-table-scrollable{\n    position:relative;\n    overflow:auto;\n    height:calc(100% - 56px); }\ntable.td-data-table{\n  width:auto !important; }\n  table.td-data-table.mat-selectable tbody > tr.td-data-table-row{\n    -webkit-transition:background-color 0.2s;\n    transition:background-color 0.2s; }\n  table.td-data-table.mat-selectable .td-data-table-column:first-child > .td-data-table-column-content-wrapper,\n  table.td-data-table.mat-selectable th.td-data-table-column:first-child > .td-data-table-column-content-wrapper,\n  table.td-data-table.mat-selectable td.td-data-table-cell:first-child > .td-data-table-column-content-wrapper{\n    width:18px;\n    min-width:18px;\n    padding:0 24px; }\n  table.td-data-table.mat-selectable .td-data-table-column:nth-child(2) > .td-data-table-column-content-wrapper,\n  table.td-data-table.mat-selectable th.td-data-table-column:nth-child(2) > .td-data-table-column-content-wrapper,\n  table.td-data-table.mat-selectable td.td-data-table-cell:nth-child(2) > .td-data-table-column-content-wrapper{\n    padding-left:0; }\n  [dir='rtl'] table.td-data-table.mat-selectable .td-data-table-column:nth-child(2) > .td-data-table-column-content-wrapper, [dir='rtl']\n  table.td-data-table.mat-selectable th.td-data-table-column:nth-child(2) > .td-data-table-column-content-wrapper, [dir='rtl']\n  table.td-data-table.mat-selectable td.td-data-table-cell:nth-child(2) > .td-data-table-column-content-wrapper{\n    padding-right:0;\n    padding-left:28px; }\n  table.td-data-table td.mat-checkbox-cell,\n  table.td-data-table th.mat-checkbox-column{\n    min-width:42px;\n    width:42px;\n    font-size:0 !important; }\n    table.td-data-table td.mat-checkbox-cell mat-pseudo-checkbox,\n    table.td-data-table th.mat-checkbox-column mat-pseudo-checkbox{\n      width:18px;\n      height:18px; }\n      ::ng-deep table.td-data-table td.mat-checkbox-cell mat-pseudo-checkbox.mat-pseudo-checkbox-checked::after, ::ng-deep\n      table.td-data-table th.mat-checkbox-column mat-pseudo-checkbox.mat-pseudo-checkbox-checked::after{\n        width:11px !important;\n        height:4px !important; }\n    table.td-data-table td.mat-checkbox-cell mat-checkbox ::ng-deep .mat-checkbox-inner-container,\n    table.td-data-table th.mat-checkbox-column mat-checkbox ::ng-deep .mat-checkbox-inner-container{\n      width:18px;\n      height:18px;\n      margin:0; }\n"],
                template: "<table td-data-table\n        [style.left.px]=\"columnsLeftScroll\"\n        [class.mat-selectable]=\"selectable\">\n  <thead class=\"td-data-table-head\">\n    <tr td-data-table-column-row>\n      <th td-data-table-column class=\"mat-checkbox-column\" *ngIf=\"selectable\">\n        <mat-checkbox\n          #checkBoxAll\n          *ngIf=\"multiple\"\n          [disabled]=\"!hasData\"\n          [indeterminate]=\"indeterminate && !allSelected && hasData\"\n          [checked]=\"allSelected && hasData\"\n          (click)=\"blockEvent($event); selectAll(!checkBoxAll.checked)\"\n          (keyup.enter)=\"selectAll(!checkBoxAll.checked)\"\n          (keyup.space)=\"selectAll(!checkBoxAll.checked)\"\n          (keydown.space)=\"blockEvent($event)\">\n        </mat-checkbox>\n      </th>\n      <th td-data-table-column\n          #columnElement\n          *ngFor=\"let column of columns; let i = index;\"\n          [style.min-width.px]=\"getColumnWidth(i)\"\n          [style.max-width.px]=\"getColumnWidth(i)\"\n          [name]=\"column.name\"\n          [numeric]=\"column.numeric\"\n          [active]=\"(column.sortable || sortable) && column === sortByColumn\"\n          [sortable]=\"column.sortable || (sortable && column.sortable !== false)\"\n          [sortOrder]=\"sortOrderEnum\"\n          [hidden]=\"column.hidden\"\n          (sortChange)=\"handleSort(column)\">\n          <span [matTooltip]=\"column.tooltip\">{{column.label}}</span>\n      </th>\n    </tr>\n  </thead>\n</table>\n<div #scrollableDiv class=\"td-data-table-scrollable\"\n      (scroll)=\"handleScroll($event)\">\n  <div [style.height.px]=\"totalHeight\"></div>\n  <table td-data-table\n          [style.transform]=\"offsetTransform\"\n          [style.position]=\"'absolute'\"\n          [class.mat-selectable]=\"selectable\"\n          [class.mat-clickable]=\"clickable\">\n    <tbody class=\"td-data-table-body\">\n      <tr td-data-table-row\n          #dtRow\n          [tabIndex]=\"selectable ? 0 : -1\"\n          [selected]=\"(clickable || selectable) && isRowSelected(row)\"\n          *ngFor=\"let row of virtualData; let rowIndex = index\"\n          (click)=\"handleRowClick(row, fromRow + rowIndex, $event)\"\n          (keyup)=\"selectable && _rowKeyup($event, row, rowIndex)\"\n          (keydown.space)=\"blockEvent($event)\"\n          (keydown.shift.space)=\"blockEvent($event)\"\n          (keydown.shift)=\"disableTextSelection()\"\n          (keyup.shift)=\"enableTextSelection()\">\n        <td td-data-table-cell class=\"mat-checkbox-cell\" *ngIf=\"selectable\">\n          <mat-pseudo-checkbox\n            [state]=\"dtRow.selected ? 'checked' : 'unchecked'\"\n            (mousedown)=\"disableTextSelection()\"\n            (mouseup)=\"enableTextSelection()\"\n            stopRowClick\n            (click)=\"select(row, $event, fromRow + rowIndex)\">\n          </mat-pseudo-checkbox>\n        </td>\n        <td td-data-table-cell\n            [numeric]=\"column.numeric\"\n            [hidden]=\"column.hidden\"\n            *ngFor=\"let column of columns; let i = index\"\n            [style.min-width.px]=\"getColumnWidth(i)\"\n            [style.max-width.px]=\"getColumnWidth(i)\">\n          <span *ngIf=\"!getTemplateRef(column.name)\">{{column.format ? column.format(getCellValue(column, row)) : getCellValue(column, row)}}</span>\n          <ng-template\n            *ngIf=\"getTemplateRef(column.name)\"\n            [ngTemplateOutlet]=\"getTemplateRef(column.name)\"\n            [ngTemplateOutletContext]=\"{ value: getCellValue(column, row), row: row, column: column.name }\">\n          </ng-template>\n        </td>\n      </tr>\n    </tbody>\n  </table>\n</div>\n<ng-content></ng-content>\n",
                inputs: ['value'],
                changeDetection: core.ChangeDetectionStrategy.OnPush,
            },] },
];
/** @nocollapse */
TdDataTableComponent.ctorParameters = function () { return [
    { type: undefined, decorators: [{ type: core.Optional }, { type: core.Inject, args: [platformBrowser.DOCUMENT,] },] },
    { type: core.ElementRef, },
    { type: platformBrowser.DomSanitizer, },
    { type: core.ChangeDetectorRef, },
]; };
TdDataTableComponent.propDecorators = {
    "_templates": [{ type: core.ContentChildren, args: [TdDataTableTemplateDirective,] },],
    "_scrollableDiv": [{ type: core.ViewChild, args: ['scrollableDiv',] },],
    "_colElements": [{ type: core.ViewChildren, args: ['columnElement',] },],
    "_rows": [{ type: core.ViewChildren, args: [TdDataTableRowComponent,] },],
    "data": [{ type: core.Input, args: ['data',] },],
    "columns": [{ type: core.Input, args: ['columns',] },],
    "selectable": [{ type: core.Input, args: ['selectable',] },],
    "clickable": [{ type: core.Input, args: ['clickable',] },],
    "multiple": [{ type: core.Input, args: ['multiple',] },],
    "sortable": [{ type: core.Input, args: ['sortable',] },],
    "sortBy": [{ type: core.Input, args: ['sortBy',] },],
    "sortOrder": [{ type: core.Input, args: ['sortOrder',] },],
    "onSortChange": [{ type: core.Output, args: ['sortChange',] },],
    "onRowSelect": [{ type: core.Output, args: ['rowSelect',] },],
    "onRowClick": [{ type: core.Output, args: ['rowClick',] },],
    "onSelectAll": [{ type: core.Output, args: ['selectAll',] },],
    "compareWith": [{ type: core.Input, args: ['compareWith',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
/**
 * @record
 */
var TdDataTableColumnComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     */
    function TdDataTableColumnComponent(_elementRef, _renderer) {
        this._elementRef = _elementRef;
        this._renderer = _renderer;
        this._sortOrder = TdDataTableSortingOrder.Ascending;
        /**
         * name?: string
         * Sets unique column [name] for [sortable] events.
         */
        this.name = '';
        /**
         * sortable?: boolean
         * Enables sorting events, sort icons and active column states.
         * Defaults to 'false'
         */
        this.sortable = false;
        /**
         * active?: boolean
         * Sets column to active state when 'true'.
         * Defaults to 'false'
         */
        this.active = false;
        /**
         * numeric?: boolean
         * Makes column follow the numeric data-table specs and sort icon.
         * Defaults to 'false'
         */
        this.numeric = false;
        /**
         * sortChange?: function
         * Event emitted when the column headers are clicked. [sortable] needs to be enabled.
         * Emits an [ITdDataTableSortChangeEvent] implemented object.
         */
        this.onSortChange = new core.EventEmitter();
        this._renderer.addClass(this._elementRef.nativeElement, 'td-data-table-column');
    }
    Object.defineProperty(TdDataTableColumnComponent.prototype, "projectedWidth", {
        /**
         * @return {?}
         */
        get: function () {
            if (this._columnContent && this._columnContent.nativeElement) {
                return ((this._columnContent.nativeElement)).getBoundingClientRect().width;
            }
            return 100;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableColumnComponent.prototype, "sortOrder", {
        /**
         * sortOrder?: ['ASC' | 'DESC'] or TdDataTableSortingOrder
         * Sets the sort order of column.
         * Defaults to 'ASC' or TdDataTableSortingOrder.Ascending
         * @param {?} order
         * @return {?}
         */
        set: function (order) {
            var /** @type {?} */ sortOrder = order ? order.toUpperCase() : 'ASC';
            if (sortOrder !== 'DESC' && sortOrder !== 'ASC') {
                throw new Error('[sortOrder] must be empty, ASC or DESC');
            }
            this._sortOrder = sortOrder === 'ASC' ?
                TdDataTableSortingOrder.Ascending : TdDataTableSortingOrder.Descending;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableColumnComponent.prototype, "bindClickable", {
        /**
         * @return {?}
         */
        get: function () {
            return this.sortable;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableColumnComponent.prototype, "bingSortable", {
        /**
         * @return {?}
         */
        get: function () {
            return this.sortable;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableColumnComponent.prototype, "bindActive", {
        /**
         * @return {?}
         */
        get: function () {
            return this.active;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TdDataTableColumnComponent.prototype, "bindNumeric", {
        /**
         * @return {?}
         */
        get: function () {
            return this.numeric;
        },
        enumerable: true,
        configurable: true
    });
    /**
     * Listening to click event on host to throw a sort event
     * @return {?}
     */
    TdDataTableColumnComponent.prototype.handleClick = function () {
        if (this.sortable) {
            this.onSortChange.emit({ name: this.name, order: this._sortOrder });
        }
    };
    /**
     * @return {?}
     */
    TdDataTableColumnComponent.prototype.isAscending = function () {
        return this._sortOrder === TdDataTableSortingOrder.Ascending;
    };
    /**
     * @return {?}
     */
    TdDataTableColumnComponent.prototype.isDescending = function () {
        return this._sortOrder === TdDataTableSortingOrder.Descending;
    };
    return TdDataTableColumnComponent;
}());
TdDataTableColumnComponent.decorators = [
    { type: core.Component, args: [{
                /* tslint:disable-next-line */
                selector: 'th[td-data-table-column]',
                styles: [":host{\n  white-space:nowrap;\n  position:relative;\n  padding:0;\n  vertical-align:middle;\n  text-align:left; }\n  :host > .td-data-table-heading{\n    padding:0 28px; }\n  :host:first-child > .td-data-table-heading{\n    padding-left:24px;\n    padding-right:initial; }\n    html[dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    :host:first-child > .td-data-table-heading bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:first-child > .td-data-table-heading bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    html[dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:first-child > .td-data-table-heading{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    :host:first-child > .td-data-table-heading bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:first-child > .td-data-table-heading bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n  :host:last-child > .td-data-table-heading{\n    padding-left:28px;\n    padding-right:24px; }\n    html[dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    :host:last-child > .td-data-table-heading bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:last-child > .td-data-table-heading bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    html[dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:last-child > .td-data-table-heading{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    :host:last-child > .td-data-table-heading bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:last-child > .td-data-table-heading bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n  :host mat-icon{\n    height:16px;\n    width:16px;\n    font-size:16px !important;\n    line-height:16px !important; }\n    :host mat-icon.td-data-table-sort-icon{\n      opacity:0;\n      -webkit-transition:-webkit-transform 0.25s;\n      transition:-webkit-transform 0.25s;\n      transition:transform 0.25s;\n      transition:transform 0.25s, -webkit-transform 0.25s;\n      position:absolute;\n      top:0; }\n      :host mat-icon.td-data-table-sort-icon.mat-asc{\n        -webkit-transform:rotate(0deg);\n                transform:rotate(0deg); }\n      :host mat-icon.td-data-table-sort-icon.mat-desc{\n        -webkit-transform:rotate(180deg);\n                transform:rotate(180deg); }\n  :host:hover.mat-sortable mat-icon.td-data-table-sort-icon,\n  :host.mat-active.mat-sortable mat-icon.td-data-table-sort-icon{\n    opacity:1; }\n  html[dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  body[dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  [dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  :host bdo[dir=rtl]{\n    direction:rtl;\n    unicode-bidi:bidi-override; }\n  :host bdo[dir=ltr]{\n    direction:ltr;\n    unicode-bidi:bidi-override; }\n  :host > *{\n    vertical-align:middle; }\n  :host.mat-clickable{\n    cursor:pointer; }\n    :host.mat-clickable:focus{\n      outline:none; }\n  :host .td-data-table-heading{\n    display:inline-block;\n    position:relative; }\n  :host.mat-numeric{\n    text-align:right; }\n    html[dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    [dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    :host.mat-numeric bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host.mat-numeric bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    :host.mat-numeric mat-icon.td-data-table-sort-icon{\n      margin-left:-22px;\n      margin-right:initial; }\n      html[dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-left:initial;\n        unicode-bidi:embed; }\n      body[dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-left:initial;\n        unicode-bidi:embed; }\n      [dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-left:initial;\n        unicode-bidi:embed; }\n      :host.mat-numeric mat-icon.td-data-table-sort-icon bdo[dir=rtl]{\n        direction:rtl;\n        unicode-bidi:bidi-override; }\n      :host.mat-numeric mat-icon.td-data-table-sort-icon bdo[dir=ltr]{\n        direction:ltr;\n        unicode-bidi:bidi-override; }\n      html[dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-right:-22px;\n        unicode-bidi:embed; }\n      body[dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-right:-22px;\n        unicode-bidi:embed; }\n      [dir=rtl] :host.mat-numeric mat-icon.td-data-table-sort-icon{\n        margin-right:-22px;\n        unicode-bidi:embed; }\n      :host.mat-numeric mat-icon.td-data-table-sort-icon bdo[dir=rtl]{\n        direction:rtl;\n        unicode-bidi:bidi-override; }\n      :host.mat-numeric mat-icon.td-data-table-sort-icon bdo[dir=ltr]{\n        direction:ltr;\n        unicode-bidi:bidi-override; }\n  :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n    margin-left:6px;\n    margin-right:initial; }\n    html[dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-left:initial;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-left:initial;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-left:initial;\n      unicode-bidi:embed; }\n    :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    html[dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-right:6px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-right:6px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon{\n      margin-right:6px;\n      unicode-bidi:embed; }\n    :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:not(.mat-numeric) mat-icon.td-data-table-sort-icon bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n"],
                template: "<span #columnContent class=\"td-data-table-heading\">\n  <mat-icon\n    class=\"td-data-table-sort-icon\"\n    *ngIf=\"sortable && numeric\"\n    [class.mat-asc]=\"(!(active) || isAscending())\"\n    [class.mat-desc]=\"(active && isDescending())\">\n    arrow_upward\n  </mat-icon>\n  <span>\n    <ng-content></ng-content>\n  </span>\n  <mat-icon\n    class=\"td-data-table-sort-icon\"\n    *ngIf=\"sortable && !numeric\"\n    [class.mat-asc]=\"(!(active) || isAscending())\"\n    [class.mat-desc]=\"(active && isDescending())\">\n    arrow_upward\n  </mat-icon>\n</span>\n",
            },] },
];
/** @nocollapse */
TdDataTableColumnComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
]; };
TdDataTableColumnComponent.propDecorators = {
    "_columnContent": [{ type: core.ViewChild, args: ['columnContent', { read: core.ElementRef },] },],
    "name": [{ type: core.Input, args: ['name',] },],
    "sortable": [{ type: core.Input, args: ['sortable',] },],
    "active": [{ type: core.Input, args: ['active',] },],
    "numeric": [{ type: core.Input, args: ['numeric',] },],
    "sortOrder": [{ type: core.Input, args: ['sortOrder',] },],
    "onSortChange": [{ type: core.Output, args: ['sortChange',] },],
    "bindClickable": [{ type: core.HostBinding, args: ['class.mat-clickable',] },],
    "bingSortable": [{ type: core.HostBinding, args: ['class.mat-sortable',] },],
    "bindActive": [{ type: core.HostBinding, args: ['class.mat-active',] },],
    "bindNumeric": [{ type: core.HostBinding, args: ['class.mat-numeric',] },],
    "handleClick": [{ type: core.HostListener, args: ['click',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDataTableCellComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     */
    function TdDataTableCellComponent(_elementRef, _renderer) {
        this._elementRef = _elementRef;
        this._renderer = _renderer;
        /**
         * numeric?: boolean
         * Makes cell follow the numeric data-table specs.
         * Defaults to 'false'
         */
        this.numeric = false;
        this._renderer.addClass(this._elementRef.nativeElement, 'td-data-table-cell');
    }
    Object.defineProperty(TdDataTableCellComponent.prototype, "bindNumeric", {
        /**
         * @return {?}
         */
        get: function () {
            return this.numeric;
        },
        enumerable: true,
        configurable: true
    });
    return TdDataTableCellComponent;
}());
TdDataTableCellComponent.decorators = [
    { type: core.Component, args: [{
                /* tslint:disable-next-line */
                selector: 'td[td-data-table-cell]',
                styles: [":host{\n  vertical-align:middle;\n  text-align:left;\n  padding:0; }\n  html[dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  body[dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  [dir=rtl] :host{\n    text-align:right;\n    unicode-bidi:embed; }\n  :host bdo[dir=rtl]{\n    direction:rtl;\n    unicode-bidi:bidi-override; }\n  :host bdo[dir=ltr]{\n    direction:ltr;\n    unicode-bidi:bidi-override; }\n  :host > .td-data-table-cell-content-wrapper{\n    padding:0 28px;\n    -webkit-box-sizing:border-box;\n            box-sizing:border-box;\n    display:-webkit-box;\n    display:-ms-flexbox;\n    display:flex;\n    -webkit-box-orient:horizontal;\n    -webkit-box-direction:normal;\n        -ms-flex-direction:row;\n            flex-direction:row;\n    -webkit-box-align:center;\n        -ms-flex-align:center;\n            align-items:center;\n    -ms-flex-line-pack:center;\n        align-content:center;\n    max-width:100%;\n    -webkit-box-pack:start;\n        -ms-flex-pack:start;\n            justify-content:start; }\n    :host > .td-data-table-cell-content-wrapper.td-data-table-cell-numeric{\n      -webkit-box-pack:end;\n          -ms-flex-pack:end;\n              justify-content:flex-end; }\n  :host:first-child > .td-data-table-cell-content-wrapper{\n    padding-left:24px;\n    padding-right:initial; }\n    html[dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-left:initial;\n      unicode-bidi:embed; }\n    :host:first-child > .td-data-table-cell-content-wrapper bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:first-child > .td-data-table-cell-content-wrapper bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    html[dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:first-child > .td-data-table-cell-content-wrapper{\n      padding-right:24px;\n      unicode-bidi:embed; }\n    :host:first-child > .td-data-table-cell-content-wrapper bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:first-child > .td-data-table-cell-content-wrapper bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n  :host:last-child > .td-data-table-cell-content-wrapper{\n    padding-left:28px;\n    padding-right:24px; }\n    html[dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-left:24px;\n      unicode-bidi:embed; }\n    :host:last-child > .td-data-table-cell-content-wrapper bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:last-child > .td-data-table-cell-content-wrapper bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n    html[dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    [dir=rtl] :host:last-child > .td-data-table-cell-content-wrapper{\n      padding-right:28px;\n      unicode-bidi:embed; }\n    :host:last-child > .td-data-table-cell-content-wrapper bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host:last-child > .td-data-table-cell-content-wrapper bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n  :host > *{\n    vertical-align:middle; }\n  :host.mat-clickable{\n    cursor:pointer; }\n    :host.mat-clickable:focus{\n      outline:none; }\n  :host.mat-numeric{\n    text-align:right; }\n    html[dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    body[dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    [dir=rtl] :host.mat-numeric{\n      text-align:left;\n      unicode-bidi:embed; }\n    :host.mat-numeric bdo[dir=rtl]{\n      direction:rtl;\n      unicode-bidi:bidi-override; }\n    :host.mat-numeric bdo[dir=ltr]{\n      direction:ltr;\n      unicode-bidi:bidi-override; }\n"],
                template: "<div class=\"td-data-table-cell-content-wrapper\"\n     [class.td-data-table-cell-numeric]=\"numeric\">\n  <ng-content></ng-content>\n</div>",
            },] },
];
/** @nocollapse */
TdDataTableCellComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
]; };
TdDataTableCellComponent.propDecorators = {
    "numeric": [{ type: core.Input, args: ['numeric',] },],
    "bindNumeric": [{ type: core.HostBinding, args: ['class.mat-numeric',] },],
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDataTableTableComponent = /** @class */ (function () {
    /**
     * @param {?} _elementRef
     * @param {?} _renderer
     */
    function TdDataTableTableComponent(_elementRef, _renderer) {
        this._elementRef = _elementRef;
        this._renderer = _renderer;
        this._renderer.addClass(this._elementRef.nativeElement, 'td-data-table');
    }
    return TdDataTableTableComponent;
}());
TdDataTableTableComponent.decorators = [
    { type: core.Component, args: [{
                /* tslint:disable-next-line */
                selector: 'table[td-data-table]',
                styles: [":host{\n  width:100%;\n  position:relative;\n  border-spacing:0;\n  overflow:hidden;\n  border-collapse:collapse; }\n"],
                template: "<ng-content></ng-content>",
            },] },
];
/** @nocollapse */
TdDataTableTableComponent.ctorParameters = function () { return [
    { type: core.ElementRef, },
    { type: core.Renderer2, },
]; };
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TdDataTableService = /** @class */ (function () {
    function TdDataTableService() {
    }
    /**
     * params:
     * - data: any[]
     * - searchTerm: string
     * - ignoreCase: boolean = false
     * - excludedColumns: string[] = []
     *
     * Searches [data] parameter for [searchTerm] matches and returns a new array with them.
     * @param {?} data
     * @param {?} searchTerm
     * @param {?=} ignoreCase
     * @param {?=} excludedColumns
     * @return {?}
     */
    TdDataTableService.prototype.filterData = function (data, searchTerm, ignoreCase, excludedColumns) {
        if (ignoreCase === void 0) { ignoreCase = false; }
        var /** @type {?} */ filter = searchTerm ? (ignoreCase ? searchTerm.toLowerCase() : searchTerm) : '';
        if (filter) {
            data = data.filter(function (item) {
                var /** @type {?} */ res = Object.keys(item).find(function (key) {
                    if (!excludedColumns || excludedColumns.indexOf(key) === -1) {
                        var /** @type {?} */ preItemValue = ('' + item[key]);
                        var /** @type {?} */ itemValue = ignoreCase ? preItemValue.toLowerCase() : preItemValue;
                        return itemValue.indexOf(filter) > -1;
                    }
                });
                return !(typeof res === 'undefined');
            });
        }
        return data;
    };
    /**
     * params:
     * - data: any[]
     * - sortBy: string
     * - sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending
     *
     * Sorts [data] parameter by [sortBy] and [sortOrder] and returns the sorted data.
     * @param {?} data
     * @param {?} sortBy
     * @param {?=} sortOrder
     * @return {?}
     */
    TdDataTableService.prototype.sortData = function (data, sortBy, sortOrder) {
        if (sortOrder === void 0) { sortOrder = TdDataTableSortingOrder.Ascending; }
        if (sortBy) {
            data = Array.from(data); // Change the array reference to trigger OnPush and not mutate original array
            data.sort(function (a, b) {
                var /** @type {?} */ compA = a[sortBy];
                var /** @type {?} */ compB = b[sortBy];
                var /** @type {?} */ direction = 0;
                if (!Number.isNaN(Number.parseFloat(compA)) && !Number.isNaN(Number.parseFloat(compB))) {
                    direction = Number.parseFloat(compA) - Number.parseFloat(compB);
                }
                else {
                    if (compA < compB) {
                        direction = -1;
                    }
                    else if (compA > compB) {
                        direction = 1;
                    }
                }
                return direction * (sortOrder === TdDataTableSortingOrder.Descending ? -1 : 1);
            });
        }
        return data;
    };
    /**
     * params:
     * - data: any[]
     * - fromRow: number
     * - toRow: : number
     *
     * Returns a section of the [data] parameter starting from [fromRow] and ending in [toRow].
     * @param {?} data
     * @param {?} fromRow
     * @param {?} toRow
     * @return {?}
     */
    TdDataTableService.prototype.pageData = function (data, fromRow, toRow) {
        if (fromRow >= 1) {
            data = data.slice(fromRow - 1, toRow);
        }
        return data;
    };
    return TdDataTableService;
}());
TdDataTableService.decorators = [
    { type: core.Injectable },
];
/** @nocollapse */
TdDataTableService.ctorParameters = function () { return []; };
/**
 * @param {?} parent
 * @return {?}
 */
function DATA_TABLE_PROVIDER_FACTORY(parent) {
    return parent || new TdDataTableService();
}
var DATA_TABLE_PROVIDER = {
    // If there is already a service available, use that. Otherwise, provide a new one.
    provide: TdDataTableService,
    deps: [[new core.Optional(), new core.SkipSelf(), TdDataTableService]],
    useFactory: DATA_TABLE_PROVIDER_FACTORY,
};
/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
var TD_DATA_TABLE = [
    TdDataTableComponent,
    TdDataTableTemplateDirective,
    TdDataTableColumnComponent,
    TdDataTableCellComponent,
    TdDataTableRowComponent,
    TdDataTableColumnRowComponent,
    TdDataTableTableComponent,
];
var CovalentDataTableModule = /** @class */ (function () {
    function CovalentDataTableModule() {
    }
    return CovalentDataTableModule;
}());
CovalentDataTableModule.decorators = [
    { type: core.NgModule, args: [{
                imports: [
                    common$1.CommonModule,
                    checkbox.MatCheckboxModule,
                    tooltip.MatTooltipModule,
                    icon.MatIconModule,
                    core$1.MatPseudoCheckboxModule,
                ],
                declarations: [
                    TD_DATA_TABLE,
                ],
                exports: [
                    TD_DATA_TABLE,
                ],
                providers: [
                    DATA_TABLE_PROVIDER,
                ],
            },] },
];
/** @nocollapse */
CovalentDataTableModule.ctorParameters = function () { return []; };

exports.CovalentDataTableModule = CovalentDataTableModule;
exports.TdDataTableSortingOrder = TdDataTableSortingOrder;
exports.TdDataTableBase = TdDataTableBase;
exports._TdDataTableMixinBase = _TdDataTableMixinBase;
exports.TdDataTableComponent = TdDataTableComponent;
exports.TdDataTableCellComponent = TdDataTableCellComponent;
exports.TdDataTableColumnComponent = TdDataTableColumnComponent;
exports.TdDataTableColumnRowComponent = TdDataTableColumnRowComponent;
exports.TdDataTableRowComponent = TdDataTableRowComponent;
exports.TdDataTableTableComponent = TdDataTableTableComponent;
exports.TdDataTableTemplateDirective = TdDataTableTemplateDirective;
exports.TdDataTableService = TdDataTableService;
exports.DATA_TABLE_PROVIDER_FACTORY = DATA_TABLE_PROVIDER_FACTORY;
exports.DATA_TABLE_PROVIDER = DATA_TABLE_PROVIDER;

Object.defineProperty(exports, '__esModule', { value: true });

})));
//# sourceMappingURL=covalent-core-data-table.umd.js.map
