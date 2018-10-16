import {Component, Input} from "@angular/core";
import {AbstractControl, FormControl, FormGroup, ValidatorFn} from '@angular/forms';
import {ValidationErrors} from '@angular/forms/src/directives/validators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDialogService} from "@covalent/core/dialogs";
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {TranslateService} from "@ngx-translate/core";
import {StateService} from "@uirouter/angular";
import {fromPromise} from "rxjs/observable/fromPromise";
import {of} from "rxjs/observable/of";
import {catchError} from 'rxjs/operators/catchError';
import {concatMap} from "rxjs/operators/concatMap";
import {filter} from "rxjs/operators/filter";
import {finalize} from 'rxjs/operators/finalize';
import {map} from "rxjs/operators/map";
import {tap} from "rxjs/operators/tap";
import * as _ from "underscore";
import {EntityAccessControlService} from "../../shared/entity-access-control/EntityAccessControlService";

import {Connector} from '../api/models/connector';
import {ConnectorPlugin} from '../api/models/connector-plugin';
import {DataSource} from '../api/models/datasource';
import {DataSourceTemplate} from '../api/models/datasource-template';
import {UiOption} from '../api/models/ui-option';
import {CatalogService} from '../api/services/catalog.service';


interface UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>): void;

    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>): void;
}

class DefaultUiOptionsMapper implements UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                ds.template.paths.push(control.value);
            } else if (key === "jars") {
                ds.template.jars = (control.value.length !== 0) ? control.value.split(",") : null;
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
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                ds.template.paths.push(control.value);
            } else if (key === "jars") {
                ds.template.jars = (control.value.length !== 0) ? control.value.split(",") : null;
            } else if (key === "account-name" || key == "account-key") {
                ds.template.options["spark.hadoop.fs.azure.account.key." + controls.get("account-name").value] = controls.get("account-key").value;
            } else {
                ds.template.options[key] = control.value;
            }
        });
    }

    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>): void {
        controls.get("path").setValue(ds.template.paths[0]);
        _.keys(ds.template.options).forEach(option => {
            if (option.startsWith("spark.hadoop.fs.azure.account.key.")) {
                const accountName = option.substring("spark.hadoop.fs.azure.account.key.".length);
                controls.get("account-name").setValue(accountName);
                controls.get("account-key").setValue(ds.template.options[option]);
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

    form = new FormGroup({});

    private titleControl: FormControl;
    private controls: Map<string, FormControl> = new Map();
    private isLoading: boolean = false;
    private testError: String;
    private testStatus: boolean = false;

    // noinspection JSUnusedLocalSymbols - called dynamically when validator is created
    private jars = (params: any): ValidatorFn => {
        return (control: AbstractControl): { [key: string]: any } => {
            return (control.value as string || "").split(",")
                .map(value => {
                    if (value.trim().length > 0) {
                        try {
                            const url = new URL(value);
                            if (params && params.protocol && url.protocol !== params.protocol) {
                                return 'url-protocol';
                            }
                        } catch (e) {
                            return 'url';
                        }
                    }
                    return null;
                })
                .reduce((accumulator, value) => {
                    if (value !== null) {
                        accumulator[value] = true;
                    }
                    return accumulator;
                }, {});
        }
    };

    // noinspection JSUnusedLocalSymbols - called dynamically when validator is created
    private url = (params: any): ValidatorFn => {
        return (control: AbstractControl): { [key: string]: any } => {
            if (control.value && control.value.trim().length > 0) {
                try {
                    const url: URL = new URL(control.value);
                    if (params && params.protocol && url.protocol !== params.protocol) {
                        return {'url-protocol': true};
                    }
                } catch (e) {
                    return {'url': true};
                }
            } else {
                return null;
            }
        };
    };

    // noinspection JSUnusedLocalSymbols - called dynamically when UiOptionsMapper is created
    private azureOptionsMapper: UiOptionsMapper = new AzureUiOptionsMapper();
    private defaultOptionsMapper: UiOptionsMapper = new DefaultUiOptionsMapper();

    constructor(private state: StateService,
                private catalogService: CatalogService,
                private snackBarService: MatSnackBar,
                private loadingService: TdLoadingService,
                private dialogService: TdDialogService,
                private translateService: TranslateService,
                private entityAccessControlService: EntityAccessControlService) {
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
                this.showSnackBar("Unknown ui options mapper " + this.plugin.optionsMapperId
                    + " for connector " + this.connector.title);
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
        this.catalogService.createDataSource(ds).pipe(
            concatMap(dataSource => {
                if (typeof ds.roleMemberships !== "undefined") {
                    this.entityAccessControlService.updateRoleMembershipsForSave(ds.roleMemberships);
                    return fromPromise(this.entityAccessControlService.saveRoleMemberships("datasource", dataSource.id, ds.roleMemberships))
                        .pipe(map(() => dataSource));
                } else {
                    return of(dataSource);
                }
            }),
            finalize(() => {
                this.isLoading = false;
                this.loadingService.resolve(ConnectorComponent.topOfPageLoader);
            })
        ).subscribe(
            source => this.state.go("catalog.datasources", {datasourceId: source.id}),
            err => {
                console.error(err);
                this.showSnackBar('Failed to save. ', err.message);
            }
        );
    }

    getDataSourceFromUi(): DataSource | undefined {
        const optionsMapper = <UiOptionsMapper>this[this.plugin.optionsMapperId || "defaultOptionsMapper"];
        if (!optionsMapper) {
            this.showSnackBar("Unknown ui options mapper " + this.plugin.optionsMapperId
                + " for connector " + this.connector.title);
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
            .open(msg + ' ' + (err ? err : ""), 'OK', {duration: 5000});
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
        return (control.value || '').trim().length === 0 ? {'required': true} : null;
    }

    cancel() {
        this.state.go("catalog.datasources");
    }

    /**
     * Delete datasource
     */
    deleteDatasource() {
        this.translateService.get("CATALOG.DATA_SOURCES.CONFIRM_DELETE").pipe(
            concatMap(messages => {
                return this.dialogService.openConfirm({
                    message: messages["MESSAGE"],
                    title: messages["TITLE"],
                    acceptButton: messages["ACCEPT"],
                    cancelButton: messages["CANCEL"]
                }).afterClosed()
            }),
            filter(accept => accept),
            tap(() => this.loadingService.register(ConnectorComponent.topOfPageLoader)),
            concatMap(() => this.catalogService.deleteDataSource(this.datasource)),
            finalize(() => this.loadingService.resolve(ConnectorComponent.topOfPageLoader))
        ).subscribe(
            () => this.state.go("catalog.datasources", {}, {reload: true}),
            err => this.showSnackBar('Failed to delete.', err.message)
        );
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
