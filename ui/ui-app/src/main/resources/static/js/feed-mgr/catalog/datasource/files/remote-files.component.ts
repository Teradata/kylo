import * as angular from "angular";

import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';
import {DatePipe} from '@angular/common';
import {StateService} from "@uirouter/angular";
import {TdBytesPipe} from '@covalent/core/common';
import {IPageChangeEvent} from '@covalent/core/paging';

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

    @Input()
    path: string;

    FILE_SIZE_FORMAT: (v: any) => any = (v: number) => new TdBytesPipe().transform(v, 2);
    DATE_FORMAT: (v: any) => any = (v: number) => new DatePipe('en-US').transform(v, 'dd/MM/yyyy hh:mm:ss');

    columns: ITdDataTableColumn[] = [
        {name: "directory", label: "", width: 48, filter: false},
        {name: "name", label: "Name", sortable: true, filter: true},
        {name: "length", label: "Size", numeric: true, sortable: true, filter: false, width: 200, format: this.FILE_SIZE_FORMAT},
        {name: "modificationTime", label: "Last modified", sortable: true, filter: false, width: 210, format: this.DATE_FORMAT}
    ];

    sortBy: string = 'name';
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;
    searchTerm: string = '';
    filteredFiles: RemoteFile[] = [];
    filteredTotal = 0;
    fromRow: number = 1;
    currentPage: number = 1;
    pageSize: number = 50;
    selectedRows: any[] = [];

    paths: string[];
    files: RemoteFile[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient, private state: StateService) {
    }

    public ngOnInit(): void {
        const datasetId = "3781fb2e-74a7-4d28-a3d6-580087b0f6d9";
        const template = angular.copy(this.datasource.template);
        template.paths[0] = this.path;
        this.paths = this.path.split("/");
        this.http.post("/proxy/v1/catalog/dataset/" + datasetId + "/files", template)
            .subscribe((data: RemoteFile[]) => {
                this.files = data;
                this.filter();
            });
    }

    browseTo(pathIndex: number) {
        const location = this.paths.slice(0, pathIndex + 1).join("/");
        this.state.go("catalog.datasource.browse", {path: encodeURIComponent(location)}, {notify:false, reload:false});
    }

    rowClick(file: RemoteFile): void {
        if (file.directory) {
            this.state.go("catalog.datasource.browse", {path: encodeURIComponent(this.path + "/" + file.name)}, {notify:false, reload:false});
        }
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }

    search(searchTerm: string): void {
        this.searchTerm = searchTerm;
        this.filter();
    }

    page(pagingEvent: IPageChangeEvent): void {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.filter();
    }

    private filter(): void {
        let newData: any[] = this.files;
        let excludedColumns: string[] = this.columns
            .filter((column: ITdDataTableColumn) => {
                return ((column.filter === undefined && column.hidden === true) ||
                    (column.filter !== undefined && column.filter === false));
            }).map((column: ITdDataTableColumn) => {
                return column.name;
            });
        newData = this.dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
        this.filteredTotal = newData.length;
        newData = this.dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        newData = this.dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredFiles = newData;
    }
}
