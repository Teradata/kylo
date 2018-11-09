import {Component, EventEmitter, Inject, Input, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {MatAutocompleteSelectedEvent} from "@angular/material";
import {Observable} from "rxjs/Observable";
import {fromPromise} from "rxjs/observable/fromPromise";
import {mergeMap} from "rxjs/operators/mergeMap";
import {startWith} from "rxjs/operators/startWith";

import {Category} from "../../../model/category/category.model";
import {CategoriesService} from "../../../services/CategoriesService";
import {CategoryAutocompleteValidators} from "./category-autocomplete.validators";

@Component({
    selector: "category-autocomplete",
    styleUrls: ["./category-autocomplete.component.css"],
    templateUrl: "./category-autocomplete.component.html"
})
export class CategoryAutocompleteComponent implements OnInit {

    @Input()
    formGroup: FormGroup;

    @Input()
    category?: Category;

    @Input()
    placeholder?: string = "Category";

    @Input()
    hint?: string;

    @Input()
    renderClearButton?: boolean = false;

    @Input()
    required: boolean = true;

    categoryControl: FormControl;

    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;

    @Output()
    categorySelected: EventEmitter<Category> = new EventEmitter<Category>();

    constructor(@Inject("CategoriesService") private categoriesService: CategoriesService) {
    }

    ngOnInit() {
        if (this.formGroup == undefined) {
            this.formGroup = new FormGroup({});
        }
        let value = null;
        if (this.category) {
            value = this.category
        }
        let validators: ValidatorFn[] = [];
        if (this.required) {
            validators.push(Validators.required);
        }
        validators.push(CategoryAutocompleteValidators.validateFeedCreatePermissionForCategory);

        this.categoryControl = new FormControl(value, validators);
        this.formGroup.registerControl("category", this.categoryControl);

        this.filteredCategories = this.categoryControl.valueChanges.pipe(
            startWith(''),
            mergeMap(text => {
                return fromPromise(this.categoriesService.querySearch(text));
            })
        );
    }

    /**
     * Function for the Autocomplete to display the name of the category object matched
     * @param {Category} category
     * @return {string | undefined}
     */
    categoryAutocompleteDisplay(category?: Category): string | undefined {
        return category ? category.name : undefined;
    }

    onCategorySelected(event: MatAutocompleteSelectedEvent) {
        let category = <Category> event.option.value;
        this.categorySelected.emit(category);
    }

    clearCategorySelection() {
        this.categoryControl.setValue("");
        this.categorySelected.emit(null);
    }

    /**
     * return the selected category object, or null if not set
     * @return {null}
     */
    getCategoryValue(){
        return typeof this.categoryControl.value == "string" ? null : this.categoryControl.value;
    }

    /**
     * Check that category is provided. It is required field.
     * @param {FormGroup} formGroup
     * @param {string} controlName
     * @returns {boolean}
     */
    checkRequired(formGroup: FormGroup, controlName: string) {
        return formGroup.get(controlName).hasError('required')
    }

    /**
     * Check that entity access permissions allow feed creation for this category
     * @param {FormGroup} formGroup
     * @param {string} controlName
     * @returns {boolean}
     */
    checkFeedCreateAccess(formGroup: FormGroup, controlName: string) {
        return formGroup.get(controlName).hasError('noFeedCreatePermissionForCategory');
    }
}
