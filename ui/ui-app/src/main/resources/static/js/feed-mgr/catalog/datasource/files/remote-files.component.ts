import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, TdDataTableService} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';

interface RemoteFile {
    name: string;
    isDirectory: boolean;
    length: number;
    modification_time: Date;
}

@Component({
    selector: "remote-files",
    styleUrls: ["js/feed-mgr/catalog/datasource/files/remote-files.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/files/remote-files.component.html"
})
export class RemoteFilesComponent implements OnInit {

    @Input()
    public datasource: DataSource;

    columns: ITdDataTableColumn[] = [
        {name: "isDirectory", label: "", width: 48},
        {name: "name", label: "Name", sortable: true},
        {name: "length", label: "Size", numeric: true, sortable: true, format: (v: number) => "4.0 KB"},
        {name: "modification_time", label: "Last modified", sortable: true}
    ];

    files: RemoteFile[] = [];

    filteredFiles: RemoteFile[] = [];

    filteredTotal = 0;

    private path: string;

    pageSize = 50;

    selectedRows: any[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient) {
    }

    public ngOnInit(): void {
        console.log('on init');
        this.path = this.datasource.paths[0];
        this.http.get("/proxy/v1/catalog/datasource/" + this.datasource.id + "/browse?path=" + encodeURIComponent(this.path))
            .subscribe((data: RemoteFile[]) => {
                this.files = data;
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
