import { EventEmitter, AfterViewInit, OnDestroy } from '@angular/core';
/**
 * CodeMirror component
 * Usage :
 * <codemirror [(ngModel)]="data" [config]="{...}"></codemirror>
 */
export declare class CodemirrorComponent implements AfterViewInit, OnDestroy {
    config: any;
    change: EventEmitter<{}>;
    focus: EventEmitter<{}>;
    blur: EventEmitter<{}>;
    cursorActivity: EventEmitter<{}>;
    host: any;
    instance: any;
    _value: string;
    /**
     * Constructor
     */
    constructor();
    value: string;
    /**
     * On component destroy
     */
    ngOnDestroy(): void;
    /**
     * On component view init
     */
    ngAfterViewInit(): void;
    /**
     * Initialize codemirror
     */
    codemirrorInit(config: any): void;
    /**
     * Value update process
     */
    updateValue(value: any): void;
    /**
     * Implements ControlValueAccessor
     */
    writeValue(value: any): void;
    onChange(_: any): void;
    onTouched(): void;
    registerOnChange(fn: any): void;
    registerOnTouched(fn: any): void;
}
