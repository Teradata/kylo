import { Component, Input } from "@angular/core";
import { ITdDataTableColumn, TdDataTableSortingOrder, ITdDataTableSortChangeEvent } from "@covalent/core/data-table";
import { IPageChangeEvent } from "@covalent/core/paging";

@Component({
    selector : 'filtered-paginated-table-view',
    templateUrl : 'js/common/filtered-paginated-table-view/filteredPaginatedTableView.html'
})
export class FilteredPaginatedTableViewComponent {
    @Input() cardTitle : string = "";
    @Input() onSearchDebounce : any;
    @Input() filteredData : any[];
    @Input() columns : ITdDataTableColumn[];
    @Input() clickable : boolean = true;
    @Input() sortable : boolean = true;
    @Input() sortOrder : TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    @Input() sortBy : string = "";
    @Input() onSortChange : any;
    @Input() onRowClick : any;
    @Input() firstLast : boolean = false;
    @Input() pageSize : number = 5;
    @Input() filteredTotal : number = 0;
    @Input() onPageSizeChange : any;
    @Input() allowedPageSize : number[] = [5,10,20,50];
    constructor(){

    }
    pageSizeChange(pagingEvent: IPageChangeEvent): void {
        this.onPageSizeChange(pagingEvent);
    }

    search(searchTerm: string): void {
        this.onSearchDebounce(searchTerm);
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.onSortChange(sortEvent);
    }
    rowClicked(clickEvent : any){
        this.onRowClick(clickEvent);
    }

}