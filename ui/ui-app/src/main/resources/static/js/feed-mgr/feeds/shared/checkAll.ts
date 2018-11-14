import * as angular from 'angular';
import * as _ from 'underscore';

export class CheckAll {
    isIndeterminate: boolean = false;
    totalChecked: number = 0;
    model: any;

    constructor( private fieldName: string, public isChecked: boolean) {

    }

    setup(editModel:any) {
        this.model = editModel;
        this.totalChecked = 0;
        _.each(this.model.feedDefinitionFieldPolicies, (field) => {
            if (field[this.fieldName]) {
                this.totalChecked++;
            }
        });
        this.markChecked();
    }

    clicked(checked: boolean) {
        if (checked) {
            this.totalChecked++;
        }
        else {
            this.totalChecked--;
        }
        this.markChecked();
    }

    markChecked() {
        if (angular.isDefined(this.model) && this.totalChecked == this.model.feedDefinitionFieldPolicies.length) {
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
    }

    toggleAll() {
        var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
        if(angular.isDefined(this.model) ) {
            _.each(this.model.feedDefinitionFieldPolicies, (field) => {
                field[this.fieldName] = checked;
            });
            if (checked) {
                this.totalChecked = this.model.feedDefinitionFieldPolicies.length;
            }
            else {
                this.totalChecked = 0;
            }
        }
        else {
            this.totalChecked = 0;
        }
        this.markChecked();
    }

}