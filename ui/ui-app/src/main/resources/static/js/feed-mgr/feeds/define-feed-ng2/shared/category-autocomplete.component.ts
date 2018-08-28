import {EventEmitter, Input, Output,OnDestroy, OnInit,Component, Inject} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {Category} from "../../../model/category/category.model";
import {MatAutocompleteSelectedEvent} from "@angular/material";
import CategoriesService from "../../../services/CategoriesService";

@Component({
    selector:"category-autocomplete",
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
        let categoryCtrl = new FormControl(value,[Validators.required])
        this.formGroup.registerControl("category",categoryCtrl);
        this.filteredCategories = categoryCtrl.valueChanges.flatMap(text => {
            return <Observable<Category[]>> Observable.fromPromise(this.categoriesService.querySearch(text));
        });
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

    ngOnDestroy() {

    }


}