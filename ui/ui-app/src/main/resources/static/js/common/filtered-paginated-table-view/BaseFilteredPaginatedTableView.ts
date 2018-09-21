import { TdDataTableSortingOrder, ITdDataTableSortChangeEvent, ITdDataTableColumn, TdDataTableService } from "@covalent/core/data-table";
import { IPageChangeEvent } from "@covalent/core/paging";

export class BaseFilteredPaginatedTableView {

    filteredData: any[];
    filteredTotal: number = 0;

    sortBy: string;

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

    pageSize: number = 5;
    fromRow: number = 1;
    searchTerm: string = '';
    currentPage: number = 1;
    public data : any[] = [];
    public columns : ITdDataTableColumn[];
    public _dataTableService : TdDataTableService;

    constructor(_dataTableService: TdDataTableService){
        this._dataTableService = _dataTableService;
    }

    setDataAndColumnSchema(data : any[], columns : ITdDataTableColumn[]) : void {
        this.data = data;
        this.columns = columns;
        this.filteredData = data;
        this.filteredTotal = data.length;
    }

    setSortBy(sortBy : string) : void{
        this.sortBy = sortBy;
    }

    setSortOrder (sortOrder: TdDataTableSortingOrder) : void {
        this.sortOrder = sortOrder;
    }

    onPageSizeChange (pagingEvent: IPageChangeEvent) {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.filter();
    }

    search (searchTerm: string): void {
        this.searchTerm = searchTerm;
        this.filter();
    }

    sort (sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }

    filter(): void {
        let newData: any[] = this.data;
        let excludedColumns: string[] = this.columns
            .filter((column: ITdDataTableColumn) => {
                return ((column.filter === undefined && column.hidden === true) ||
                    (column.filter !== undefined && column.filter === false));
            }).map((column: ITdDataTableColumn) => {
                return column.name;
            });
        newData = this._dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
        this.filteredTotal = newData.length;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredData = newData;
    }
}