import {HttpClient} from "@angular/common/http";
import {Compiler, Component, Inject, Injector, Input, NgModuleFactory, NgModuleFactoryLoader, OnChanges, OnDestroy, SimpleChanges, Type} from "@angular/core";
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

import {ProcessorControl} from "../../../../../../lib/feed/processor/processor-control";
import {ProcessorRef} from "../../../../../../lib/feed/processor/processor-ref";
import {UiComponentsService} from "../../../../services/UiComponentsService";
import {ProcessorTemplate} from "./processor-template";

enum State {
    ERROR = "ERROR",
    FORM = "FORM",
    LOADING = "LOADING",
    TEMPLATE = "TEMPLATE"
}

@Component({
    selector: "feed-details-processor-field",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/feed-details-processor-field.component.html"
})
export class FeedDetailsProcessorFieldComponent implements OnChanges, OnDestroy {

    @Input()
    processor: ProcessorRef;

    @Input()
    readonly: boolean;

    childInjector: Injector;
    childModule: NgModuleFactory<any>;
    childType: Type<any>;
    error: string;
    form = new FormGroup({});
    state = State.LOADING;
    statusSubscription: Subscription;
    private isSystemJsSetup = false;

    constructor(private http: HttpClient, private injector: Injector, private moduleFactoryLoader: NgModuleFactoryLoader,
                @Inject("UiComponentsService") private uiComponentsService: UiComponentsService, private _compiler: Compiler) {
    }

    ngOnDestroy(): void {
        if (this.statusSubscription != null) {
            this.statusSubscription.unsubscribe();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.processor) {
            // Unsubscribe from form status changes
            if (this.statusSubscription != null) {
                this.statusSubscription.unsubscribe();
            }

            if (changes.processor.currentValue) {
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
                            changes.processor.currentValue.control = this.form;
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
                if (changes.processor.previousValue) {
                    changes.processor.previousValue.control = null;
                }
            }
        }

        // Ensure form state matches readonly state
        if (changes.readonly && this.processor != null) {
            if (this.readonly) {
                this.processor.form.disable();
            } else {
                this.processor.form.enable();
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
            }),
            map((moduleFactory: any) => {
                // Find processor control
                const module = moduleFactory.create(this.injector);
                const processorControl = module.injector.get(ProcessorControl as any).find((control:any) => control.supportsProcessorType(this.processor.type));
                if (typeof processorControl === "undefined" || processorControl == null) {
                    throw new Error("Missing ProcessorControl provider for processor type: " + this.processor.type);
                }

                // Load component and update state
                this.childInjector = Injector.create([{provide: ProcessorRef, useValue: this.processor}], module.injector);
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

        SystemJS.registerDynamic('@angular/core', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/core');
        });
        SystemJS.registerDynamic('@angular/material/dialog', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('@angular/material/dialog');
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
        SystemJS.registerDynamic('@kylo/feed', [], true, function(_require: any, _exports: any, _module: any) {
            _module.exports = require('../../../../../../lib/feed/index');
        });
    }

}
