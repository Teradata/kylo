import {Component, Input} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Connector} from '../api/models/connector';
import {AbstractControl, FormControl, ValidatorFn} from '@angular/forms';
import {UiOption} from '../api/models/ui-option';
import {DataSource} from '../api/models/datasource';
import {DataSourceTemplate} from '../api/models/datasource-template';
import {CatalogService} from '../api/services/catalog.service';
import {finalize} from 'rxjs/operators/finalize';
import {catchError} from 'rxjs/operators/catchError';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ValidationErrors} from '@angular/forms/src/directives/validators';


interface UiOptionsMapper {
    mapOptions(ds: DataSource, controls: Map<string, FormControl>): void;
}

class DefaultUiOptionsMapper implements UiOptionsMapper {
    mapOptions(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                ds.template.paths.push(control.value);
            } else {
                ds.template.options[key] = control.value;
            }
        });
    }
}

class AzureUiOptionsMapper implements UiOptionsMapper {
    mapOptions(ds: DataSource, controls: Map<string, FormControl>): void {
        ds.template.paths.push(controls.get("path").value);
        ds.template.options["spark.hadoop.fs.azure.account.key." + controls.get("account-name").value] = controls.get("account-key").value;
    }
}

/**
 * Displays selected connector properties.
 */
@Component({
    selector: "catalog-connector",
    styleUrls: ["js/feed-mgr/catalog/connector/connector.component.css"],
    templateUrl: "js/feed-mgr/catalog/connector/connector.component.html"
})
export class ConnectorComponent {

    static LOADER = "ConnectorComponent.LOADER";
    private static topOfPageLoader: string = "ConnectorComponent.topOfPageLoader";

    @Input("connector")
    public connector: Connector;

    private titleControl: FormControl;
    private controls: Map<string, FormControl> = new Map();
    private isLoading: boolean = false;

    // noinspection JSUnusedLocalSymbols - called dynamically when validator is created
    private url = (params: any): ValidatorFn => {
        return (control: AbstractControl): { [key: string]: any } => {
            if (control.value && control.value.trim().length > 0) {
                try {
                    const url: URL = new URL(control.value);
                    if (url.protocol !== params.protocol) {
                        return { 'url-protocol': true };
                    }
                } catch (e) {
                    return { 'url': true };
                }
            } else {
                return null;
            }
        };
    };

    // noinspection JSUnusedLocalSymbols - called dynamically when UiOptionsMapper is created
    private azureOptionsMapper: UiOptionsMapper = new AzureUiOptionsMapper();
    private defaultOptionsMapper: UiOptionsMapper = new DefaultUiOptionsMapper();

    constructor(private state: StateService, private catalogService: CatalogService,
                private snackBarService: MatSnackBar,
                private loadingService: TdLoadingService) {
        this.titleControl = new FormControl('', ConnectorComponent.required);

        this.loadingService.create({
            name: ConnectorComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }

    public ngOnInit() {
    }

    /**
     * Creates a new datasource for this Connector
     */
    createDatasource() {
        const ds = new DataSource();
        ds.title = this.titleControl.value;
        ds.connector = this.connector;
        ds.template = new DataSourceTemplate();
        ds.template.paths = [];
        ds.template.options = {};
        const optionsMapper = <UiOptionsMapper>this[this.connector.optionsMapperId || "defaultOptionsMapper"];
        if (optionsMapper) {
            optionsMapper.mapOptions(ds, this.controls);
        } else {
            this.showSnackBar("Unknown ui options mapper " + this.connector.optionsMapperId
                + " for connector " + this.connector.title);
            return;
        }

        this.isLoading = true;
        this.loadingService.register(ConnectorComponent.topOfPageLoader);
        this.catalogService.createDataSource(ds)
            .pipe(finalize(() => {
                this.isLoading = false;
                this.loadingService.resolve(ConnectorComponent.topOfPageLoader);
            }))
            .pipe(catchError((err) => {
                this.showSnackBar(err.message);
                return [];
            }))
            .subscribe((source: DataSource) => {
                this.state.go("catalog.datasource", {datasourceId: source.id});
            });
    }

    showSnackBar(err: string): void {
        this.snackBarService
            .open('Failed to save. ' + (err ? err : ""), 'OK', { duration: 5000 });
    }

    isInputType(option: UiOption): boolean {
        return option.type === undefined || option.type === '' || option.type === "input" || option.type === "password";
    }

    isSelectType(option: UiOption): boolean {
        return option.type === "select";
    }

    isFormInvalid(): boolean {
        return this.titleControl.invalid || Array.from(this.controls.values())
            .map(item => item.invalid)
            .reduce((prev, current) => {
                    return prev || current;
                }, false
            );
    }

    getControl(option: UiOption) {
        let control = this.controls.get(option.key);
        if (!control) {
            const validators = [];
            if (option.required === undefined || option.required === true) {
                validators.push(ConnectorComponent.required);
            }
            for (let validator of option.validators || []) {
                if (this[validator.type]) {
                    validators.push(this[validator.type](validator.params));
                } else {
                    console.error('Unknown validator type ' + validator.type);
                }
            }
            control = new FormControl('', validators);
            if (option.value) {
                control.setValue(option.value);
            }
            this.controls.set(option.key, control);
        }
        return control;
    }

    /**
     * Validation function which does not allow only empty space
     * @param {AbstractControl} control
     * @returns {ValidationErrors}
     */
    private static required(control: AbstractControl): ValidationErrors {
        return (control.value || '').trim().length === 0 ? { 'required': true } : null;
    }


}
