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
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {DatasourceComponent} from '../datasource.component';
import {finalize} from 'rxjs/operators/finalize';

@Component({
    selector: "remote-files",
    styleUrls: ["js/feed-mgr/catalog/datasource/api/browser.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasource/api/browser.component.html"
})
export class BrowserComponent implements OnInit {

    private static LOADER: string = "BrowserComponent.LOADER";
    private static LOADER1: string = "BrowserComponent.LOADER1";

    @Input()
    public datasource: DataSource;

    @Input()
    params: any;

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

    files: BrowserObject[] = [];
    private root: Node;
    private node: Node;
    private pathNodes: Node[];

    constructor(private dataTableService: TdDataTableService, private http: HttpClient,
                private state: StateService, private selectionService: SelectionService,
                private dialog: MatDialog, private loadingService: TdLoadingService) {
        this.columns = this.getColumns();
        this.sortBy = this.getSortByColumnName();

        this.loadingService.create({
            name: BrowserComponent.LOADER,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });

    }

    public ngOnInit(): void {
        if(this.params == undefined){
            //attempt to get it from the selection service
            this.params = this.selectionService.getLastPath(this.datasource.id);
        }
        this.initNodes();
        this.init();
    }

    /**
     * To be implemented by subclasses, e.g. load data from server.
     * This method is called during standard lifecycle ngOnInit method.
     */
    init(): void {

    }

    /**
     * Needs to be explicitly called by subclasses to load data from server, e.g. during init() method.
     */
    initData(): void {
        const thisNode = this.node;
        this.loadingService.register(BrowserComponent.LOADER);
        this.loadingService.register(BrowserComponent.LOADER1);
        this.http.get(this.getUrl(), {params: this.params})
            .pipe(finalize(() => {
                console.log('finalise');
                this.loadingService.resolve(BrowserComponent.LOADER);
                this.loadingService.resolve(BrowserComponent.LOADER1);
            }))
            .subscribe((data: Array<any>) => {
                console.log('received');
                this.files = data.map(serverObject => {
                    const browserObject = this.mapServerResponseToBrowserObject(serverObject);
                    const node = new Node(browserObject.name);
                    node.setBrowserObject(browserObject);
                    thisNode.addChild(node);
                    return browserObject;
                });
                this.initSelection();
                this.filter();
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

    /**
     * To be implemented by subclasses
     * @returns {Node} root node of the hierarchy
     */
    createRootNode(): Node {
        return undefined;
    }

    /**
     * To be implemented by subclasses.
     * @param {BrowserObject} obj
     * @returns {any} query parameters when navigating into child browser object
     */
    createChildBrowserObjectParams(obj: BrowserObject): any {
        return undefined;
    }

    /**
     * To be implemented by subclasses.
     * @param {Node} node
     * @returns {any} query parameters when navigating to any parent node
     */
    createParentNodeParams(node: Node): any {
        return undefined;
    }

    /**
     * To be implemented by subclasses.
     * @param {Node} root
     * @param params query parameters
     * @returns {Node} must return hierarchy node for browser location identified by query parameters
     */
    findOrCreateThisNode(root: Node, params: any): Node {
        return undefined;
    }

    private initSelection(): void {
        this.initIsParentSelected();
        this.initSelectedDescendantCounts();
        //mark the last selected path for this datasource.id in the selection service
        this.selectionService.setLastPath(this.datasource.id, this.params)
    }

    private initNodes(): void {
        this.root = this.selectionService.get(this.datasource.id);
        if (this.root === undefined) {
            this.root = this.createRootNode();
            this.selectionService.set(this.datasource.id, this.root);
        }
        this.node = this.findOrCreateThisNode(this.root, this.params);
        this.pathNodes = this.node.getPathNodes();
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
            this.browse(this.createChildBrowserObjectParams(obj));
        }
    }

    breadcrumbClick(node: Node): void {
        this.browse(this.createParentNodeParams(node));
    }

    private browse(params: any): void {
        this.browseTo(params, undefined);
    }

    /**
     * @param {string} params
     * @param {string} location to replace OS browser location set this to "replace"
     */
    browseTo(params: any, location: string): void {
        const options: any = {notify: false, reload: false};
        if (location !== undefined) {
            options.location = location;
        }
        this.state.go(this.getStateName(), params, options);
    }




    isChecked(fileName: string): boolean {
        return this.isParentSelected || this.node.isChildSelected(fileName);
    }

    onToggleAll(): void {
        this.node.toggleAll(this.selectAll);
        this.initSelection();
    }

    onToggleRow(event: any, file: BrowserObject): void {
        this.node.toggleChild(file.name, event.checked);
        this.initSelection();
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
            minWidth: 600,
            data: {
                datasourceId: this.datasource.id
            }
        });

        dialogRef.afterClosed().subscribe(itemsWereRemoved => {
            if (itemsWereRemoved) {
                this.selectAll = false;
                this.initSelection();
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
