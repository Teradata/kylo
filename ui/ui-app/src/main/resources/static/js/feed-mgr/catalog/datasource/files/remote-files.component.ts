import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, TdDataTableService} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';

interface RemoteFile {
    name: string;
    directory: boolean;
    length: number;
    modificationTime: Date;
    path: string;
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

    path: string;

    pageSize = 50;

    selectedRows: any[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient) {
    }

    public ngOnInit(): void {
        console.log('on init');
        this.path = this.datasource.paths[0];
        this.browse();
    }

    browse() {
        this.http.get("/proxy/v1/catalog/datasource/" + this.datasource.id + "/browse?path=" + encodeURIComponent(this.path))
            .subscribe((data: RemoteFile[]) => {
                this.files = data;
                this.filter();
            });
    }

    rowClick(file: RemoteFile): void {
        console.log("row click, row=" + file.name);
        if (file.directory) {
            this.path += "/" + file.name;
            this.browse();
        }
    }

    search(event: any): void {

    }

    private filter(): void {
        this.filteredFiles = this.files;
    }
}
