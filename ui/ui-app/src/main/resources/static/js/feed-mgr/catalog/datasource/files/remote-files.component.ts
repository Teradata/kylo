import * as angular from "angular";

import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';
import {StateService} from "@uirouter/angular";
import {IPageChangeEvent} from '@covalent/core/paging';
import {SelectionService} from '../../api/services/selection.service';
import {MatDialog} from '@angular/material/dialog';
import {SelectionDialogComponent} from './dialog/selection-dialog.component';
import {RemoteFile, RemoteFileDescriptor} from './remote-file';

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

    columns: ITdDataTableColumn[] = RemoteFileDescriptor.COLUMNS;
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
    isParentSelected: boolean = false;
    selectedChildCount: Map<string, number> = new Map<string, number>();

    paths: string[];
    files: RemoteFile[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient,
                private state: StateService, private selectionService: SelectionService,
                private dialog: MatDialog) {
    }

    public ngOnInit(): void {
        const template = angular.copy(this.datasource.template);
        template.paths[0] = this.path;
        this.paths = this.path.split("/");
        this.http.post("/proxy/v1/catalog/datasource/" + this.datasource.id + "/files?path=" + encodeURIComponent(this.path), {})
            .subscribe((data: RemoteFile[]) => {
                this.files = data;
                this.init();
            });
    }

    private init() {
        this.initParentSelection();
        this.initChildSelection();

        let previousSelection = this.selectionService.get(this.datasource.id, this.path);
        this.selected = previousSelection !== undefined ? previousSelection : new Map<string, boolean>();
        this.filter();
    }

    private initChildSelection() {
        const allPaths = this.selectionService.getAll(this.datasource.id);
        for (let file of this.files) {
            let searchString = this.path + "/" + file.name;
            let selectedChildren = 0;
            allPaths.forEach((selection: Map<string, boolean>, path: string) => {
                if (path.startsWith(searchString)) {
                    selectedChildren += selection.size;
                }
            });
            this.selectedChildCount.set(file.name, selectedChildren);
        }
    }

    private initParentSelection() {
        //disable selection if parent directory is selected
        let parent = "";
        let nextChildIdx: number;
        for (let i in this.paths) {
            let currentPath = this.paths[i];
            if (currentPath.length !== 0) {
                parent += "/" + currentPath;
                nextChildIdx = 1 + Number(i);
                if (this.paths.length > Number(nextChildIdx)) {
                    let child = this.paths[nextChildIdx];
                    this.selectionService.get(this.datasource.id, parent).forEach((isSelected: boolean, childPath: string) => {
                        this.isParentSelected = this.isParentSelected || (childPath === child && isSelected);
                    });
                }
            }
        }
    }

    browseTo(pathIndex: number) {
        const location = this.paths.slice(0, pathIndex + 1).join("/");
        this.state.go("catalog.datasource.browse", {path: encodeURIComponent(location)}, {notify:false, reload:false});
    }

    numberOfSelectedChildren(fileName: string): number {
        return this.selectedChildCount.get(fileName);
    }

    isChecked(fileName: string) {
        return this.isParentSelected || this.selected.get(fileName) !== undefined;
    }

    onToggleAll(): void {
        //todo warn user that downstream selection will be removed, e.g.
        //todo 1. user selects file on path /a/b/c/file.txt
        //todo 2. user selects directory on path /a/b, which includes downstream /a/b/c/file.txt
        if (this.selectAll) {
            for (let file of this.files) {
                this.selected.set(file.name, this.selectAll);
            }
        } else {
            this.selected = new Map<string, boolean>();
        }

        this.storeSelection();
    }

    onToggleRow(event: any, file: RemoteFile): void {
        if (event.checked) {
            this.selected.set(file.name, event.checked);
        } else {
            this.selected.delete(file.name);
        }
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

    openSelectionDialog(): void {
        const dialogRef = this.dialog.open(SelectionDialogComponent, {
            data: {
                datasourceId: this.datasource.id
            }
        });

        dialogRef.afterClosed().subscribe(itemsWereRemoved => {
            if (itemsWereRemoved) {
                this.selectAll = false;
                this.init();
            }
        });
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
