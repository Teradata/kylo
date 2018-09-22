import { AbstractControl } from '@angular/forms';

/**
 * Validators for category auto-completion component during feed setup
 */
export class CategoryAutocompleteValidators {

    /**
     * Check if feed creation is allowed for category based upon entity access permissions
     * @param {AbstractControl} control
     * @returns {any}
     */
    static validateFeedCreatePermissionForCategory(control: AbstractControl) {
        if (control.value!=undefined && !control.value.createFeed && control.value!='') {
            return { "noFeedCreatePermissionForCategory": true };
        }

        return null;
    }
}