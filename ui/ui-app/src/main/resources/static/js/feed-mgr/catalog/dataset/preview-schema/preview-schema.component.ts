import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {DomSanitizer} from "@angular/platform-browser";
import {ITdDataTableColumn} from "@covalent/core/data-table";

@Component({
    selector: "preview-schema",
    templateUrl: "js/feed-mgr/catalog/dataset/preview-schema/preview-schema.component.html"
})
export class PreviewSchemaComponent implements OnInit {

    @Input()
    public connection: any;

    columns: ITdDataTableColumn[];

    data: any[];

    delimiter: string;

    format: string;

    header: boolean;

    raw: any;

    constructor(private http: HttpClient, private sanitizer: DomSanitizer) {
    }

    public ngOnInit(): void {
        this.format = "csv";
        if (this.format === "csv") {
            this.updateRaw();
        } else {
            this.update();
        }
    }

    update() {
        const request = {
            format: "csv",
            options: {
                header: this.header
            },
            paths: ["file:/opt/kylo/setup/data/sample-data/csv/userdata1.csv"]
        };
        this.http.post("/proxy/v1/catalog/dataset/{id}/preview", request)
            .subscribe((data: any) => {
                this.columns = data.results.columns.map((column: any) => {
                    return {name: column.field, label: column.displayName}
                });
                this.data = data.results.rows.map((row: any) => {
                    const data1 = {};
                    for (let i=0; i < data.results.columns.length; ++i) {
                        data1[data.results.columns[i].field] = row[i];
                    }
                    return data1;
                });
            });
    }

    updateRaw() {
        const request = {
            format: "text",
            paths: ["file:/opt/kylo/setup/data/sample-data/csv/userdata1.csv"]
        };
        this.http.post("/proxy/v1/catalog/dataset/{id}/preview", request)
            .subscribe((data: any) => {
                this.raw = this.sanitizer.bypassSecurityTrustHtml(data.results.rows
                    .map((row: any) => row[0])
                    .join("<br>"));
            });
    }
}
