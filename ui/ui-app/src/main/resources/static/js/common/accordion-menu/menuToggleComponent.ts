import * as _ from "underscore";
import AccessControlService from "../../services/AccessControlService";
import { Component, Input, Output, EventEmitter, SimpleChanges, ElementRef } from "@angular/core";
import { ObjectUtils } from "../utils/object-utils";

// TODO 
// ng-if-permission [ngIfPermission]="item.permission" at line 20
@Component({
    selector: 'menu-toggle',
    template: `
        <div class="collapsible-item" [ngClass]="{open: section.expanded}" *ngIf="section.hidden == false" id="{{section.elementId}}">
            <div class="title" [ngClass]="{disabled: section.disabled}" (click)="toggle()" flex layout-align="start start" layout="row">
                <span flex style="font-size: 14px;">{{section.text | translate}}</span>
                <ng-md-icon md-icon icon="{{section.expandIcon}}" *ngIf="!isCollapsed"></ng-md-icon>
            </div>
            <div class="accordion-body">
                <mat-list id="menu-{{section.text}}" class="accordion-list">
                    <div>
                        <mat-list-item *ngFor="let item of section.links" style="height:auto;">
                            <div>   
                                <menu-link [section]="item" [collapsed]="isCollapsed"></menu-link>
                            </div>
                        </mat-list-item>
                    </div>
                </mat-list> 
            </div>
        </div>
    `
})
export class MenuToggleComponent {

    isOpened: boolean = false;

    @Input() section: any;
    @Input() isCollapsed: boolean;

    @Output() openToggleItemEmitter: EventEmitter<any> = new EventEmitter<any>();

    constructor(private accessControlService: AccessControlService,
                private element: ElementRef) {}

    ngOnInit() {

        this.section.hidden = true;
        this.isOpened = this.section.expanded;
        if(this.isOpened) {
            this.section.expandIcon = 'expand_less';
        }
        else {
            this.section.expandIcon = 'expand_more';
        }

        this.checkPermissions();
    }

    ngOnChanges(changes: SimpleChanges) {
        if(changes.isCollapsed.currentValue && !changes.isCollapsed.firstChange) {
            if(changes.isCollapsed.currentValue) {
                $(this.element.nativeElement).find('.toggle-label').addClass('collapsed-toggle-header').removeClass('layout-padding-left-8');
                $(this.element.nativeElement).find('.menu-link').removeClass('layout-padding-left-8');
            }
            else {
                $(this.element.nativeElement).find('.toggle-label').removeClass('collapsed-toggle-header').addClass('layout-padding-left-8')
                $(this.element.nativeElement).find('.menu-link').addClass('layout-padding-left-8');
            }
        }
    }

    checkPermissions() {
        this.accessControlService.doesUserHavePermission(this.getTogglePermissions()).then((allowed: any)=>{
            //if not allowed, remove the links;
            if(!allowed){
                this.section.links = [];
            }

            this.section.hidden = !allowed;
        })
    }

    getTogglePermissions() {
        var allPermissions: any[] = [];
        _.each(this.section.links,(item: any)=>{
            var permissionStr = item.permission;
            if(permissionStr != undefined) {
                var arr = [];
                if(ObjectUtils.isArray(permissionStr)) {
                    arr = permissionStr;
                    //the directive template uses the permission key to check.  it needs to be a string.
                    item.permission = arr.join(",")
                }
                else {
                    arr = permissionStr.split(',');
                }
                allPermissions = _.union(allPermissions, arr);
            }
        });
        return allPermissions;
    }
    
    toggle() {
        if(!this.section.expanded) {
            this.openToggleItemEmitter.emit(this.section);
        }
        else {
            this.section.expanded = false;
            this.section.expandIcon = 'expand_more';
        }
    };
}
