import {HttpClient} from "@angular/common/http";
import {Component, OnInit} from "@angular/core";
import {ITdDataTableColumn, TdDataTableService} from "@covalent/core/data-table";

interface RemoteFile {
    name: string;
    isDirectory: boolean;
    length: number;
    modification_time: Date;
}

// TODO https://teradata-corp.slack.com/archives/C4G9S3L4Q/p1524003641000293?thread_ts=1524003072.000184&cid=C4G9S3L4Q
@Component({
    selector: "remote-files",
    styleUrls: ["js/feed-mgr/catalog/dataset/files/remote-files.component.css"],
    templateUrl: "js/feed-mgr/catalog/dataset/files/remote-files.component.html"
})
export class RemoteFilesComponent implements OnInit {

    columns: ITdDataTableColumn[] = [
        {name: "isDirectory", label: "", width: 48},
        {name: "name", label: "Name", sortable: true},
        {name: "length", label: "Size", numeric: true, sortable: true, format: (v: number) => "4.0 KB"},
        {name: "modification_time", label: "Last modified", sortable: true}
    ];

    files: RemoteFile[] = [];

    filteredFiles: RemoteFile[] = [];

    filteredTotal = 0;

    path = "s3n://thinkbig.greg/";

    pageSize = 50;

    selectedRows: any[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient) {
    }

    public ngOnInit(): void {
        this.http.get("/proxy/v1/catalog/dataset/abc/browse?path=" + encodeURIComponent(this.path))
            .subscribe((data: any) => {
                this.files = data.results.rows.map((row: any) => {
                    return {name: row[0].substr(this.path.length), length: 0, modification_time: new Date(), isDirectory: row[1]};
                });
                this.filter();
            });
    }

    rowClick(event: any): void {
        if (event.row.isDirectory) {
            this.path += event.row.name + "/";
            this.ngOnInit();
        }
    }

    search(event: any): void {

    }

    private filter(): void {
        this.filteredFiles = this.files;
    }
}
