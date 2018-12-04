import {FieldConfig} from "./FieldConfig";
import * as _ from "underscore"

export class Chip extends FieldConfig<string> {
    static CONTROL_TYPE = "chips"
    controlType = Chip.CONTROL_TYPE;
    items:any[] = [];
    stacked:boolean = false;
    constructor(options: {} = {}) {
        super(options);
        this.items = options['items'];
        if(options['values'] !== undefined && _.isArray(options['values'])){
            this.selectedItems = options['values'].map((item:any) => {
                return item.label;
            });
            this.filteredItems = this.selectedItems;
        }
        this.stacked = options['stacked'] || false;
    }

    disabled: boolean = false;
    chipAddition: boolean = true;
    chipRemoval: boolean = true;

    filteredItems: string[];

    selectedItems: string[] = [];

    ngOnInit(): void {
        this.filterItems('');
    }

    filterItems(value: string): void {
        this.filteredItems = this.items.filter((item: any) => {
            if (value) {
                return item.value && (item.value as string).toLowerCase().indexOf(value.toLowerCase()) > -1;
            } else {
                return false;
            }
        }).filter((filteredItem: any) => {
            return this.selectedItems ? this.selectedItems.indexOf(filteredItem.label) < 0 : true;
        }).map((item:any) => {
            return item.label;
        });
    }

    updateModel(value: any) {

        this.model['values'] = this.items.filter((item: any) => {
            return this.selectedItems ? this.selectedItems.indexOf(item.label) > -1 : false;
        });
    }
}