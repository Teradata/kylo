import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { CategoryDetails } from "./category-details.component";
import { CategoriesComponent } from "./Categories.component";

export const categoriesStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.CATEGORIES.state, 
        url: "/categories", 
        views: { 
            "content": { 
                component: CategoriesComponent 
            } 
        }, 
        data: { 
            breadcrumbRoot:true,
            displayName:'Categories',
            permissions:AccessConstants.UI_STATES.CATEGORIES.permissions
        } 
    },
    {
        name: AccessConstants.UI_STATES.CATEGORY_DETAILS.state,
        url :"/category-details/{categoryId}",
        params: {
            categoryId:null
        },
        views: {
            "content": {
                component: CategoryDetails
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Category Details',
            permissions:AccessConstants.UI_STATES.CATEGORY_DETAILS.permissions
        }
    }
]; 