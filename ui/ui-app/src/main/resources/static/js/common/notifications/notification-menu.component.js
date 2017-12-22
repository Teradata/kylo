var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "../../services/notification.service"], function (require, exports, core_1, notification_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Displays a toolbar button that opens a menu containing notifications from {@link NotificationService}.
     */
    var NotificationMenuComponent = /** @class */ (function () {
        function NotificationMenuComponent(service) {
            /**
             * Duration since each notification was created.
             */
            this.durations = [];
            /**
             * Last time the menu was opened
             */
            this.lastOpened = new Date();
            /**
             * Indicates that one or more notifications are loading.
             */
            this.loading = false;
            /**
             * Number of new notifications since the menu was opened.
             */
            this.newCount = 0;
            /**
             * Ordered list of notifications
             */
            this.notifications = [];
            this.subscription = service.subscribe(this);
            // Initialize notifications
            this.onNotificationEvent({ added: service.notifications, removed: [] });
        }
        NotificationMenuComponent.prototype.ngOnDestroy = function () {
            if (this.subscription) {
                this.subscription.unsubscribe();
            }
        };
        /**
         * Called when a menu item in clicked.
         */
        NotificationMenuComponent.prototype.onClick = function (item) {
            if (item.callback) {
                item.callback(item);
            }
        };
        /**
         * Called when the menu is opened.
         */
        NotificationMenuComponent.prototype.onMenuOpened = function () {
            this.lastOpened = new Date();
            this.newCount = 0;
            // Update durations
            var now = new Date().getTime();
            this.durations = this.notifications.map(function (notification) {
                return DateTimeUtils.formatMillisAsText(now - notification.createTime.getTime(), true, false);
            });
        };
        /**
         * Called when a notification is added or removed from the {@link NotificationService}.
         */
        NotificationMenuComponent.prototype.onNotificationEvent = function (event) {
            var _this = this;
            // Update list of notifications
            this.notifications = this.notifications
                .filter(function (notification) {
                return event.removed.every(function (item) { return notification.id !== item.id; });
            })
                .concat(event.added)
                .sort(function (a, b) {
                if (a.createTime.getTime() === b.createTime.getTime()) {
                    return 0;
                }
                else {
                    return (a.createTime.getTime() < b.createTime.getTime()) ? 1 : -1;
                }
            });
            // Update button
            this.loading = this.notifications.some(function (notification) { return notification.loading; });
            this.newCount += event.added.length;
            this.newCount -= event.removed.filter(function (item) { return item.createTime.getTime() > _this.lastOpened.getTime(); }).length;
        };
        NotificationMenuComponent = __decorate([
            core_1.Component({
                selector: "notification-menu",
                template: "\n      <button class=\"kylo-notification-button overflow-visible\" mat-icon-button [matMenuTriggerFor]=\"notificationsMenu\" (menuOpened)=\"onMenuOpened()\" [ngClass]=\"{'loading': loading}\">\n        <ng-template tdLoading [tdLoadingUntil]=\"!loading\" tdLoadingColor=\"accent\" tdLoadingStrategy=\"overlay\">\n          <td-notification-count color=\"accent\" [notifications]=\"loading ? 0 : newCount\">\n            <mat-icon>notifications</mat-icon>\n          </td-notification-count>\n        </ng-template>\n      </button>\n      <mat-menu #notificationsMenu=\"matMenu\" class=\"kylo-notification-menu\">\n        <td-menu>\n          <div td-menu-header class=\"mat-subheading-2\" style=\"margin: 0;\">Notifications</div>\n          <mat-nav-list dense>\n            <ng-template ngFor let-item let-index=\"index\" let-last=\"last\" [ngForOf]=\"notifications\">\n              <a mat-list-item [ngClass]=\"{'not-clickable': item.callback == null}\" (click)=\"onClick(item)\">\n                <mat-icon *ngIf=\"!item.loading; else loadingIcon\" mat-list-icon>{{item.icon}}</mat-icon>\n                <ng-template #loadingIcon>\n                  <mat-progress-spinner [diameter]=\"20\" matListIcon mode=\"indeterminate\"></mat-progress-spinner>\n                </ng-template>\n                <h4 matLine>{{item.message}}</h4>\n                <p matLine>{{durations[index]}}</p>\n              </a>\n              <mat-divider *ngIf=\"!last\"></mat-divider>\n            </ng-template>\n          </mat-nav-list>\n        </td-menu>\n      </mat-menu>\n    "
            }),
            __metadata("design:paramtypes", [notification_service_1.NotificationService])
        ], NotificationMenuComponent);
        return NotificationMenuComponent;
    }());
    exports.NotificationMenuComponent = NotificationMenuComponent;
});
//# sourceMappingURL=notification-menu.component.js.map