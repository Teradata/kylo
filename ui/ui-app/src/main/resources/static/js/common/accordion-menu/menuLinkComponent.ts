import { Component, Input, Output, EventEmitter } from "@angular/core";
'use strict';

@Component({
    selector: 'menu-link',
    template: `
        <a flex uiSref="{{section.sref}}" (click)="selectMenuItem()" class="nav-btn md-button md-kylo-theme md-ink-ripple" [ngClass]="{'selected' : section.selected,'md-icon-button': collapsed}">
            <div style="float: left;" class="layout-padding-left-8 menu-link" *ngIf="collapsed" matTooltip="{{section.text}}" placement="right">
                <ng-md-icon md-icon style="fill:#607D8B;" icon="{{section.icon}}" class="nav-btn" [ngClass]="{'selected' : section.selected }" ></ng-md-icon>
            </div>
            <div style="float: left;" class="layout-padding-left-8 menu-link" *ngIf="!collapsed">
                <ng-md-icon md-icon style="fill:#607D8B;" icon="{{section.icon}}" class="nav-btn" [ngClass]="{'selected' : section.selected }" ></ng-md-icon>
                <span style="padding-left:10px;color: #607D8B;text-transform: none;">{{section.text | translate}}</span>
            </div>
        </a>
    `
})
export class menuLinkComponent {

    @Input() section: any;
    @Input() currentSection: any;
    @Input() collapsed: boolean;

    @Output() isCollapsedEmitter: EventEmitter<any> = new EventEmitter<any>();
    @Output() autoFocusContentEmitter: EventEmitter<any> = new EventEmitter<boolean>();

    selectMenuItem() {
        this.autoFocusContentEmitter.emit();
    }

    isCollapsed() {
        return this.isCollapsedEmitter.emit();
    }
    
    sectionClass() {
        if(this.section == this.currentSection ) {
            return 'selected';
        }
        else {
            return 'nav-btn';
        }
    }
}
