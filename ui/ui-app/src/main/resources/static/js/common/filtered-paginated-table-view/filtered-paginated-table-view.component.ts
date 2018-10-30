import {AfterViewInit, Component, ContentChildren, Input, QueryList, ViewChild, Output, EventEmitter} from "@angular/core";
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableComponent, TdDataTableSortingOrder, TdDataTableTemplateDirective} from "@covalent/core/data-table";
import {IPageChangeEvent} from "@covalent/core/paging";

@Component({
    selector: 'filtered-paginated-table-view',
    templateUrl: './filtered-paginated-table-view.component.html'
})
export class FilteredPaginatedTableViewComponent implements AfterViewInit {

    @ContentChildren(TdDataTableTemplateDirective) templates: QueryList<TdDataTableTemplateDirective>;
    @Input() cardTitle: string = "";
    @Input() filteredData: any[];
    @Input() columns: ITdDataTableColumn[];
    @Input() clickable: boolean = true;
    @Input() sortable: boolean = true;
    @Input() sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    @Input() sortBy: string = "";
    @Input() firstLast: boolean = false;
    @Input() pageSize: number = 5;
    @Input() filteredTotal: number = 0;
    @Input() allowedPageSize: number[] = [5, 10, 20, 50];
    @ViewChild(TdDataTableComponent) dataTable: TdDataTableComponent;
    @Input() showToolbar: boolean = true;
    @Input() showTotal: boolean = true;

    @Output() searchDebounced: EventEmitter<any> = new EventEmitter<string>();
    @Output() sortChanged:  EventEmitter<any> = new EventEmitter<string>();
    @Output() rowClicked:  EventEmitter<any> = new EventEmitter<string>();
    @Output() pageSizeChanged:  EventEmitter<any> = new EventEmitter<string>();

    /**
     * Should the view be rendered in a card
     * @type {boolean}
     */
    @Input()
    cardView:boolean = true;

    @Input()
    tableHeight:number = 400;

    loading: boolean = false;

    ctx:FilteredPaginatedTableViewComponent = this;

    pageSizeChange(pagingEvent: IPageChangeEvent): void {
        this.pageSizeChanged.emit(pagingEvent);
    }

    search(searchTerm: string): void {
        this.searchDebounced.emit(searchTerm);
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortChanged.emit(sortEvent);
    }

    onRowClick(clickEvent: any) {
        this.rowClicked.emit(clickEvent);
    }

    ngAfterViewInit() {
        // Templates won't be seen by TdDataTable; need to manually link them
        this.dataTable._templates = this.templates;
        this.dataTable.ngAfterContentInit();
    }
}
