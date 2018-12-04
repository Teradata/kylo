import {Component, Inject, OnInit} from '@angular/core';
import {ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableService, TdDataTableSortingOrder} from '@covalent/core/data-table';
import {IPageChangeEvent} from '@covalent/core/paging';
import {SelectionService} from '../../../api/services/selection.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Node} from '../../../api/models/node';

export class SelectedItem {
    node: Node;
    path: string;
    constructor(path: string, node: Node) {
        this.node = node;
        this.path = path;
    }
}


@Component({
    selector: 'selection-dialog',
    styleUrls: ['./selection-dialog.component.scss'],
    templateUrl: './selection-dialog.component.html',
})
export class SelectionDialogComponent implements OnInit {

    sortBy = 'path';
    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;
    searchTerm: string = '';
    filteredFiles: SelectedItem[] = [];
    filteredTotal = 0;
    fromRow: number = 1;
    currentPage: number = 1;
    pageSize: number = 10;
    columns: ITdDataTableColumn[] = [
        {name: "remove", label: "", sortable: false, width: 15},
        {name: "path", label: "Path", sortable: false, width: 1250},
    ];
    datasourceId: string;
    selected: SelectedItem[] = [];
    removed: SelectedItem[] = [];
    initialItemCount: number = 0;

    constructor(private selfReference: MatDialogRef<SelectionDialogComponent>, private dataTableService: TdDataTableService,
                private selectionService: SelectionService, @Inject(MAT_DIALOG_DATA) public data: any) {
        this.datasourceId = data.datasourceId;
    }

    public ngOnInit(): void {
        const root: Node = this.selectionService.get(this.datasourceId);
        const nodes = root.getSelectedDescendants();
        for (let node of nodes) {
            this.selected.push(new SelectedItem(node.getPathNodes().map(n => n.getName()).join("/"), node));
        }
        this.initialItemCount = this.selected.length;
        this.filter();
    }


    onOk() {
        if (this.isSelectionUpdated()) {
            for (let item of this.removed) {
                item.node.setSelected(false);
            }
            this.selfReference.close(true);
        } else {
            this.selfReference.close(false);
        }
    }

    private isSelectionUpdated() {
        return this.initialItemCount !== this.selected.length;
    }

    removeItem(toBeRemoved: SelectedItem) {
        this.selected = this.selected.filter(item => item !== toBeRemoved);
        this.removed.push(toBeRemoved);
        this.filter();
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
        setTimeout(() => {
            //async because otherwise ExpressionChangedAfterItHasBeenCheckedError
            // occurs when changing page size
            this.filter();
        });
    }

    private filter(): void {
        let newData: any[] = this.selected;
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
