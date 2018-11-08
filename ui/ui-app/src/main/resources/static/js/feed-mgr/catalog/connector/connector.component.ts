import {Component, Input} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Connector} from '../api/models/connector';
import {ConnectorPlugin} from '../api/models/connector-plugin';
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
import * as _ from "underscore";
import { TranslateService } from "@ngx-translate/core";


interface UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>): void;
    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>): void;
}

class DefaultUiOptionsMapper implements UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                ds.template.paths.push(control.value);
            } else {
                ds.template.options[key] = control.value;
            }
        });
    }
    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                control.setValue(ds.template.paths[0]);
            } else {
                control.setValue(ds.template.options[key]);
            }
        });
    }
}

class AzureUiOptionsMapper implements UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>): void {
        ds.template.paths.push(controls.get("path").value);
        ds.template.options["spark.hadoop.fs.azure.account.key." + controls.get("account-name").value] = controls.get("account-key").value;
    }
    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.get("path").setValue(ds.template.paths[0]);
        _.keys(ds.template.options).forEach(option => {
            if (option.startsWith("spark.hadoop.fs.azure.account.key.")) {
                const accountName = option.substring("spark.hadoop.fs.azure.account.key.".length);
                controls.get("account-name").setValue(accountName);
            }
        });
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

    @Input("datasource")
    public datasource: DataSource;

    @Input("connector")
    public connector: Connector;

    @Input("connectorPlugin")
    public plugin: ConnectorPlugin;

    private titleControl: FormControl;
    private controls: Map<string, FormControl> = new Map();
    private isLoading: boolean = false;
    private testError: String;
    private testStatus: boolean = false;

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
                private loadingService: TdLoadingService,
                private translate : TranslateService) {
        this.titleControl = new FormControl('', ConnectorComponent.required);

        this.loadingService.create({
            name: ConnectorComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }

    public ngOnInit() {
        this.createControls();
        this.initControls();
    }

    initControls() {
        if (this.datasource) {
            this.titleControl.setValue(this.datasource.title);
            const optionsMapper = <UiOptionsMapper>this[this.plugin.optionsMapperId || "defaultOptionsMapper"];
            if (optionsMapper) {
                optionsMapper.mapFromModelToUi(this.datasource, this.controls);
            } else {
                this.showSnackBar(this.translate.instant('FEEDMGR.connector.component.unknown.ui.options',
                                {mapper:this.plugin.optionsMapperId,connector:this.connector.title}));
                return;
            }
        }
    }

    createControls() {
        const options = this.plugin.options;
        if (!options) {
            return;
        }
        for (let option of options) {
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
        }
    }

    /**
     * Creates a new datasource or updates an existing one
     */
    saveDatasource() {
        const ds = this.getDataSourceFromUi();
        if (ds === undefined) {
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
                this.showSnackBar(this.translate.instant('views.common.save.failed',{entity : ''}), err.message);
                return [];
            }))
            .subscribe((source: DataSource) => {
                this.state.go("catalog.datasources", {datasourceId: source.id});
            });
    }

    getDataSourceFromUi(): DataSource | undefined {
        const optionsMapper = <UiOptionsMapper>this[this.plugin.optionsMapperId || "defaultOptionsMapper"];
        if (!optionsMapper) {
            this.showSnackBar(this.translate.instant('FEEDMGR.connector.component.unknown.ui.options',
                                {mapper:this.plugin.optionsMapperId,connector:this.connector.title}));
            return undefined;
        }

        const ds = this.datasource ? this.datasource : new DataSource();
        ds.title = this.titleControl.value;
        ds.connector = this.connector;
        ds.template = new DataSourceTemplate();
        ds.template.paths = [];
        ds.template.options = {};
        optionsMapper.mapFromUiToModel(ds, this.controls);
        return ds;
    }


    showSnackBar(msg: string, err?: string): void {
        this.snackBarService
            .open(msg + ' ' + (err ? err : ""), 'OK', { duration: 5000 });
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
        return this.controls.get(option.key);
    }

    /**
     * Validation function which does not allow only empty space
     * @param {AbstractControl} control
     * @returns {ValidationErrors}
     */
    private static required(control: AbstractControl): ValidationErrors {
        return (control.value || '').trim().length === 0 ? { 'required': true } : null;
    }

    cancel() {
        this.state.go("catalog.datasources");
    }

    /**
     * Delete datasource
     */
    deleteDatasource() {
        this.loadingService.register(ConnectorComponent.topOfPageLoader);
        this.catalogService.deleteDataSource(this.datasource)
            .pipe(finalize(() => {
                this.loadingService.resolve(ConnectorComponent.topOfPageLoader);
            }))
            .pipe(catchError((err) => {
                this.showSnackBar(this.translate.instant('views.common.delete.failure', {entity : ''}), err.message);
                return [];
            }))
            .subscribe(() => {
                this.state.go("catalog.datasources", {}, {reload: true});
            });
    }

    /**
     * Validates data source connection
     */
    test() {
        this.testStatus = false;
        this.testError = undefined;
        const ds = this.getDataSourceFromUi();
        if (ds === undefined) {
            return;
        }

        this.loadingService.register(ConnectorComponent.topOfPageLoader);
        this.catalogService.testDataSource(ds)
            .pipe(finalize(() => {
                this.loadingService.resolve(ConnectorComponent.topOfPageLoader);
                this.testStatus = true;
            }))
            .pipe(catchError((err) => {
                this.testError = err.error.developerMessage ? err.error.developerMessage : err.error.message;
                return [];
            }))
            .subscribe(() => {
                console.log('validated');
            });
    }

    isTestAvailable(): boolean {
        const tabs = this.plugin.tabs;
        return tabs && (".browse" === tabs[0].sref || ".connection" === tabs[0].sref);
    }
}
