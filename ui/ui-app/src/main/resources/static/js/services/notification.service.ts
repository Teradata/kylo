import * as angular from "angular";
import "rxjs/add/operator/auditTime";
import {Subject} from "rxjs/Subject";
import {Subscription} from "rxjs/Subscription";

import "./module"; // ensure module is loaded first

declare const IDGenerator: any;

export interface Alert {
    type: string;
    msg: string;
    id: string;
    detailMsg?: string;
    errorType: string;
    hasDetail: boolean;
    groupKey?: string;
}

/**
 * A notification about an event or task in Kylo.
 */
export class KyloNotification {

    private id_: string;
    private callback_: (notification: KyloNotification) => void;
    private icon_: string;
    private loading_: boolean;
    private message_: string;
    private createTime_ = new Date();
    private subject = new Subject<KyloNotification>();

    /**
     * Constructs a {@code KyloNotification} with the specified unique identifier and subject for property changes.
     */
    constructor(id: string, subject: Subject<KyloNotification>) {
        this.id_ = id;
        this.subject = subject;
    }

    /**
     * A unique identifier for this notification.
     */
    get id(): string {
        return this.id_;
    }

    /**
     * Called when the notification is clicked.
     */
    get callback(): (notification: KyloNotification) => void {
        return this.callback_;
    }

    set callback(value: (notification: KyloNotification) => void) {
        this.callback_ = value;
        this.trigger();
    }

    /**
     * Icon name.
     */
    get icon(): string {
        return this.icon_;
    }

    set icon(value: string) {
        this.icon_ = value;
        this.trigger();
    }

    /**
     * Indicates a progress spinner should be displayed for this notification.
     */
    get loading() {
        return this.loading_;
    }

    set loading(value: boolean) {
        this.loading_ = value;
        this.trigger();
    }

    /**
     * Title.
     */
    get message(): string {
        return this.message_;
    }

    set message(value: string) {
        this.message_ = value;
        this.trigger();
    }

    /**
     * Time when the notification was created.
     */
    get createTime(): Date {
        return this.createTime_;
    }

    /**
     * Triggers a property change event.
     */
    private trigger() {
        this.subject.next(this);
    }
}

/**
 * Fired when a notification is added or removed from the {@link NotificationService}.
 */
export interface NotificationEvent {

    /**
     * List of added notifications.
     */
    added: KyloNotification[];

    /**
     * List of removed notifications.
     */
    removed: KyloNotification[];
}

/**
 * Listens for notification events.
 */
export interface NotificationEventListener {

    /**
     * Called when a notification is added or removed from the {@link NotificationService}.
     */
    onNotificationEvent(event: NotificationEvent): void;
}

export class NotificationService {

    static readonly CONNECTION_ERRORS_ALERT_THRESHOLD = 5;

    alerts: { [id: string]: Alert } = {};

    connectionErrors = 0;

    /**
     * List of notifications.
     */
    private kyloNotifications: KyloNotification[] = [];

    /**
     * Subject for notification property changes.
     */
    private kyloNotificationSubject = new Subject<KyloNotification>();

    /**
     * Callbacks for notification events.
     */
    private notificationEventSubject = new Subject<NotificationEvent>();

    lastConnectionError = 0;

    static readonly $inject = ["$timeout", "$mdToast"];

    constructor(private $timeout: angular.ITimeoutService, private $mdToast: angular.material.IToastService) {
        // Listen for changes to KyloNotifications and create NotificationEvents
        this.kyloNotificationSubject
            .auditTime(10)
            .subscribe(() => this.notificationEventSubject.next({added: [], removed: []}));
    }

    /**
     * Gets a copy of all notifications.
     */
    get notifications() {
        return this.kyloNotifications.slice();
    }

    /**
     * Creates a new notification.
     *
     * @param message - title
     * @param icon - icon name
     * @returns the notification
     */
    addNotification(message: string, icon: string): KyloNotification {
        const notification = new KyloNotification(IDGenerator.generateId("notification"), this.kyloNotificationSubject);
        notification.message = message;
        notification.icon = icon;
        this.kyloNotifications.push(notification);
        this.notificationEventSubject.next({added: [notification], removed: []});
        return notification;
    }

    /**
     * Removes the specified notification.
     */
    removeNotification(notification: KyloNotification) {
        this.kyloNotifications = this.notifications.filter(item => notification.id === item.id);
        this.notificationEventSubject.next({added: [], removed: [notification]});
    }

    /**
     * Subscribes to notification events.
     */
    subscribe(listener: NotificationEventListener): Subscription {
        return this.notificationEventSubject.subscribe(listener.onNotificationEvent.bind(listener));
    }

    addAlert(errorType: string, message: string, detailMsg: string, type: string, timeout: number, groupKey: string = null) {
        let id = IDGenerator.generateId("alert");
        let alert: Alert = {type: type, msg: message, id: id, detailMsg: detailMsg, errorType: errorType, hasDetail: false};
        if (detailMsg != undefined && detailMsg != "") {
            alert.hasDetail = true;
        }
        this.alerts[id] = alert;
        /*   if(timeout){
         $timeout(function(){
         self.removeAlert(id)
         },timeout);
         }*/
        if (groupKey) {
            alert.groupKey = groupKey;
        }
        this.toastAlert(alert, timeout);
        return alert;
    }

    toastAlert(alert: Alert, timeout: number) {

        let options: { hideDelay: number | false, msg: string } = {hideDelay: false, msg: alert.msg};
        if (timeout) {
            options.hideDelay = timeout;
        }
        if (alert.hasDetail) {
            options.msg += " " + alert.detailMsg;
        }

        let alertId = alert.id;
        let toast = this.$mdToast.simple()
            .textContent(options.msg)
            .action('Ok')
            .highlightAction(true)
            .hideDelay(options.hideDelay);
        // .position(pinTo);
        this.$mdToast.show(toast).then((response) => {
            if (response == 'ok') {
                this.$mdToast.hide();
                this.removeAlert(alertId)
            }
        });

        if (timeout) {
            this.$timeout(() => {
                this.removeAlert(alertId);
            }, timeout);
        }

    }

    getAlertWithGroupKey(groupKey: string) {
        let returnedAlert: Alert = null;
        angular.forEach(this.alerts, (alert) => {
            if (returnedAlert == null && alert.groupKey && alert.groupKey == groupKey) {
                returnedAlert = alert;
            }
        });
        return returnedAlert;
    }

    success(message: string, timeout: number) {
        return this.addAlert("Success", message, undefined, "success", timeout);
    }

    error(message: string, timeout: number) {
        //  console.error("ERROR ",message)
        return this.addAlert("Error", message, undefined, "danger", timeout);
    }

    errorWithErrorType(errorType: string, message: string, timeout: number) {
        // console.error("ERROR ",message)
        return this.addAlert(errorType, message, undefined, "danger", timeout);
    }

    errorWithDetail(errorType: string, message: string, detailMsg: string, timeout: number) {
        //  console.error("ERROR ",message, detailMsg)
        return this.addAlert(errorType, message, detailMsg, "danger", timeout);
    }

    errorWithGroupKey(errorType: string, message: string, groupKey: string, detailMsg: string) {
        //   console.error("ERROR ",message, detailMsg)
        //Only add the error if it doesnt already exist
        if (groupKey != undefined) {
            if (this.getAlertWithGroupKey(groupKey) == null) {
                let alert = false;
                if (groupKey == "Connection Error") {
                    this.connectionErrors++;
                    //reset the connection error check if > 1 min
                    if ((new Date().getTime() - this.lastConnectionError) > 60000) {
                        this.connectionErrors = 0;
                    }
                    this.lastConnectionError = new Date().getTime();
                    if (this.connectionErrors > NotificationService.CONNECTION_ERRORS_ALERT_THRESHOLD) {
                        this.connectionErrors = 0;
                        alert = true;
                    }
                } else {
                    alert = true;
                }
                if (alert) {
                    return this.addAlert(errorType, message, detailMsg, "danger", undefined, groupKey);
                }
                else {
                    return {};
                }

            }
        }
        else {
            this.error(message, undefined);
        }
    }

    removeAlert(id: any) {
        delete this.alerts[id];
    }

    getAlerts() {
        return this.alerts;
    }
}

angular.module(require("services/module-name"))
    .service("NotificationService", NotificationService);
