import {EventEmitter, Input, Output,OnDestroy, OnInit,Component, Inject} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {Category} from "../../../model/category/category.model";
import {MatAutocompleteSelectedEvent} from "@angular/material";
import CategoriesService from "../../../services/CategoriesService";
import {flatMap, startWith} from "rxjs/operators";
import {fromPromise} from "rxjs/observable/fromPromise";
import {CategoryAutocompleteValidators} from "./category-autocomplete.validators";

@Component({
    selector:"category-autocomplete",
    styleUrls:["js/feed-mgr/feeds/define-feed-ng2/shared/category-autocomplete.component.css"],
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/shared/category-autocomplete.component.html"
})
export class CategoryAutocompleteComponent implements OnInit, OnDestroy{

    @Input()
    formGroup:FormGroup

    @Input()
    category?:Category;

    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;

    @Output()
    categorySelected:EventEmitter<Category> = new EventEmitter<Category>();

    constructor(@Inject("CategoriesService")private categoriesService:CategoriesService){



    }

    ngOnInit(){
        if(this.formGroup == undefined){
            this.formGroup = new FormGroup({});
        }
        let value = null;
        if(this.category) {
            value = this.category
        }
        let categoryCtrl = new FormControl(value,[Validators.required, CategoryAutocompleteValidators.validateFeedCreatePermissionForCategory]);
        this.formGroup.registerControl("category",categoryCtrl);
        this.filteredCategories = categoryCtrl.valueChanges.pipe(
            startWith(''),
            flatMap(text => {
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

    onCategorySelected(event:MatAutocompleteSelectedEvent){
        let category = <Category> event.option.value;
        this.categorySelected.emit(category);
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

    ngOnDestroy() {

    }


}