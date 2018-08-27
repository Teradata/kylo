import {FieldConfig} from "./FieldConfig";
import * as angular from 'angular';

export class Chip extends FieldConfig<string> {
    static CONTROL_TYPE = "chips"
    controlType = Chip.CONTROL_TYPE;
    items:any[] = [];
    constructor(options: {} = {}) {
        super(options);
        this.items = options['items'];
        if(options['values'] !== undefined){
            this.selectedItems = options['values'].map((item:any) => {
                return item.label;
            });
        }
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
                return angular.lowercase(item.value).indexOf(value.toLowerCase()) > -1;
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