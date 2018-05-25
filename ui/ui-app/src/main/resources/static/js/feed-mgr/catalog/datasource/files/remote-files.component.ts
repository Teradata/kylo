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
import {Node} from './node';


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
    sortBy = this.columns[1].name;
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    searchTerm: string = '';
    filteredFiles: RemoteFile[] = [];
    filteredTotal = 0;
    fromRow: number = 1;
    currentPage: number = 1;
    pageSize: number = 50;
    selected: Node[] = [];
    selectAll: boolean = false;
    isParentSelected: boolean = false;
    selectedDescendantCounts: Map<string, number> = new Map<string, number>();

    paths: string[];
    files: RemoteFile[] = [];
    private root: Node;
    private node: Node;

    constructor(private dataTableService: TdDataTableService, private http: HttpClient,
                private state: StateService, private selectionService: SelectionService,
                private dialog: MatDialog) {
    }

    public ngOnInit(): void {
        this.paths = this.path.split("/");
        this.initNodes();
        const node = this.node;
        this.http.post("/proxy/v1/catalog/datasource/" + this.datasource.id + "/files?path=" + encodeURIComponent(this.path), {})
            .subscribe((data: RemoteFile[]) => {
                this.files = data;
                for (let file of this.files) {
                    node.addChild(new Node(file.name));
                }
                this.init();
            });
    }

    private init() {
        this.initIsParentSelected();
        this.initSelectedDescendantCounts();
        this.filter();
    }

    private initNodes() {
        this.root = this.selectionService.get(this.datasource.id);
        if (this.root === undefined) {
            this.root = this.createNode(this.getPaths());
            this.selectionService.set(this.datasource.id, this.root);
        }
        this.node = this.root.find(this.getPaths());
    }

    getPaths(): string[] {
        return angular.copy(this.paths);
    }

    createNode(paths: string[]): Node {
        const node = new Node(paths.splice(0, 1)[0]);
        if (paths.length > 0) {
            const child = this.createNode(paths);
            node.addChild(child);
        }
        return node;
    }

    private initSelectedDescendantCounts() {
        for (let node of this.node.children()) {
            this.selectedDescendantCounts.set(node.name, node.countSelectedDescendants());
        }
    }

    private initIsParentSelected() {
        this.isParentSelected = this.node.isAnyParentSelected();
    }

    browseTo(pathIndex: number) {
        const location = this.getPaths().slice(0, pathIndex + 1).join("/");
        this.state.go("catalog.datasource.browse", {path: encodeURIComponent(location)}, {notify:false, reload:false});
    }

    isChecked(fileName: string) {
        return this.isParentSelected || this.node.isChildSelected(fileName);
    }

    onToggleAll(): void {
        this.node.toggleAll(this.selectAll);
        this.init();
    }

    onToggleRow(event: any, file: RemoteFile): void {
        this.node.toggleChild(file.name, event.checked);
        this.init();
    }

    numberOfSelectedDescendants(fileName: string): number {
        return this.selectedDescendantCounts.get(fileName);
    }

    selectedHere() {
        return this.node.countSelectedChildren();
    }

    selectedTotal() {
        return this.root.countSelectedDescendants();
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
