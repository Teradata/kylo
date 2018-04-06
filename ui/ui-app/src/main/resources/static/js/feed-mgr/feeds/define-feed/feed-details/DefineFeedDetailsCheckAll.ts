import * as _ from "underscore";
export class CheckAll {
    isChecked: boolean = true;
    isIndeterminate: boolean = false;
    totalChecked: number = 0;
    clicked(checked: any,self:any) {
        if (checked) {
            this.totalChecked++;
        }
        else {
            this.totalChecked--;
        }
        this.markChecked(self);
    };
    markChecked(self:any) {
        if (this.totalChecked == self.model.table.fieldPolicies.length) {
            this.isChecked = true;
            this.isIndeterminate = false;
        }
        else if (this.totalChecked > 0) {
            this.isChecked = false;
            this.isIndeterminate = true;
        }
        else if (this.totalChecked == 0) {
            this.isChecked = false;
            this.isIndeterminate = false;
        }
    };
}
/**
         * Toggle Check All/None on Profile column
         * Default it to true
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
*/
export class ProfileCheckAll extends CheckAll {
    isChecked: boolean = true;
    isIndeterminate: boolean = false;
    toggleAll(self:any) {
        var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
        _.each(self.model.table.fieldPolicies, function (field: any) {
            field.profile = checked;
        });
        if (checked) {
            this.totalChecked = self.model.table.fieldPolicies.length;
        }
        else {
            this.totalChecked = 0;
        }
        this.markChecked(self);
    };
    setup(self:any) {
        self.profileCheckAll.totalChecked = 0;
        _.each(self.model.table.fieldPolicies, function (field: any) {
            if (field.profile) {
                self.profileCheckAll.totalChecked++;
            }
        });
        self.profileCheckAll.markChecked(self);
    }
}
/**
         *
         * Toggle check all/none on the index column
         *
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
*/
export class IndexCheckAll extends CheckAll {
    isChecked:boolean = false;
    isIndeterminate:boolean = false;
    toggleAll(self:any) {
        var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
        _.each(self.model.table.fieldPolicies, function (field: any) {
            field.index = checked;
        });
        this.isChecked = checked;

        if (checked) {
            this.totalChecked = self.model.table.fieldPolicies.length;
        }
        else {
            this.totalChecked = 0;
        }
        this.markChecked(self);
    };
    setup(self:any) {
        self.indexCheckAll.totalChecked = 0;
        _.each(self.model.table.fieldPolicies, function (field: any) {
            if (field.index) {
                self.indexCheckAll.totalChecked++;
            }
        });
        self.indexCheckAll.markChecked(self);
    }
}