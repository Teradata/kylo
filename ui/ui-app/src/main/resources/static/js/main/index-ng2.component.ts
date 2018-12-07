import {Injector, Component, ElementRef, Inject, ViewChild} from "@angular/core";
import {LoadingMode, LoadingType, TdLoadingService} from "@covalent/core/loading";
import {RejectType, Transition, TransitionService} from "@uirouter/core";

import "app";
import {StateService} from  "../services/StateService";
import {AccessControlService} from "../services/AccessControlService";
import AccessConstants from "../constants/AccessConstants";
import {SearchService} from "../services/SearchService";
import {SideNavService} from "../services/SideNavService";

import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { downgradeComponent } from "@angular/upgrade/static";

const STATE_LOADER = "stateLoader";

@Component({
    selector:'index-controller-ng2',
    templateUrl: './index-ng2.component.html'
})
export class IndexComponent {

    LOADING_DIALOG_WAIT_TIME:number = 100;
    stateLoaderTimeout: number;
    
    topNavMenu: any = [];

    sideNavOpen: boolean = this.SideNavService.isLockOpen;
    sideNavService: any = this.SideNavService;
    searchQuery: string = null;
    allowSearch: boolean = false;
    currentState: any = null;

    @ViewChild('search') searchElement: ElementRef;

    ngOnInit() {

        // Create state loading bar
        this.loadingService.create({
            name: STATE_LOADER,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: "accent"
        });

        // Listen for state transitions
        this.transitions.onCreate({}, this.onTransitionStart.bind(this));
        this.transitions.onSuccess({}, this.onTransitionSuccess.bind(this));
        this.transitions.onError({}, this.onTransitionError.bind(this));

        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any)=> {
                this.allowSearch = this.accessControlService
                                        .hasAction(AccessConstants.GLOBAL_SEARCH_ACCESS,
                                                    actionSet.actions);
            });
        
    }

    constructor(private transitions: TransitionService,
                private loadingService: TdLoadingService,
                private StateService:StateService,
                private SearchService: SearchService,
                private SideNavService: SideNavService,
                private accessControlService:AccessControlService,
                private dialog: MatDialog,
                @Inject("$injector") private $injector: any) {}

    /**
     * Check if the Side Nav is hidden or not
     * @returns {*|boolean}
     */
    isSideNavHidden = () =>{
        return this.$injector.get("$mdMedia")('gt-md') && this.SideNavService.isLockOpen;
    };

    toggleSideNavList=()=>{
        this.sideNavService.toggle();
    };

    closeSideNavList() {
        this.sideNavService.close();
    }

    showPreSearchBar() : boolean {
        return this.searchQuery == null;
    };

    initiateSearch() : void {
        this.searchQuery = '';
    };

    focusSearchBar() : void {
        if (this.searchElement)
            this.searchElement.nativeElement.focus();
    }

    showSearchBar() : boolean {
        return this.searchQuery != null;
    };

    endSearch() : void {
        return this.searchQuery = null;
    };

    /**
     * Search for something
     */
    search = () =>{
        if (this.searchQuery != null && this.searchQuery.length > 0) {
            this.SearchService.setSearchQuery(this.searchQuery);
            if (this.currentState.name != 'search') {
                this.StateService.Search().navigateToSearch(true);
            }
        }
    };

    /**
     * Detect if a user presses Enter while focused in the Search box
     * @param $event
     */
    onSearchKeypress = ($event:any)=> {
        if ($event.which === 13) {
            this.search();
        }
    };

    loading: any = false;

    showLoadingDialog() {
        this.loading = true;

        let dialogRef = this.dialog.open(LoadingDialogComponent, {
            panelClass: "full-screen-dialog"
        });
        
    }

    /**
     * Show a loading dialog if the load takes longer than xx ms
     */
    loadingTimeout: any = setTimeout(()=> {
        this.showLoadingDialog();

    }, this.LOADING_DIALOG_WAIT_TIME);

    /**
     * Called when transitioning to a new state.
     */
    onTransitionStart(transition: Transition) {
        if (this.stateLoaderTimeout == null) {
            this.stateLoaderTimeout = setTimeout(() => this.loadingService.register(STATE_LOADER), 250);
        }
    }

    /**
     * Called when the transition was successful.
     */
    onTransitionSuccess(transition: Transition) {
        // Clear state loading bar. Ignore parent states as child states will load next.
        if (!transition.to().name.endsWith(".**")) {
            clearTimeout(this.stateLoaderTimeout);
            this.stateLoaderTimeout = null;
            this.loadingService.resolveAll(STATE_LOADER);
        }

        // Clear search query on "search" state
        this.currentState = transition.to();
        if (this.currentState.name != 'search') {
            this.searchQuery = null;
        }
        else {
            this.searchQuery = this.SearchService.getSearchQuery();
        }

        //hide the loading dialog
        if (!this.accessControlService.isFutureState(this.currentState.name)) {
            if (this.loadingTimeout != null) {
                clearTimeout(this.loadingTimeout);
                this.loadingTimeout = null;
            }
            if (this.loading) {
                this.loading = false;
                this.dialog.closeAll();
            }
        }
    }

    /**
     * Called when the transition failed.
     */
    onTransitionError(transition: Transition) {
        // Clear state loading bar. Ignore parent states (type is SUPERSEDED) as child states will load next.
        if (transition.error().type !== RejectType.SUPERSEDED) {
            clearTimeout(this.stateLoaderTimeout);
            this.stateLoaderTimeout = null;
            this.loadingService.resolveAll(STATE_LOADER);
        }
    }
}

@Component({
    templateUrl: './loading-dialog-ng2.html'
})
export class LoadingDialogComponent {
    constructor(private dialogRef: MatDialogRef<LoadingDialogComponent>){}
}
