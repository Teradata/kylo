import {HttpClient} from "@angular/common/http";
import {Compiler, Component, Inject, Injector, Input, NgModuleFactory, NgModuleFactoryLoader, OnChanges, OnDestroy, OnInit, SimpleChanges, Type} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {ArrayObservable} from "rxjs/observable/ArrayObservable";
import {empty} from "rxjs/observable/empty";
import {fromPromise} from "rxjs/observable/fromPromise";
import {catchError} from "rxjs/operators/catchError";
import {concatMap} from "rxjs/operators/concatMap";
import {filter} from "rxjs/operators/filter";
import {find} from "rxjs/operators/find";
import {map} from "rxjs/operators/map";
import {single} from "rxjs/operators/single";
import {Subscription} from "rxjs/Subscription";
import {EmptyError} from "rxjs/util/EmptyError";

import {ProcessorRef} from "../../../../../../lib";
import {UiComponentsService} from "../../../../services/UiComponentsService";
import {ProcessorTemplate} from "./processor-template";

declare const SystemJS: any;

enum State {
    ERROR = "ERROR",
    FORM = "FORM",
    LOADING = "LOADING",
    TEMPLATE = "TEMPLATE",
    EMPTY = "EMPTY"
}

@Component({
    selector: "feed-details-processor-field",
    templateUrl: "./feed-details-processor-field.component.html"
})
export class FeedDetailsProcessorFieldComponent implements OnInit, OnChanges, OnDestroy {

    @Input()
    processor: ProcessorRef;

    @Input()
    readonly: boolean;

    childInjector: Injector;
    childModule: NgModuleFactory<any>;
    childType: Type<any>;
    error: string;
    /**
     * Map of the forms associated for each processor
     * @type {{}}
     */
    forms : {[key:string]: FormGroup} = {};
    state = State.LOADING;
    statusSubscription: Subscription;
    private isSystemJsSetup = false;

    constructor(private http: HttpClient, private injector: Injector, private moduleFactoryLoader: NgModuleFactoryLoader,
                @Inject("UiComponentsService") private uiComponentsService: UiComponentsService, private _compiler: Compiler) {
    }

    ngOnInit(){
        if(this.processor == undefined){
            this.state = State.EMPTY;
        }
    }

    ngOnDestroy(): void {
        if (this.statusSubscription != null) {
            this.statusSubscription.unsubscribe();
        }
    }

    /**
     * Ensure the current processor is in the form map
     * @private
     */
    private _ensureProcessorForm(){
        if(this.forms[this.processor.id] == undefined){
            this.forms[this.processor.id] = new FormGroup({});
        }
    }

    /**
     * Get the form for the current processor
     * @return {}
     */
    getProcessorForm(){
        this._ensureProcessorForm();
        return this.forms[this.processor.id];
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.processor || (changes.readonly && changes.readonly.currentValue == false)) {
            // Unsubscribe from form status changes
            if (this.statusSubscription != null) {
                this.statusSubscription.unsubscribe();
            }

            let processor = changes.processor != undefined ? changes.processor.currentValue : this.processor;
            let previousProcessorValue = changes.processor != undefined ? changes.processor.previousValue: undefined;
            if(processor != undefined) {
                if ((changes.processor && changes.processor.currentValue) || (changes.readonly && changes.readonly.currentValue == false)) {
                    // Ensure form state matches readonly state
                    this.statusSubscription = this.processor.form.statusChanges.subscribe(status => {
                        if (this.readonly === true && status !== "DISABLED") {
                            this.processor.form.disable();
                        }
                    });


                    // Fetch template and update state
                    this.getProcessorTemplate().pipe(
                        single(),
                        catchError(err => {
                            if (err instanceof EmptyError) {
                                this.state = State.FORM;
                                processor.control = this.getProcessorForm();
                                return empty();
                            } else {
                                throw err;
                            }
                        })
                    ).subscribe(null, (err: any) => {
                        console.error(err);
                        this.error = err;
                        this.state = State.ERROR;
                    });
                } else {
                    this.childType = null;
                    this.state = State.LOADING;
                    if (previousProcessorValue) {
                        previousProcessorValue.control = null;
                    }
                }
            }
        }

        // Ensure form state matches readonly state
        if (changes.readonly && this.processor != null) {
            if (this.readonly) {
                this.processor.form.disable();
                this.getProcessorForm().disable();
            } else {
                this.processor.form.enable();
                this.getProcessorForm().enable();
            }

        }
    }

    private getProcessorTemplate(): Observable<void> {
        return fromPromise(this.uiComponentsService.getProcessorTemplates()).pipe(
            concatMap((templates: ProcessorTemplate[]) => ArrayObservable.create(templates)),
            find(template => {
                if (template.module != null && template.module != "" && (template.processorDisplayName == null || template.processorDisplayName === this.processor.name)) {
                    const match = template.processorTypes.find(type => type === this.processor.type);
                    return typeof match !== "undefined";
                } else {
                    return false;
                }
            }),
            filter(template => typeof template !== "undefined"),
            concatMap(template => {
                // const template = args[0];
                // const kyloModule = args[1];
                if (!this.isSystemJsSetup) {
                    this.setupSystemJs();
                    this.isSystemJsSetup = true;
                }
                const split = template.module.split('#');
                const module = split[0];
                const exportName = split[1];
                return SystemJS.import("js/" + module)
                    .then((module: any) => module[exportName])
                    .then((type: any) => this.checkNotEmpty(type, module, exportName))
                    .then((type: any) => this._compiler.compileModuleAsync(type));
                // .then((x: any) => [x, kyloModule]);
            }),
            concatMap(template => SystemJS.import("@kylo/feed").then(kyloModule => [template, kyloModule])),
            map((imports: any) => {
                const moduleFactory = imports[0];
                const kyloModule = imports[1];

                // Find processor control
                const module = moduleFactory.create(this.injector);
                const processorControl = module.injector.get(kyloModule.ProcessorControl as any).find((control:any) => control.supportsProcessorType(this.processor.type));
                if (typeof processorControl === "undefined" || processorControl == null) {
                    throw new Error("Missing ProcessorControl provider for processor type: " + this.processor.type);
                }

                // Load component and update state
                this.childInjector = Injector.create([{provide: kyloModule.ProcessorRef, useValue: this.processor}], module.injector);
                this.childModule = moduleFactory;
                this.childType = processorControl.component;
                this.state = State.TEMPLATE;
                return null;
            })
        );
    }

    private checkNotEmpty(value: any, modulePath: string, exportName: string): any {
        if (!value) {
            throw new Error(`Cannot find '${exportName}' in '${modulePath}'`);
        }
        return value;
    }

    private setupSystemJs() {
        SystemJS.config({
            defaultJSExtensions: true,
        });

        SystemJS.registerDynamic('angular', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../bower_components/angular/angular.min');
        });
        SystemJS.registerDynamic('@angular/core', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/core');
        });
        SystemJS.registerDynamic('@angular/material/dialog', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/dialog');
        });
        SystemJS.registerDynamic('@angular/material', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material');
        });
        SystemJS.registerDynamic('@angular/material/toolbar', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/toolbar');
        });
        SystemJS.registerDynamic('@angular/material/divider', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/divider');
        });
        SystemJS.registerDynamic('@angular/material/checkbox', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/checkbox');
        });
        SystemJS.registerDynamic('@angular/material/core', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/core');
        });
        SystemJS.registerDynamic('@angular/material/form-field', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/form-field');
        });
        SystemJS.registerDynamic('@angular/material/card', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/card');
        });
        SystemJS.registerDynamic('@angular/material/list', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/list');
        });
        SystemJS.registerDynamic('@angular/material/tabs', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/tabs');
        });
        SystemJS.registerDynamic('@angular/common/http', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/common/http');
        });
        SystemJS.registerDynamic('@angular/forms', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/forms');
        });
        SystemJS.registerDynamic('@angular/material/autocomplete', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/autocomplete');
        });
        SystemJS.registerDynamic('rxjs/operators', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators');
        });
        SystemJS.registerDynamic('rxjs/Observable', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/Observable');
        });
        SystemJS.registerDynamic('rxjs/Subscription', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/Subscription');
        });
        SystemJS.registerDynamic('rxjs/add/operator/do', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/add/operator/do');
        });
        SystemJS.registerDynamic('rxjs/add/operator/debounceTime', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/add/operator/debounceTime');
        });
        SystemJS.registerDynamic('rxjs/add/observable/merge', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/add/observable/merge');
        });
        SystemJS.registerDynamic('rxjs/observable/of', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/observable/of');
        });
        SystemJS.registerDynamic('rxjs/operators/catchError', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/catchError')
        });
        SystemJS.registerDynamic('rxjs/operators/debounceTime', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/debounceTime')
        });
        SystemJS.registerDynamic('rxjs/operators/distinctUntilChanged', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/distinctUntilChanged')
        });
        SystemJS.registerDynamic('rxjs/operators/filter', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/filter')
        });
        SystemJS.registerDynamic('rxjs/operators/map', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/map')
        });
        SystemJS.registerDynamic('rxjs/operators/skip', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/skip')
        });
        SystemJS.registerDynamic('rxjs/operators/switchMap', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../node_modules/rxjs/operators/switchMap')
        });
        SystemJS.registerDynamic('underscore', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../bower_components/underscore/underscore-min')
        });
        SystemJS.registerDynamic('@angular/common', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/common');
        });
        SystemJS.registerDynamic('@angular/flex-layout', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/flex-layout');
        });
        SystemJS.registerDynamic('@angular/material/button', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/button');
        });
        SystemJS.registerDynamic('@angular/material/icon', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/icon');
        });
        SystemJS.registerDynamic('@angular/material/input', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/input');
        });
        SystemJS.registerDynamic('@angular/material/progress-spinner', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/progress-spinner');
        });
        SystemJS.registerDynamic('@angular/material/radio', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/radio');
        });
        SystemJS.registerDynamic('@angular/material/select', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/select');
        });
        SystemJS.registerDynamic('@covalent/core/common', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@covalent/core/common');
        });
        SystemJS.registerDynamic('@ngx-translate/core', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@ngx-translate/core');
        });
        SystemJS.registerDynamic('@covalent/core/chips', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@covalent/core/chips');
        });
        SystemJS.registerDynamic('@covalent/core/dialogs', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@covalent/core/dialogs');
        });
        SystemJS.registerDynamic('@kylo/feed', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../../../../../target/classes/static/lib/bundles/kylo-feed.umd.min.js');
        });
    }

}
