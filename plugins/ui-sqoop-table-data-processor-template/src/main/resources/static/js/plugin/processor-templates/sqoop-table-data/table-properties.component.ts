import {HttpClient} from "@angular/common/http";
import {Component, Inject, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from "@angular/core";
import {FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {MatAutocompleteSelectedEvent, MatAutocompleteTrigger} from "@angular/material/autocomplete";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ProcessorRef, Property} from "@kylo/feed";
import {Observable} from "rxjs/Observable";
import {of} from "rxjs/observable/of";
import {catchError} from "rxjs/operators/catchError";
import {debounceTime} from "rxjs/operators/debounceTime";
import {distinctUntilChanged} from "rxjs/operators/distinctUntilChanged";
import {filter} from "rxjs/operators/filter";
import {map} from "rxjs/operators/map";
import {skip} from "rxjs/operators/skip";
import {switchMap} from "rxjs/operators/switchMap";

import {DescribeTableDialogComponent} from "./describe-table-dialog.component";

export class TableRef {

    constructor(readonly fullName: string) {
    }

    get fullNameLower(): string {
        return this.fullName.toLowerCase();
    }

    get schema(): string {
        return this.fullName.substr(0, this.fullName.indexOf("."));

    }

    get tableName(): string {
        return this.fullName.substr(this.fullName.indexOf(".") + 1);
    }
}

@Component({
    selector: "thinkbig-get-table-data-properties",
    styleUrls: ["./table-properties.component.css"],
    templateUrl: "./table-properties.component.html"
})
export class TablePropertiesComponent implements OnChanges, OnInit {

    @Input()
    connectionServiceKey: any;

    @Input()
    incrementalPropertyKey: any;

    @Input()
    loadStrategyKey: any;

    /**
     * Load strategies either passed in via the directive, or defaulted
     */
    @Input()
    loadStrategyOptions: any;

    @Input()
    processor: ProcessorRef;

    @Input()
    renderLoadStrategyOptions: any;

    @Input()
    useTableNameOnly = false;

    readonly ARCHIVE_UNIT_PROPERTY_KEY = 'Minimum Time Unit';

    readonly RETENTION_PERIOD_PROPERTY_KEY = 'Backoff Period';

    readonly SOURCE_TABLE_PROPERTY_KEY = 'Source Table';

    readonly SOURCE_FIELDS_PROPERTY_KEY = 'Source Fields';

    /**
     * Cache of the Table objects returned from the initial autocomplete call out
     */
    allTables = {};

    archiveUnitProperty: any;

    /**
     * Flag to indicate if the controller service has connection errors.
     * if there are errors the UI will display input boxes for the user to defin the correct table name
     */
    databaseConnectionError = false;

    databaseConnectionErrorObject: any;

    dbConnectionProperty: any;

    deleteSourceProperty: any;

    defaultLoadStrategyValue = 'INCREMENTAL';

    describingTableSchema = false;

    fieldsProperty: any;

    filteredFieldDates: any = [];

    form = new FormGroup({});

    incrementalFieldProperty: any;

    /**
     * boolean flag to indicate we are initializing the controller
     */
    initializing = true;

    loadStrategyProperty: any;

    model: any;

    nonCustomProperties: any[];

    /**
     * property that stores the selected table fields and maps it to the hive table schema
     */
    originalTableFields: any = [];

    outputTypeProperty: any;

    restrictIncrementalToDateOnly: boolean;

    retentionPeriodProperty: any;

    /**
     * The object storing the table selected via the autocomplete
     */
    selectedTable: any = null;

    @ViewChild("tableAutoInput", {read: MatAutocompleteTrigger})
    tableAutoTrigger: MatAutocompleteTrigger;

    tableControl = new FormControl(null, [
        Validators.required,
        () => this.validate()
    ]);

    tableItems: Observable<any[]>;

    /**
     * Cache of the fields related to the table selected
     * This is used the the dropdown when showing the fields for the incremtal options
     */
    tableFields: any = [];

    tableFieldsDirty: boolean;

    tableProperty: any;

    /**
     * The table Schema, parsed from the table autocomplete
     */
    tableSchema: any = null;

    catalogNiFiControllerServiceIds:string[] = null;

    initialized:boolean = false;

    constructor(private http: HttpClient, private dialog: MatDialog, @Inject("FeedService") private feedService: any, @Inject("DBCPTableSchemaService") private tableSchemaService: any) {
        // Handle connection service changes
        this.form.valueChanges.pipe(
            filter(() => this.processor != null && this.processor.form.enabled),
            map(value => (this.dbConnectionProperty != null) ? value[this.dbConnectionProperty.nameKey] : null),
            filter(value => typeof value !== "undefined" && value != null),
            distinctUntilChanged(),
            skip(1)
        ).subscribe(() => this.onDbConnectionPropertyChanged());

        // Handle output type changes
        this.form.valueChanges.pipe(
            filter(() => this.processor != null && this.processor.form.enabled),
            map(value => (this.outputTypeProperty != null) ? value[this.outputTypeProperty.nameKey] : null),
            filter(value => typeof value !== "undefined" && value != null),
            distinctUntilChanged()
        ).subscribe(value => {
            if (value == 'AVRO') {
                this.model.table.feedFormat = 'STORED AS AVRO'
            } else if (value == 'DELIMITED') {
                this.model.table.feedFormat = this.feedService.getNewCreateFeedModel().table.feedFormat
            }
        });

        // Add table control
        this.form.addControl("table", this.tableControl);
        this.tableItems = this.tableControl.valueChanges.pipe(
            filter(value => value != null && value.length >= 2),
            debounceTime(300),
            switchMap(value => {
                this.selectedTable = value;
                return this.queryTablesSearch(value)
            })
        );

        this.fetchCatalogJbdcSources();
    }

    updateDbConnectionPropertyOptions(){
        if(this.dbConnectionProperty && this.catalogNiFiControllerServiceIds != null){
            (this.dbConnectionProperty.propertyDescriptor.allowableValues as any[])
                .filter((allowableValue: any) => allowableValue.displayName.indexOf("**") == -1 && this.catalogNiFiControllerServiceIds.indexOf(allowableValue.value) >=0)
                .forEach((allowableValue:any) =>  {
                            allowableValue.catalogSource = true;
                            allowableValue.origName = allowableValue.displayName;
                            allowableValue.displayName +="**";
                });
              }

    }

    ngOnInit() {
        if (typeof this.loadStrategyOptions === "undefined") {
            this.loadStrategyOptions = [
                {name: 'Full Load', type: 'SNAPSHOT', strategy: 'FULL_LOAD', hint: 'Replace entire table'},
                {name: 'Incremental', type: 'DELTA', strategy: 'INCREMENTAL', hint: 'Incremental load based on a high watermark', incremental: true, restrictDates: true}
            ];
        }

        /**
         * Default the property keys that are used to look up the
         */
        if (typeof this.connectionServiceKey === "undefined") {
            this.connectionServiceKey = 'Source Database Connection';
        }
        if (typeof this.loadStrategyKey === "undefined") {
            this.loadStrategyKey = 'Load Strategy';
        }
        if (typeof this.incrementalPropertyKey === "undefined") {
            this.incrementalPropertyKey = 'Date Field';
        }
        this.dbConnectionProperty = this.findProperty(this.connectionServiceKey, false);

        //update the hint only when editing
        const hintSuffix = "   ** Indicates an existing catalog data source";
        if(this.dbConnectionProperty) {
            let desc = this.dbConnectionProperty.propertyDescriptor.origDescription || this.dbConnectionProperty.propertyDescriptor.description;
            if (!this.processor.form.disabled) {
                if(desc != undefined && desc.indexOf(hintSuffix) == -1){
                    this.dbConnectionProperty.propertyDescriptor.origDescription = desc;
                    this.dbConnectionProperty.propertyDescriptor.description = desc+hintSuffix;
                }
                else if (desc == undefined || desc == null){
                    this.dbConnectionProperty.propertyDescriptor.description = hintSuffix;
                }
            } else if(desc != undefined){
                this.dbConnectionProperty.propertyDescriptor.description = desc;
            }
        }
        /**
         * Autocomplete objected used in the UI html page
         */
        if (this.dbConnectionProperty.value == null) {
            this.tableControl.disable();
        }

        /**
         * lookup and find the respective Nifi Property objects that map to the custom property keys
         */
        this.initPropertyLookup();

        this.nonCustomProperties = this.processor.processor.properties.filter((property: any) => !this.isCustomProperty(property));

        /**
         * if we are editing or working with the cloned feed then get the selectedTable saved on this model.
         */
        this.initializeAutoComplete();
        if (this.processor.form.enabled) {
            if (this.model.cloned == true) {
                //if we are cloning and creating a new feed setup the autocomplete
                this.setupClonedFeedTableFields();
            } else {
                this.editIncrementalLoadDescribeTable()
            }
        }
        this.updateDbConnectionPropertyOptions();

        this.initializing = false;
    }

    fetchCatalogJbdcSources(){
        let url = "/proxy/v1/catalog/datasource/plugin-id?pluginIds=jdbc";
        this.http.get(url).subscribe((responses:any[]) => {
            this.catalogNiFiControllerServiceIds = responses.map((source:any) => source.nifiControllerServiceId);
            this.updateDbConnectionPropertyOptions();
            this.initialized = true;
        },(error1:any) => this.initialized = true);

    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.processor) {
            if (this.processor != null) {
                this.model = this.processor.feed;
                this.processor.control = this.form;
            } else {
                this.model = null;
            }
        }
    }

    /**
     * If there is a LOAD_STRATEGY property then watch for changes to show/hide additional options
     */
    onLoadStrategyChange() {
        let newVal = this.loadStrategyProperty.value;
        this.loadStrategyProperty.displayValue = newVal;
        if (newVal == 'FULL_LOAD') {
            this.model.table.tableType = 'SNAPSHOT';
            this.restrictIncrementalToDateOnly = false;
        } else if (this.isIncrementalLoadStrategy(newVal)) {
            this.model.table.tableType = 'DELTA';
            //reset the date field
            this.model.table.sourceTableIncrementalDateField = '';
            let option = this.loadStrategyOptions.find((opt: any) => opt.strategy == newVal);
            if (option) {
                this.restrictIncrementalToDateOnly = option.restrictDates != undefined ? option.restrictDates : false;
            }
            this.editIncrementalLoadDescribeTable();
        }
    }

    // TODO setupClonedFeedTableFields()

    getTableName(table?: TableRef): string {
        return table ? table.fullName : null;
    }

    /**
     * lookup and find the respective Nifi Property objects that map to the custom property keys
     */
    private initPropertyLookup() {
        this.tableProperty = this.findProperty(this.SOURCE_TABLE_PROPERTY_KEY);
        this.fieldsProperty = this.findProperty(this.SOURCE_FIELDS_PROPERTY_KEY);
        this.loadStrategyProperty = this.findProperty(this.loadStrategyKey);
        if (this.loadStrategyProperty && (this.loadStrategyProperty.value == null || this.loadStrategyProperty.value == undefined)) {
            this.loadStrategyProperty.value = this.defaultLoadStrategyValue;
        }


        this.retentionPeriodProperty = this.findProperty(this.RETENTION_PERIOD_PROPERTY_KEY);
        this.archiveUnitProperty = this.findProperty(this.ARCHIVE_UNIT_PROPERTY_KEY);

        this.deleteSourceProperty = {value: 'false', key: 'Delete Source'};
        this.incrementalFieldProperty = this.findProperty(this.incrementalPropertyKey);

        this.outputTypeProperty = this.findProperty('Output Type');
    }

    /**
     * Check to see if the property is in the list of custom ones.
     * if so it will bypass the nifi-property directive for rendering
     */
    isCustomProperty(property: any) {
        let customPropertyKeys = [this.connectionServiceKey, this.SOURCE_TABLE_PROPERTY_KEY, this.SOURCE_FIELDS_PROPERTY_KEY, this.loadStrategyKey, this.incrementalPropertyKey, this.RETENTION_PERIOD_PROPERTY_KEY, this.ARCHIVE_UNIT_PROPERTY_KEY];
        return customPropertyKeys.findIndex(value => value == property.key) != -1;
    }

    /**
     * Determine if the incoming value or if the current selected LoadStrategy is of type incremental
     * Incremental properties need additional option to define the field used for incrementing
     */
    isIncrementalLoadStrategy(val?: any) {
        let checkValue = val;
        if (typeof checkValue === "undefined" || val === null) {
            checkValue = (this.loadStrategyProperty) ? this.loadStrategyProperty.value : undefined;
        }

        return checkValue && this.loadStrategyOptions.find((v: any) => v.strategy == checkValue && v.incremental == true);
    }

    /**
     * Watch for changes on the table to refresh the schema
     */
    onTableSelected(event: MatAutocompleteSelectedEvent) {
        this.selectedTable = event.option.value;
        if (this.tableProperty && this.tableControl.value != null) {
            let needsDescribe = (!this.model.cloned || this.model.table.feedDefinitionTableSchema.fields.length == 0);

            if (this.useTableNameOnly) {
                needsDescribe = needsDescribe || (this.model.cloned && this.tableProperty.value != this.tableControl.value.tableName);
                this.tableProperty.value = this.tableControl.value.tableName;
            } else {
                needsDescribe = needsDescribe || (this.model.cloned && this.tableProperty.value != this.tableControl.value.fullName);
                this.tableProperty.value = this.tableControl.value.fullName;
            }


            if (this.processor.form.enabled) {
                //only describe on the Create as the Edit will be disabled and we dont want to change the field data.
                //If we are working with a cloned feed we should attempt to get the field information from the cloned model
                if (needsDescribe) {
                    this.describeTable();
                }
            }
        }
    }

    private setupClonedFeedTableFields() {
        this.databaseConnectionError = false;
        this.tableSchema = this.model.table.feedDefinitionTableSchema;
        this.tableFields = this.tableSchema.fields;
        this.originalTableFields = this.model.table.sourceTableSchema;
        this.tableFieldsDirty = false;

        //   FeedService.setTableFields(this.tableSchema.fields);
        this.model.table.method = 'EXISTING_TABLE';
    }

    /**
     * Change listener when the user changes the controller service in the UI
     */
    private onDbConnectionPropertyChanged(): void {
        this.selectedTable = undefined;
        this.tableControl.setValue(null);
        this.model.table.sourceTableIncrementalDateField = null;
        this.databaseConnectionError = false;
    }

    /**
     * Finds the correct NiFi processor Property associated with the incoming key.
     */
    private findProperty(key: string, clone: boolean = false): Property {
        //get all the props for this input
        let matchingProperty = this.processor.processor.allProperties.find((property: any) => property.key == key);

        //on edit mode the model only has the props saved for that type.
        //need to find the prop associated against the other input type
        if ((matchingProperty == undefined || matchingProperty == null) && this.model.allInputProcessorProperties != undefined) {
            var props = this.model.allInputProcessorProperties[(this.processor.processor as any).processorId];
            if (props) {
                matchingProperty = props.find((property: any) => property.key == key);
            }
        }
        if (matchingProperty == null) {
            //  console.log('UNABLE TO GET MATCHING PROPERTY FOR ',key,'model ',this.model, this.processor)
        } else {
            if (clone) {
                return {...matchingProperty};
            }
        }

        return matchingProperty;
    }

    /**
     * match query term case insensitive
     */
    private createFilterForTable(query: string) {
        var lowercaseQuery = query.toLowerCase();
        return function filterFn(item: any) {
            return (item.fullNameLower.indexOf(lowercaseQuery) != -1);
        };
    }

    /**
     * return the list of tables for the selected Service ID
     */
    private queryTablesSearch(query: string): Observable<any[]> {
        if (this.dbConnectionProperty == null || this.dbConnectionProperty.value == null || this.dbConnectionProperty.value == "") {
            return of([]);
        } else if (this.dbConnectionProperty in this.allTables) {
            return query ? this.allTables[this.dbConnectionProperty].filter(this.createFilterForTable(query)) : [];
        } else {
            let httpOptions: any = {params: {tableName: (query != null) ? "%" + query + "%" : "%"}};
            if (Array.isArray(this.dbConnectionProperty.propertyDescriptor.allowableValues)) {
                const service = (this.dbConnectionProperty.propertyDescriptor.allowableValues as any[])
                    .find((allowableValue: any) => allowableValue.value == this.dbConnectionProperty);
                if (typeof service !== "undefined") {
                    let name = service.displayName;
                    if(service.catalogSource && service.origName){
                        name = service.origName;
                    }
                    httpOptions.params.serviceName = name
                }
            }


            return this.http.get(this.tableSchemaService.LIST_TABLES_URL(this.dbConnectionProperty.value), httpOptions).pipe(
                catchError((error: any) => {
                    return Observable.throw(error);
                }),
                map((data: any) => {
                    if (Array.isArray(data)) {
                        return data;
                    } else {
                        throw (data != null) ? data : new Error("service not available");
                    }
                }),
                map((data: any) => {
                    this.databaseConnectionError = false;
                    const tables = this.parseTableResponse(data);
                    // Dont cache .. uncomment to cache results
                    // this.allTables[serviceId] = parseTableResponse(response.data);
                    return query ? tables.filter(this.createFilterForTable(query)) : tables;
                }),
                catchError<any, any[]>((error: any) => {
                    this.setDatabaseConnectionError(error)
                    return Observable.throw(error);
                })
            );
        }
    }

    private setDatabaseConnectionError(error:any){
        if(error && error.error && error.error.message){
            this.databaseConnectionErrorObject = error.error;
        }
        else if(error && error.message ){
            this.databaseConnectionErrorObject = error;
        }
        else {
            this.databaseConnectionErrorObject = {message:"Unable to connect to data source ",developerMessage:null};
        }
        this.databaseConnectionErrorObject.message +=". You can manually enter the table and fields below";

        if(this.tableProperty){
            this.tableProperty.value = "";
        }
        if(this.fieldsProperty) {
            this.fieldsProperty.value = "";
        }
        this.databaseConnectionError = true;
    }

    /**
     * Turn the schema.table string into an object for template display
     */
    private parseTableResponse(response: any[]) {
        if (response) {
            return response.map(table => new TableRef(table));
        } else {
            return [];
        }
    }

    private showDescribeTableSchemaDialog(tableDefinition: any): MatDialogRef<DescribeTableDialogComponent> {
        const ref = this.dialog.open(DescribeTableDialogComponent, {data: tableDefinition});
        ref.afterClosed().subscribe(() => this.tableAutoTrigger.closePanel());
        return ref;
    }

    private hideDescribeTableSchemaDialog(ref: MatDialogRef<DescribeTableDialogComponent>): void {
        ref.close();
    }

    private updateRestrictIncrementalToDateOnly(){
        let newVal = this.loadStrategyProperty.value;
        if (newVal == 'FULL_LOAD') {
            this.restrictIncrementalToDateOnly = false;
        } else if (this.isIncrementalLoadStrategy(newVal)) {
            let option = this.loadStrategyOptions.find((opt: any) => opt.strategy == newVal);
            if (option) {
                this.restrictIncrementalToDateOnly = option.restrictDates != undefined ? option.restrictDates : false;
            }
        }
    }

    /**
     * on edit describe the table for incremental load to populate the tableField options
     */
    private editIncrementalLoadDescribeTable() {
        //get the property that stores the DBCPController Service
        let dbcpProperty = this.dbConnectionProperty;
        if (dbcpProperty != null && dbcpProperty.value != null && this.selectedTable != null) {
            this.updateRestrictIncrementalToDateOnly();
            let serviceId = dbcpProperty.value;

            let serviceName = '';
            if (Array.isArray(dbcpProperty.propertyDescriptor.allowableValues)) {
             let service =   dbcpProperty.propertyDescriptor.allowableValues.find((allowableValue: any) => allowableValue.value == serviceId);
             if(service != undefined) {
                 serviceName = service.displayName;
                 if (service.catalogSource && service.origName) {
                     serviceName = service.origName;
                 }
             }
            }
            this.http.get(this.tableSchemaService.DESCRIBE_TABLE_URL(serviceId, this.selectedTable.tableName), {params: {schema: this.selectedTable.schema, serviceName: serviceName}})
                .subscribe((response) => {
                    this.databaseConnectionError = false;
                    this.tableSchema = response;
                    this.tableFields = this.tableSchema.fields;
                    this.filteredFieldDates = this.tableFields.filter(this.filterFieldDates);
                }, (error:any) => {
                    this.setDatabaseConnectionError(error);
                    console.error("error",error)
                });
        }
    }

    /**
     * Describe the table
     * This is called once a user selects a table from the autocomplete
     * This will setup the model populating the destination table fields
     */
    private describeTable() {
        //get the property that stores the DBCPController Service
        let dbcpProperty = this.dbConnectionProperty;
        if (dbcpProperty != null && dbcpProperty.value != null && this.selectedTable != null) {
            const dialogRef = this.showDescribeTableSchemaDialog(this.selectedTable);
            this.describingTableSchema = true;

            let serviceId = dbcpProperty.value;
            let service = null;
            let serviceName = '';
            if (dbcpProperty.propertyDescriptor.allowableValues) {
                service =   dbcpProperty.propertyDescriptor.allowableValues.find((allowableValue: any) => allowableValue.value == serviceId);
            }
            if(service != undefined && service != null) {
                serviceName = service.displayName;
                if (service.catalogSource && service.origName) {
                    serviceName = service.origName;
                }
            }
            this.http.get(this.tableSchemaService.DESCRIBE_TABLE_URL(serviceId, this.selectedTable.tableName), {params: {schema: this.selectedTable.schema, serviceName: serviceName}})
                .subscribe((response) => {
                    this.databaseConnectionError = false;

                    this.tableSchema = response;
                    this.tableFields = this.tableSchema.fields;
                    this.originalTableFields = [...this.tableSchema.fields];
                    this.filteredFieldDates = this.tableFields.filter(this.filterFieldDates);
                    this.tableFieldsDirty = false;



                    this.model.table.sourceTableSchema.fields = this.originalTableFields;

                    this.model.table.setTableFields(this.tableSchema.fields)

                    //this.feedService.setTableFields(this.tableSchema.fields);
                    if(this.model.setTableMethod) {
                        this.model.setTableMethod('EXISTING_TABLE');
                    }
                    else {
                        this.model.table.method = 'EXISTING_TABLE';
                    }

                    if (this.tableSchema.schemaName != null) {
                        this.model.table.existingTableName = this.tableSchema.schemaName + "." + this.tableSchema.name;
                    }
                    else {
                        this.model.table.existingTableName = this.tableSchema.name;
                    }
                    this.model.table.sourceTableSchema.name = this.model.table.existingTableName;
                    this.describingTableSchema = false;
                    this.hideDescribeTableSchemaDialog(dialogRef);
                }, (error:any) => {
                    this.setDatabaseConnectionError(error)
                    this.describingTableSchema = false;
                    this.hideDescribeTableSchemaDialog(dialogRef);
                });

        }
    }


    onManualTableNameChange() {
        if (this.model.table.method != 'EXISTING_TABLE') {
            this.model.table.method = 'EXISTING_TABLE';
        }
        this.model.table.sourceTableSchema.name = this.tableProperty.value;
        this.model.table.existingTableName = this.tableProperty.value;
    }

    onManualFieldNameChange() {
        if (this.model.table.method != 'EXISTING_TABLE') {
            this.model.table.method = 'EXISTING_TABLE';
        }
        let fields: any[] = [];
        let val: string = this.fieldsProperty.value;
        let fieldNames: any[] = [];
        val.split(",").forEach(field => {
            let col = this.feedService.newTableFieldDefinition();
            col.name = field.trim();
            col.derivedDataType = 'string';
            fields.push(col);
            fieldNames.push(col.name);
        });
        this.model.table.sourceTableSchema.fields = [...fields];
        this.model.table.setTableFields(fields)

        this.model.table.sourceFieldsCommaString = fieldNames.join(",");
        this.model.table.sourceFields = fieldNames.join("\n")

    }

    /**
     * Filter for fields that are Date types
     */
    private filterFieldDates(field: any): boolean {
        return field.derivedDataType == 'date' || field.derivedDataType == 'timestamp';
    }

    onIncrementalDateFieldChange() {
        let prop = this.incrementalFieldProperty;
        if (prop != null) {
            prop.value = this.model.table.sourceTableIncrementalDateField;
            prop.displayValue = prop.value;
        }
    }

    /**
     * Validates the autocomplete has a selected table
     */
    private validate(): ValidationErrors {
        if (this.selectedTable == undefined) {
            return {required: true};
        } else {
            if (this.describingTableSchema) {
                return {};
            } else {
                return {};
            }
        }
    }

    private initializeAutoComplete() {
        if (this.dbConnectionProperty != null && this.dbConnectionProperty.value != null && this.dbConnectionProperty.value != "") {
            const processorTableName = this.model.table.existingTableName;
            this.tableControl.setValue(processorTableName);
            if (processorTableName != null) {
                this.selectedTable = new TableRef(processorTableName);
                this.tableControl.setValue(this.selectedTable);
            }
        }
    }
}
