import {Compiler, Component, ComponentRef, Injector, Input, NgModuleFactory, NgModuleFactoryLoader, OnChanges, OnInit, SimpleChanges, Type, ViewContainerRef} from "@angular/core";
import "rxjs/add/observable/forkJoin";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/finally";
import "rxjs/add/operator/map";
import {Observable} from "rxjs/Observable";

declare const SystemJS: any;

@Component({
    selector: "lazy-load",
    template: `
      <ng-template tdLoading [tdLoadingUntil]="!loading">
        <td-message *ngIf="error != null" label="Error!" [sublabel]="error" color="warn" icon="error"></td-message>
      </ng-template>
    `
})
export class LazyLoadComponent implements OnInit {

    /**
     * Entry component to display
     */
    @Input("component")
    public componentName: string;

    /**
     * Object of @Input properties to set on component
     */
    @Input()
    public inputs: object;

    /**
     * Path to the module containing the entry component
     */
    @Input("module")
    public moduleName: string;

    /**
     * Error message if component cannot be resolved
     */
    error: string;

    /**
     * Indicates the component is being resolved
     */
    loading = true;

    /**
     * Component reference
     */
    private component: ComponentRef<any>;

    constructor(private injector: Injector, private viewRef: ViewContainerRef, private compiler: Compiler, private loader: NgModuleFactoryLoader) {
    }

    public ngOnInit(): void {
        Observable.forkJoin(this.loadModule(), this.loadComponent())
            .map(([moduleFactory, componentType]) => this.createComponent(componentType, moduleFactory))
            .catch(err => {
                console.log("Failed to load component '" + this.componentName + ": " + err);
                this.error = "This component could not be loaded.";
                throw err;
            })
            .finally(() => this.loading = false)
            .subscribe();
    }

    createComponent(componentType: Type<any>, moduleFactory: NgModuleFactory<any>) {
        // Create component
        const moduleRef = moduleFactory.create(this.injector);
        const componentFactory = moduleRef.componentFactoryResolver.resolveComponentFactory(componentType);
        this.component = this.viewRef.createComponent(componentFactory);

        // Set inputs
        if (this.inputs) {
            Object.keys(this.inputs).forEach(key => {
                this.component.instance[key] = this.inputs[key];
            });
        }

        // Return component
        return this.component;
    }

    loadComponent(): Promise<Type<any>> {
        const [path, exportName] = this.componentName.split("#");
        return SystemJS.import(path)
            .then((exports: any) => exports[exportName]);
    }

    loadModule(): Promise<NgModuleFactory<any>> {
        return this.loader.load(this.moduleName);
    }
}
