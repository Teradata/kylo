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

    constructor(private compiler: Compiler, private http: HttpClient, private injector: Injector, private moduleFactoryLoader: NgModuleFactoryLoader,
                @Inject("UiComponentsService") private uiComponentsService: UiComponentsService) {
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
            concatMap(template => this.moduleFactoryLoader.load(template.module)),
            map(moduleFactory => {
                const module = moduleFactory.create(this.injector);
                const processorControl = module.injector.get(ProcessorControl);
                // this.childInjector = this.injector;
                this.childInjector = Injector.create([{provide: ProcessorRef, useValue: this.processor}], this.injector);
                this.childModule = moduleFactory;
                this.childType = processorControl[0].component;
                this.state = State.TEMPLATE;
                return null;
            })
        );
    }
}
