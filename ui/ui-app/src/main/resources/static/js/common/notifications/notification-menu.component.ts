import {Component, OnDestroy} from "@angular/core";
import {Subscription} from "rxjs/Subscription";

import {KyloNotification, NotificationEvent, NotificationEventListener, NotificationService} from "../../services/notification.service";

declare const DateTimeUtils: any;

/**
 * Displays a toolbar button that opens a menu containing notifications from {@link NotificationService}.
 */
@Component({
    selector: "notification-menu",
    template: `
      <button class="kylo-notification-button overflow-visible" mat-icon-button [matMenuTriggerFor]="notificationsMenu" (menuOpened)="onMenuOpened()" [ngClass]="{'loading': loading}">
        <ng-template tdLoading [tdLoadingUntil]="!loading" tdLoadingColor="accent" tdLoadingStrategy="overlay">
          <td-notification-count color="accent" [notifications]="loading ? 0 : newCount">
            <mat-icon>notifications</mat-icon>
          </td-notification-count>
        </ng-template>
      </button>
      <mat-menu #notificationsMenu="matMenu" class="kylo-notification-menu">
        <td-menu>
          <div td-menu-header class="mat-subheading-2" style="margin: 0;">Notifications</div>
          <mat-nav-list dense>
            <ng-template ngFor let-item let-index="index" let-last="last" [ngForOf]="notifications">
              <a mat-list-item [ngClass]="{'not-clickable': item.callback == null}" (click)="onClick(item)">
                <mat-icon *ngIf="!item.loading; else loadingIcon" mat-list-icon>{{item.icon}}</mat-icon>
                <ng-template #loadingIcon>
                  <mat-progress-spinner [diameter]="20" matListIcon mode="indeterminate"></mat-progress-spinner>
                </ng-template>
                <h4 matLine>{{item.message}}</h4>
                <p matLine>{{durations[index]}}</p>
              </a>
              <mat-divider *ngIf="!last"></mat-divider>
            </ng-template>
          </mat-nav-list>
        </td-menu>
      </mat-menu>
    `
})
export class NotificationMenuComponent implements NotificationEventListener, OnDestroy {

    /**
     * Duration since each notification was created.
     */
    durations: string[] = [];

    /**
     * Last time the menu was opened
     */
    lastOpened = new Date();

    /**
     * Indicates that one or more notifications are loading.
     */
    loading = false;

    /**
     * Number of new notifications since the menu was opened.
     */
    newCount = 0;

    /**
     * Ordered list of notifications
     */
    notifications: KyloNotification[] = [];

    /**
     * Notification service subscription
     */
    subscription: Subscription;

    constructor(service: NotificationService) {
        this.subscription = service.subscribe(this);

        // Initialize notifications
        this.onNotificationEvent({added: service.notifications, removed: []});
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    /**
     * Called when a menu item in clicked.
     */
    onClick(item: KyloNotification) {
        if (item.callback) {
            item.callback(item);
        }
    }

    /**
     * Called when the menu is opened.
     */
    onMenuOpened() {
        this.lastOpened = new Date();
        this.newCount = 0;

        // Update durations
        const now = new Date().getTime();
        this.durations = this.notifications.map(notification => {
            return DateTimeUtils.formatMillisAsText(now - notification.createTime.getTime(), true, false);
        });
    }

    /**
     * Called when a notification is added or removed from the {@link NotificationService}.
     */
    onNotificationEvent(event: NotificationEvent) {
        // Update list of notifications
        this.notifications = this.notifications
            .filter(notification => {
                return event.removed.every(item => notification.id !== item.id)
            })
            .concat(event.added)
            .sort((a, b) => {
                if (a.createTime.getTime() === b.createTime.getTime()) {
                    return 0;
                } else {
                    return (a.createTime.getTime() < b.createTime.getTime()) ? 1 : -1;
                }
            });

        // Update button
        this.loading = this.notifications.some(notification => notification.loading);
        this.newCount += event.added.length;
        this.newCount -= event.removed.filter(item => item.createTime.getTime() > this.lastOpened.getTime()).length;
    }
}
