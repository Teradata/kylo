import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, TdDataTableService} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';
import {DatePipe} from '@angular/common';
import {FileSizePipe} from '../../api/pipes/file-size.pipe';

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

    FILE_SIZE_FORMAT: (v: any) => any = (v: number) => new FileSizePipe().transform(v);
    DATE_FORMAT: (v: any) => any = (v: number) => new DatePipe('en-US').transform(v, 'dd/MM/yyyy hh:mm:ss');

    columns: ITdDataTableColumn[] = [
        {name: "directory", label: "", width: 48},
        {name: "name", label: "Name", sortable: true},
        {name: "length", label: "Size", numeric: true, sortable: true, format: this.FILE_SIZE_FORMAT},
        {name: "modificationTime", label: "Last modified", sortable: true, format: this.DATE_FORMAT}
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
