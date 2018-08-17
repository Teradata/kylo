import {FieldConfig} from "./FieldConfig";

export class Chip extends FieldConfig<string> {
    controlType = 'chips';
    items:any[] = [];
    constructor(options: {} = {}) {
        super(options);
        this.items = options['items'];
    }

    disabled: boolean = false;
    chipAddition: boolean = true;
    chipRemoval: boolean = true;

    filteredFeeds: string[];

    feedsModel: string[] = [];

    ngOnInit(): void {
        this.filterFeeds('');
    }

    filterFeeds(value: string): void {
        this.filteredFeeds = this.items.filter((item: any) => {
            if (value) {
                return item.value.indexOf(value.toLowerCase()) > -1;
            } else {
                return false;
            }
        }).filter((filteredItem: any) => {
            return this.feedsModel ? this.feedsModel.indexOf(filteredItem.label) < 0 : true;
        }).map((item:any) => {
            return item.label;
        });
    }
}