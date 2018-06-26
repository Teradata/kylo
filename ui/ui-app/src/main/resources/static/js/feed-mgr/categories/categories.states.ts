import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { CategoryDetailsController } from "./category-details.component";
import { CategoriesControllerComponent } from "./CategoriesController.component";

export const categoriesStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.CATEGORIES.state, 
        url: "/categories", 
        views: { 
            "content": { 
                component: CategoriesControllerComponent 
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
                component: CategoryDetailsController
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Category Details',
            permissions:AccessConstants.UI_STATES.CATEGORY_DETAILS.permissions
        }
    }
]; 