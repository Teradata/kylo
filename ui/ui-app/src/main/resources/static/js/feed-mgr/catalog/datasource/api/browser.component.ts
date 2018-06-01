import {HttpClient} from "@angular/common/http";
import {Component, Input, OnInit} from "@angular/core";
import {ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from "@covalent/core/data-table";
import {DataSource} from '../../api/models/datasource';
import {StateService} from "@uirouter/angular";
import {IPageChangeEvent} from '@covalent/core/paging';
import {SelectionService} from '../../api/services/selection.service';
import {MatDialog} from '@angular/material/dialog';
import {SelectionDialogComponent} from './dialog/selection-dialog.component';
import {Node} from './node';
import {BrowserObject} from './browser-object';
import {BrowserColumn} from './browser-column';

@Component({
    selector: "remote-files",
    styleUrls: ["js/feed-mgr/catalog/datasource/api/browser.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/api/browser.component.html"
})
export class BrowserComponent implements OnInit {

    @Input()
    public datasource: DataSource;

    @Input()
    path: string;

    columns: BrowserColumn[];
    sortBy: string;
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    searchTerm: string = '';
    filteredFiles: BrowserObject[] = [];
    filteredTotal = 0;
    fromRow: number = 1;
    currentPage: number = 1;
    pageSize: number = 50;
    selected: Node[] = [];
    selectAll: boolean = false;
    isParentSelected: boolean = false;
    selectedDescendantCounts: Map<string, number> = new Map<string, number>();

    paths: string[];
    files: BrowserObject[] = [];
    private root: Node;
    private node: Node;
    private pathNodes: Node[] = [];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient,
                private state: StateService, private selectionService: SelectionService,
                private dialog: MatDialog) {
    }

    public ngOnInit(): void {
        this.columns = this.getColumns();
        this.sortBy = this.getSortByColumnName();
        this.initNodes();
        const node = this.node;
        this.http.get(this.getUrl() + encodeURIComponent(this.path), {})
            .subscribe((data: Array<any>) => {
                this.files = data.map(obj => {
                    const dbObj = this.mapServerResponseToBrowserObject(obj);
                    node.addChild(new Node(dbObj.name));
                    return dbObj;
                });
                this.init();
            });
    }

    /**
     * To be implemented by subclasses
     * @returns {undefined} column name for initial sort of the table
     */
    getSortByColumnName(): string {
        return undefined;
    }

    /**
     * To be implemented by subclasses
     * @returns {undefined} column descriptions
     */
    getColumns(): BrowserColumn[] {
        return undefined;
    }

    /**
     * To be implemented by subclasses
     * @returns {string}
     */
    getStateName(): string {
        return undefined;
    }

    /**
     * To be implemented by subclasses
     * @returns {string} request URL from where to get the data
     */
    getUrl(): string {
        return undefined;
    }

    /**
     * To be implemented by subclasses
     * @param obj object returned by the server
     * @returns {BrowserObject} must be a subclass of BrowserObject
     */
    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return undefined;
    }

    private init(): void {
        this.initIsParentSelected();
        this.initSelectedDescendantCounts();
        this.filter();
    }

    private initNodes(): void {
        this.root = this.selectionService.get(this.datasource.id);
        if (this.root === undefined) {
            this.root = new Node(this.datasource.template.paths[0]);
            this.selectionService.set(this.datasource.id, this.root);
        }
        this.node = this.root.findFullPath(this.path);
        this.pathNodes.push(this.node);
        let parent = this.node.parent;
        while (parent) {
            this.pathNodes.push(parent);
            parent = parent.parent;
        }
        this.pathNodes = this.pathNodes.reverse();
    }

    private initSelectedDescendantCounts(): void {
        for (let node of this.node.children()) {
            this.selectedDescendantCounts.set(node.name, node.countSelectedDescendants());
        }
    }

    private initIsParentSelected(): void {
        this.isParentSelected = this.node.isAnyParentSelected();
    }

    rowClick(obj: BrowserObject): void {
        if (obj.canBeParent()) {
            this.browse(this.path + "/" + obj.name);
        }
    }

    browseTo(node: Node): void {
        this.browse(node.path);
    }

    private browse(path: string): void {
        this.state.go(this.getStateName(), {path: encodeURIComponent(path)}, {notify: false, reload: false});
    }

    isChecked(fileName: string): boolean {
        return this.isParentSelected || this.node.isChildSelected(fileName);
    }

    onToggleAll(): void {
        this.node.toggleAll(this.selectAll);
        this.init();
    }

    onToggleRow(event: any, file: BrowserObject): void {
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
        let newData: BrowserObject[] = this.files;
        let excludedColumns: string[] = this.columns
            .filter((column: BrowserColumn) => {
                return ((column.filter === undefined && column.hidden === true) ||
                    (column.filter !== undefined && column.filter === false));
            }).map((column: BrowserColumn) => {
                return column.name;
            });
        newData = this.dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
        this.filteredTotal = newData.length;
        newData = this.dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        newData = this.dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredFiles = newData;
    }
}
