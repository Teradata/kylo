import {Component, Input, ViewChild} from "@angular/core";
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
import {AccessControlService} from "../../../services/AccessControlService";
import {EntityAccessControlService} from "../../shared/entity-access-control/EntityAccessControlService";

import {Connector} from '../api/models/connector';
import {ConnectorPlugin} from '../api/models/connector-plugin';
import {CreateDataSource, DataSource} from '../api/models/datasource';
import {DataSourceTemplate} from '../api/models/datasource-template';
import {UiOption} from '../api/models/ui-option';
import {CatalogService} from '../api/services/catalog.service';
import {MatSelectionListChange, MatSelectionList} from "@angular/material/list";
import {HttpErrorResponse} from "@angular/common/http";


interface UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void;

    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void;
}

/**
 * Class to track original property values and changes associated with each input.
 * Sensitive properties will be initiall rendered with 5 ***** instead of the encrypted value
 */
export class OptionValue {
    modifiedValue:string;
    modified:boolean = false;

    constructor(public key:string, public originalValue:string, public uiOption:UiOption){
        if(this.originalValue == undefined ){
            this.originalValue = null;
        }
        this.modifiedValue = this.originalValue;
        if(this.modifiedValue && this.uiOption && this.uiOption.sensitive){
            //default sensitive values to *****
            this.modifiedValue ="*****";
        }
    }

    public isModified(){
        return this.modified;
    }

    public getValue(){
        if(!this.modified) {
            return this.originalValue;
        }
        else {
            return this.modifiedValue;
        }
    }

}

class DefaultUiOptionsMapper implements UiOptionsMapper {

    /**
     * Map of the property key to the option value storing the changes
     * @type {{}}
     */
    propertyMap:{ [k: string]: OptionValue } = {};

    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                ds.template.paths.push(control.value);
            } else if (key === "jars") {
                ds.template.jars = (control.value && control.value.length !== 0) ? control.value.split(",") : null;
            } else {
                this.setModelValue(ds,key,control);
            }
        });
    }

    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void {
        controls.forEach((control: FormControl, key: string) => {
            if (key === "path") {
                control.setValue(ds.template.paths[0]);
            } else if (key === "jars") {
                if(ds.template.jars != undefined && ds.template.jars.length >0){
                    control.setValue(ds.template.jars.join(","));
                }
            } else {
              this.setUiValueAndSubscribeToChanges(ds,control,key,uiOptions.get(key))
            }

        });
    }

    /**
     * Set the datasource property on the model
     * sensitive properties that are not changed will return the original encrypted cipher, otherwise the updated value
     * @param {DataSource} ds
     * @param {string} key
     * @param {FormControl} control
     */
    private setModelValue(ds: DataSource,key:string,control:FormControl){
        let optionValue = this.propertyMap[key];
        if(optionValue){
            ds.template.options[key] = optionValue.getValue();
        }
        else {
            ds.template.options[key] = control.value;
        }
    }

    /**
     * Set the FormControl UI value and subscribe to changes with the input.
     * @param {DataSource} ds
     * @param {FormControl} control
     * @param {string} key
     * @param {UiOption} uiOption
     */
    private setUiValueAndSubscribeToChanges(ds: DataSource,control: FormControl, key: string, uiOption?:UiOption) {
        let value = ds.template.options[key];
        let optionValue = new OptionValue(key,value, uiOption);
        this.propertyMap[key] = optionValue;
        control.setValue(optionValue.modifiedValue);
        control.valueChanges.subscribe((newValue:any)=> {
            let optValue = this.propertyMap[key];
            if(optValue){
                optValue.modifiedValue = newValue;
                optValue.modified = true;
            }

        });
    }
}

class AzureUiOptionsMapper implements UiOptionsMapper {
    mapFromUiToModel(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void {
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

    mapFromModelToUi(ds: DataSource, controls: Map<string, FormControl>, uiOptions: Map<string, UiOption>): void {
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

export interface ControllerServiceConflict {
    name:string;
    matchingServices:any;
    identityProperties:any;

}

/**
 * Displays selected connector properties.
 */
@Component({
    selector: "catalog-connector",
    styleUrls: ["./connector.component.scss"],
    templateUrl: "./connector.component.html"
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

    /**
     * Indicates if admin actions are allowed
     */
    allowAdmin = false;

    form = new FormGroup({});

    titleControl: FormControl;
    private controls: Map<string, FormControl> = new Map();
    isLoading: boolean = false;
    private controlToUIOption: Map<string, UiOption> = new Map();
    private testError: String;
    private testStatus: boolean = false;
    public controllerServiceConflictError:ControllerServiceConflict = null;
    private matchingControllerServiceCheck:boolean = true;
    private selectedMatchingControllerService:any;

    private matchingControllerServiceControl:FormControl;

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
                private entityAccessControlService: EntityAccessControlService,
                private accessControlService: AccessControlService) {
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

        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAdmin = this.accessControlService.hasAction(AccessControlService.DATASOURCE_ADMIN, actionSet.actions)
                    && this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.DATASOURCE.CHANGE_DATASOURCE_PERMISSIONS, this.datasource,
                        EntityAccessControlService.entityRoleTypes.DATASOURCE)
            });
    }

    initControls() {
        if (this.datasource) {
            this.titleControl.setValue(this.datasource.title);
            const optionsMapper = <UiOptionsMapper>this[this.plugin.optionsMapperId || "defaultOptionsMapper"];
            if (optionsMapper) {
                optionsMapper.mapFromModelToUi(this.datasource, this.controls, this.controlToUIOption);
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
                this.controlToUIOption.set(option.key,option);
            }
        }
        this.matchingControllerServiceControl = new FormControl("");
        this.form.addControl("matchingControllerService",this.matchingControllerServiceControl);
    }

    /**
     * Creates a new datasource or updates an existing one
     */
    saveDatasource() {
        this.testError = undefined;
        // attempt to detect any matching controller services only the first time.
        //once we detect if there are matching ones the next save will not detect
        let detectServices = this.controllerServiceConflictError == null;

        this.controllerServiceConflictError = null;

        const ds = this.getDataSourceFromUi() as CreateDataSource;
        if (ds === undefined) {
            return;
        }

        let matchingControllerServiceId = this.matchingControllerServiceControl.value.length >0 ? this.matchingControllerServiceControl.value[0] : null;
        if(matchingControllerServiceId != null){
            ds.nifiControllerServiceId = matchingControllerServiceId;
            detectServices = false;
        }
        else {
            ds.nifiControllerServiceId = null;
        }
        ds.detectSimilarNiFiControllerServices = detectServices;

        console.log("saving ",ds);
        this.isLoading = true;
        this.loadingService.register(ConnectorComponent.topOfPageLoader);
        this.catalogService.createDataSource(ds).pipe(
            concatMap(dataSource => {
                if (this.allowAdmin && typeof ds.roleMemberships !== "undefined") {
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
            (err:HttpErrorResponse) => {
                let propertyString:string;
                let errorMessage = err.error.message || err.message;
                if(err.status == 409 && err.error.type && err.error.type == "ControllerServiceConflictEntity") {
                    let controllerServiceConflict = (err.error as ControllerServiceConflict);
                    let identityPropertyKeys = Object.keys(controllerServiceConflict.identityProperties);
                    controllerServiceConflict.matchingServices.forEach((svc: any) => {
                        svc.identityPropertyString = ''
                        if (identityPropertyKeys) {
                            identityPropertyKeys.forEach((key: string) => {
                                let serviceValue = svc.properties[key];
                                if (serviceValue) {
                                    if (svc.identityPropertyString != "") {
                                        svc.identityPropertyString += ", ";
                                    }
                                    svc.identityPropertyString += key + "=" + serviceValue;
                                }
                            })
                        }
                    });
                    controllerServiceConflict.matchingServices = _.sortBy(controllerServiceConflict.matchingServices, (service: any) => service.state == 'ENABLED' ? 0 : 1)

                    this.controllerServiceConflictError = controllerServiceConflict;
                    this.matchingControllerServiceControl.setValue([""]);
                    let msg = this.translateService.instant("CATALOG.DATA_SOURCES.NIFI_CONTROLLER_SERVICE_CONFILCT_SNACK_BAR")
                    this.showSnackBar('Similar NiFi controller services were found. Review options before saving');
                }
                else {
                    console.error(err);
                    this.showSnackBar('Failed to save. ',errorMessage);
                }


            }
        );
    }

    /**
     * ensure only 1 service is selected
     * @param event
     */
    controllerServiceConflictChanged(event:MatSelectionListChange){
        console.log('controllerServiceConflictChanged ',event)

        if (event.option.selected) {
            event.source.options.toArray().forEach(element => {
                if (element != event.option) {
                    element.selected = false;
                }
            });
       }
    }

    getDataSourceFromUi(): DataSource | CreateDataSource | undefined {
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
        optionsMapper.mapFromUiToModel(ds, this.controls, this.controlToUIOption);
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
            err => {
                console.error(err);
                if (err.status == 409) {
                    this.showSnackBar("Failed to delete. This data source is currently being used by a feed.");
                } else {
                    this.showSnackBar('Failed to delete.', err.message);
                }
            }
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

            });
    }

    isTestAvailable(): boolean {
        const tabs = this.plugin.tabs;
        return tabs && (".browse" === tabs[0].sref || ".connection" === tabs[0].sref);
    }
}
