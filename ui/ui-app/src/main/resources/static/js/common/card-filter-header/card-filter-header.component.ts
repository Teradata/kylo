import { Component, Input, Output, EventEmitter, ElementRef, SimpleChanges } from '@angular/core';
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import * as $ from "jquery";
import { ObjectUtils } from "../../../lib/common/utils/object-utils";

@Component({
    selector: 'tba-card-filter-header',
    templateUrl: './card-filter-header-template.html',
    styleUrls: ['./card-filter-header-style.css']
})
export class CardFilterHeaderComponent {

    @Input() cardTitle: any;
    @Input() viewType: any;
    @Input() filterModel: any;
    @Input() filterModelOptions: any;
    @Input() sortOptions: any;
    @Input() pageName: any;
    @Input() onSelectedOption: any;
    @Input() additionalOptions: any;
    @Input() onSelectedAdditionalOption: any;
    @Input() onMenuOpen: any;
    @Input() onShowFilterHelp: any;
    @Input() renderFilter: any;
    @Input() cardController: any;
    @Input() customFilterTemplate: any;
    @Input() renderHelp:any;

    @Output()
    viewTypeChange: EventEmitter<string> = new EventEmitter<string>();

    @Output()
    filterModelChange: EventEmitter<string> = new EventEmitter<string>();

    ngAfterViewInit() {
        $(this.elRef.nativeElement).parents('.md-toolbar-tools:first').addClass('card-filter-header');
    }

    ngOnInit() {

        this.filterModelOptions = this.filterModelOptions || {};
        this.renderFilter = ObjectUtils.isUndefined(this.renderFilter) ? true : this.renderFilter;
        this.renderHelp = ObjectUtils.isDefined(this.onShowFilterHelp);
        this.customFilterTemplate = ObjectUtils.isUndefined(this.customFilterTemplate) ? '' : this.customFilterTemplate;
    
    }

    constructor(private elRef: ElementRef,
        private TableOptionsService: DefaultTableOptionsService) {}

    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedOption (option: any) {
        if (option.type == 'sort') {
            this.TableOptionsService.toggleSort(this.pageName, option);
            if (this.onSelectedOption) {
                this.onSelectedOption(option);
            }
        }
    }

    selectedAdditionalOption (option: any) {
        if (this.onSelectedAdditionalOption) {
            this.onSelectedAdditionalOption(option);
        }
    }

    showFilterHelpPanel (ev: any) {
        if (this.onShowFilterHelp) {
            this.onShowFilterHelp(ev);
        }
    }

    /**
     *
     * @param options {sortOptions:this.sortOptions,additionalOptions:this.additionalOptions}
     */
    menuOpen (options: any) {
        if (this.onMenuOpen) {
            this.onMenuOpen(options);
        }
    }

    onViewTypeChange(viewType:any) {
        this.viewTypeChange.emit(viewType);
    }

    changeFilter(filterValue: any) {
        this.filterModelChange.emit(filterValue);
    }

}

