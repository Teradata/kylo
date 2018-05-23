import * as angular from "angular";

import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';
import {DatePipe} from '@angular/common';
import {StateService} from "@uirouter/angular";
import {TdBytesPipe} from '@covalent/core/common';
import {IPageChangeEvent} from '@covalent/core/paging';
import {SelectionService} from '../../api/services/selection.service';

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
        {name: "directory", label: "", sortable: false, width: 48, filter: false},
        {name: "name", label: "Name", sortable: true, filter: true},
        {name: "length", label: "Size", numeric: true, sortable: true, filter: false, width: 200, format: this.FILE_SIZE_FORMAT},
        {name: "modificationTime", label: "Last modified", sortable: true, filter: false, width: 210, format: this.DATE_FORMAT}
    ];

    sortBy = 'name';
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    searchTerm: string = '';
    filteredFiles: RemoteFile[] = [];
    filteredTotal = 0;
    fromRow: number = 1;
    currentPage: number = 1;
    pageSize: number = 50;
    selected: Map<string, boolean> = new Map<string, boolean>();
    selectAll: boolean = false;

    paths: string[];
    files: RemoteFile[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient,
                private state: StateService, private selectionService: SelectionService) {
        console.log('new RemoteFilesComponent');
    }

    public ngOnInit(): void {
        const datasetId = "3781fb2e-74a7-4d28-a3d6-580087b0f6d9";
        const template = angular.copy(this.datasource.template);
        template.paths[0] = this.path;
        this.paths = this.path.split("/");
        this.http.post("/proxy/v1/catalog/dataset/" + datasetId + "/files", template)
            .subscribe((data: RemoteFile[]) => {
                console.log('resetting selection');
                this.selected = new Map<string, boolean>();
                this.files = data;
                for (let file of this.files) {
                    this.selected.set(file.name, false);
                }
                console.log('getting existing selection for data source id ' + this.datasource.id + ' and path ' + this.path);

                //disable selection if parent directory is selected
                console.log('paths', this.paths);
                let parent = "";
                let hasParentSelection = false;
                let nextChildIdx: number;
                for (let i in this.paths) {
                    console.log('i = ' + i);
                    let currentPath = this.paths[i];
                    if (currentPath.length !== 0) {
                        parent += "/" + currentPath;
                        console.log('parent = ' + parent);
                        nextChildIdx = 1 + Number(i);
                        console.log('next child idx = ' + nextChildIdx + ', paths.length=' + this.paths.length);
                        if (this.paths.length > Number(nextChildIdx)) {
                            let child = this.paths[nextChildIdx];
                            console.log('child = ' + child);
                            this.selectionService.get(this.datasource.id, parent).forEach((isSelected: boolean, childPath: string) => {
                                hasParentSelection = hasParentSelection || (childPath === child && isSelected);
                            });
                        }
                    }
                }
                console.log("has parent selection? " + hasParentSelection);


                const existingSelection = this.selectionService.get(this.datasource.id, this.path);
                console.log(existingSelection);
                existingSelection.forEach((value: boolean, key: string) => {
                    this.selected.set(key, value);
                });
                this.filter();
            });
    }

    browseTo(pathIndex: number) {
        const location = this.paths.slice(0, pathIndex + 1).join("/");
        this.state.go("catalog.datasource.browse", {path: encodeURIComponent(location)}, {notify:false, reload:false});
    }

    onToggleAll(): void {
        //todo warn user that downstream selection will be removed, e.g.
        //todo 1. user selects file on path /a/b/c/file.txt
        //todo 2. user selects directory on path /a/b, which includes downstream /a/b/c/file.txt
        for (let file of this.files) {
            this.selected.set(file.name, this.selectAll);
        }
        this.storeSelection();
    }

    onToggleRow(event: any, file: RemoteFile): void {
        this.selected.set(file.name, event.checked);
        this.storeSelection();
    }

    private storeSelection() {
        this.selectionService.set(this.datasource.id, this.path, this.selected);
    }

    numberOfSelectedFiles() {
        return Array.from(this.selected.values()).filter(selected => selected).length;
    }

    totalNumberOfSelectedFiles() {
        let result = 0;
        let allPaths = this.selectionService.getAll(this.datasource.id);
        allPaths.forEach((pathSelection: Map<string, boolean>) => {
            result += Array.from(pathSelection.values()).filter(selected => selected).length;
        });
        return result;
    }

    rowClick(file: RemoteFile): void {
        if (file.directory) {
            this.state.go("catalog.datasource.browse", {path: encodeURIComponent(this.path + "/" + file.name)}, {notify:false, reload:false});
        }
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order === TdDataTableSortingOrder.Descending ? TdDataTableSortingOrder.Ascending : TdDataTableSortingOrder.Descending;
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
