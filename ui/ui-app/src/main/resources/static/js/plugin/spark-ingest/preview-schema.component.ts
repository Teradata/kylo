import {Component, ElementRef, Input, OnChanges, OnInit} from "@angular/core";
import {DomSanitizer} from "@angular/platform-browser";
import {ITdDataTableColumn} from "@covalent/core";
import {QueryEngine} from "../../feed-mgr/visual-query/wrangler";
import * as angular from "angular";
import "rxjs/add/operator/map";

@Component({
    templateUrl: "js/plugin/spark-ingest/preview-schema.component.html"
})
export class PreviewSchemaComponent implements OnChanges, OnInit {

    @Input()
    public model: any;

    @Input()
    public stepIndex: string;

    @Input()
    public stepper: any;

    columns: ITdDataTableColumn[];

    data: any[];

    delimiter: string;

    format: string;

    raw: any;

    wrangler: QueryEngine<any>;

    private $injector: angular.auto.IInjectorService;

    private FeedService: any;

    constructor(private element: ElementRef, private sanitizer: DomSanitizer) {
        this.$injector = angular.element(document.body).injector();
        this.FeedService = this.$injector.get("FeedService");

        const VisualQueryEngineFactory: any = this.$injector.get("VisualQueryEngineFactory");
        VisualQueryEngineFactory.getEngine("spark").then((wrangler: any) => this.wrangler = wrangler);
    }

    public ngOnInit(): void {
        //Listen for when the next step is active
        const BroadcastService: any = this.$injector.get("BroadcastService");
        const StepperService: any = this.$injector.get("StepperService");
        BroadcastService.subscribe(angular.element(this.element.nativeElement).scope(), StepperService.STEP_CHANGED_EVENT, () => {
            if (this.stepper.selectedStepIndex == this.stepIndex) {
                this.ngOnChanges();
            }
        });
    }

    public ngOnChanges(): void {
        this.format = this.model.tableOption.sourceFormat;
        if (this.format === "csv") {
            this.updateRaw();
        } else {
            this.update();
        }
    }

    next() {
        // Go to next step
        const step = this.stepper.getStep(this.stepIndex);
        this.stepper.stepEnabled(step.nextActiveStepIndex);
        this.stepper.completeStep(step.index);
        window.setTimeout(() => this.stepper.selectedStepIndex = step.nextActiveStepIndex, 0);
    }

    update() {
        let script = `import com.thinkbiganalytics.kylo.catalog.KyloCatalog\n` +
            `var df = KyloCatalog.read.format("${this.model.tableOption.sourceFormat}")`;
        if (this.model.tableOption.sourceJars) {
            script += this.model.tableOption.sourceJars.split(";").map((jar: any) => `.addJar("${jar}")`).join("");
        }
        if (this.model.tableOption.sourceOptions) {
            script += this.model.tableOption.sourceOptions.split(";").map((option: any) => {
                const [key, value] = option.split("=");
                return `.option("${key}", "${value}")`;
            }).join("");
        }
        if (this.format === "csv" && this.delimiter) {
            script += `.option("delimiter", "${this.delimiter}")`;
        }
        script += (this.model.tableOption.sourcePath) ? `.load("${this.model.tableOption.sourcePath}")` : `.load()`;
        script += "\n";

        script += `df = df.drop("processing_dttm")\n`;

        this.wrangler.setScript(script);
        this.wrangler.limit(3);
        this.wrangler.transform().subscribe(null, e => alert("ERROR: " + e), () => {
            const columns = this.wrangler.getColumns();

            // Update data table
            this.columns = columns.map(column => {
                return {name: column.field, label: column.displayName};
            });
            this.data = this.wrangler.getRows().map(row => {
                const data = {};
                for (let i=0; i < columns.length; ++i) {
                    data[columns[i].field] = row[i];
                }
                return data;
            });

            // Update feed model
            const fields = columns.map(column => {
                const field: any = angular.copy(column);
                field.name = field.field;
                field.derivedDataType = field.dataType;
                return field;
            });
            this.model.table.sourceTableSchema.fields = angular.copy(fields);
            this.FeedService.setTableFields(fields);
            this.model.table.method = "EXISTING_TABLE";
            this.model.table.feedFormat = "STORED AS ORC";
        });
    }

    updateRaw() {
        let script = `import com.thinkbiganalytics.kylo.catalog.KyloCatalog\n` +
            `var df = KyloCatalog.read.format("text")`;
        if (this.model.tableOption.sourceJars) {
            script += this.model.tableOption.sourceJars.split(";").map((jar: any) => `.addJar("${jar}")`).join("");
        }
        if (this.model.tableOption.sourceOptions) {
            script += this.model.tableOption.sourceOptions.split(";").map((option: any) => {
                const [key, value] = option.split("=");
                return `.option("${key}", "${value}")`;
            }).join("");
        }
        script += (this.model.tableOption.sourcePath) ? `.load("${this.model.tableOption.sourcePath}")` : `.load()`;
        script += "\n";

        this.wrangler.setScript(script);
        this.wrangler.limit(3);
        this.wrangler.transform().subscribe(null, e => alert("ERROR: " + e), () => {
            this.raw = this.sanitizer.bypassSecurityTrustHtml(this.wrangler.getRows()
                .map(row => row[0])
                .join("<br>"));
        });
    }
}
