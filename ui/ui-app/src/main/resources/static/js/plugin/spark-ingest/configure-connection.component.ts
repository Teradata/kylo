import {Component, ElementRef, Input, OnChanges, OnInit, ViewChild} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {ITdDynamicElementConfig, TdDynamicElement, TdDynamicFormsComponent} from "@covalent/dynamic-forms";
import * as angular from "angular";
import {BrowseDialog} from "./browse.component";

@Component({
    templateUrl: "js/plugin/spark-ingest/configure-connection.component.html"
})
export class ConfigureConnectionComponent implements OnInit, OnChanges {

    /**
     * Feed model
     */
    @Input()
    public model: any;

    @Input()
    public stepIndex: string;

    @Input()
    public stepper: any;

    elements: ITdDynamicElementConfig[] = [];

    @ViewChild(TdDynamicFormsComponent)
    form: TdDynamicFormsComponent;

    format: string;

    formType: string;

    hiveTables: any[];

    path: string;

    private HiveService: any;

    constructor(private element: ElementRef, private dialog: TdDialogService) {
    }

    get valid(): boolean {
        if (this.formType === "DYNAMIC") {
            return this.form && this.form.valid;
        } else if (this.formType === "HIVE") {
            return this.path && this.path.length > 0;
        }
        return true;
    }

    public ngOnInit(): void {
        const $injector = angular.element(document.body).injector();
        this.HiveService = $injector.get("HiveService");

        //Listen for when the next step is active
        const BroadcastService: any = $injector.get("BroadcastService");
        const StepperService: any = $injector.get("StepperService");
        BroadcastService.subscribe(angular.element(this.element.nativeElement).scope(), StepperService.STEP_CHANGED_EVENT, () => {
            if (this.stepper.selectedStepIndex == this.stepIndex) {
                this.ngOnChanges();
            }
        });
    }

    public ngOnChanges(): void {
        if (this.model.tableOption.sparkConnector) {
            switch (this.model.tableOption.sparkConnector.title) {
                case "SQL Source":
                    this.formType = "DYNAMIC";
                    this.elements = [{
                        label: "JDBC URL",
                        name: "url",
                        required: true,
                        type: TdDynamicElement.Input
                    }, {
                        label: "JDBC Driver Class",
                        name: "driver",
                        type: TdDynamicElement.Input
                    }, {
                        label: "JDBC Driver Jar Location(s)",
                        name: "kyloSparkJars",
                        type: TdDynamicElement.Input
                    }, {
                        label: "Table Name",
                        name: "dbtable",
                        required: true,
                        type: TdDynamicElement.Input
                    }, {
                        label: "Username",
                        name: "user",
                        required: true,
                        type: TdDynamicElement.Input
                    }, {
                        label: "Password",
                        name: "password",
                        required: true,
                        type: TdDynamicElement.Password
                    }];
                    break;

                case "HDFS":
                case "Amazon S3":
                case "Google Cloud Storage":
                    this.formType = "HDFS";
                    break;

                case "Hive":
                    this.formType = "HIVE";
                    break;

                case "Teradata":
                    this.formType = "DYNAMIC";
                    this.elements = [{
                        label: "Table Name",
                        name: "dbtable",
                        required: true,
                        type: TdDynamicElement.Input
                    }];
                    break;

                case "Kafka":
                    this.formType = "DYNAMIC";
                    this.elements = [{
                        label: "Topic Name",
                        name: "subscribe",
                        required: true,
                        type: TdDynamicElement.Input
                    }];
                    break;

                case "File Upload":
                    this.formType = "";
                    this.elements = [];
                    break;
            }
        }
    }

    browse(): void {
        this.dialog.open(BrowseDialog)
            .afterClosed()
            .subscribe(path => {
                if (path) {
                    this.path = path;
                    if (path.indexOf("csv") > -1) {
                        this.format = "csv";
                    } else if (path.indexOf("orc") > -1) {
                        this.format = "orc";
                    } else {
                        this.format = "";
                    }
                }
            });
    }

    refreshHive(): void {
        const tables = this.HiveService.queryTablesSearch(this.path);
        if (tables.then) {
            tables.then((tables: any) => this.hiveTables = tables);
        } else {
            this.hiveTables = tables;
        }
    }

    save(): void {
        if (this.formType === "DYNAMIC") {
            this.saveDynamic();
        } else if (this.formType === "HDFS") {
            this.saveHdfs();
        } else if (this.formType === "HIVE") {
            this.saveHive();
        }

        // Go to next step
        const step = this.stepper.getStep(this.stepIndex);
        this.stepper.stepEnabled(step.nextActiveStepIndex);
        this.stepper.completeStep(step.index);
        window.setTimeout(() => this.stepper.selectedStepIndex = step.nextActiveStepIndex, 0);
    }

    private saveDynamic(): void {
        // Set table options
        this.model.tableOption.connectionOptions = this.form.value;
        this.model.tableOption.sourceOptions = Object.keys(this.form.value)
            .filter(key => key !== "kyloSparkFormat" && key !== "kyloSparkJars" && key !== "path")
            .map(key => key + "=" + this.form.value[key])
            .join(";");
        if (this.model.tableOption.sparkConnector["sparkOptions"]) {
            if (this.model.tableOption.sourceOptions.length > 0) {
                this.model.tableOption.sourceOptions += ";";
            }
            this.model.tableOption.sourceOptions += this.model.tableOption.sparkConnector["sparkOptions"];
        }

        if (this.form.value["kyloSparkFormat"]) {
            this.model.tableOption.sourceFormat = this.form.value["kyloSparkFormat"];
        } else if (this.model.tableOption.sparkConnector["sparkFormat"]) {
            this.model.tableOption.sourceFormat = this.model.tableOption.sparkConnector["sparkFormat"];
        }

        let jars = "";
        if (this.form.value["kyloSparkJars"]) {
            jars += this.form.value["kyloSparkJars"];
        }
        if (this.model.tableOption.sparkConnector["sparkJars"]) {
            if (jars.length > 0) {
                jars += ";";
            }
            jars += this.model.tableOption.sparkConnector["sparkJars"];
        }
        if (jars.length > 0) {
            this.model.tableOption.sourceJars = jars;
        }

        if (this.form.value["path"]) {
            this.model.tableOption.sourcePath = this.form.value["path"];
        }

        // Set general info
        switch (this.model.tableOption.sparkConnector.title) {
            case "SQL Source":
                this.model.feedName = "sql table " + this.form.value["dbtable"];
                this.model.table.existingTableName = this.form.value["dbtable"];
                this.model.table.sourceTableSchema.name = this.model.table.existingTableName;
                break;

            case "HDFS":
                this.model.feedName = this.form.value["path"].replace(/.*\/|\..*/g, "") + " file";
                break;

            case "Teradata":
                this.model.feedName = "teradata table " + this.form.value["dbtable"];
                this.model.table.existingTableName = this.form.value["dbtable"];
                this.model.table.sourceTableSchema.name = this.model.table.existingTableName;
                break;
        }
    }

    private saveHdfs(): void {
        if (this.model.tableOption.sparkConnector["sparkJars"]) {
            this.model.tableOption.sourceJars = this.model.tableOption.sparkConnector["sparkJars"];
        }

        if (this.model.tableOption.sparkConnector["sparkOptions"]) {
            this.model.tableOption.sourceOptions = this.model.tableOption.sparkConnector["sparkOptions"];
        }

        let path = this.path;
        if (this.model.tableOption.sparkConnector.title) {
            path = "gs://archive.freastro.net" + path;
        }

        this.model.tableOption.sourceFormat = this.format;
        this.model.tableOption.sourcePath = path;

        this.model.feedName = this.path.replace(/.*\//, "");
    }

    private saveHive(): void {
        this.model.tableOption.sourceFormat = "hive";
        this.model.tableOption.sourcePath = this.path;

        this.model.feedName = "hive table " + this.path;
        this.model.table.existingTableName = this.path;
        this.model.table.sourceTableSchema.name = this.model.table.existingTableName;
    }
}
