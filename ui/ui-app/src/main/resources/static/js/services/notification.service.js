define(["require", "exports", "angular", "rxjs/Subject", "rxjs/add/operator/auditTime", "./module"], function (require, exports, angular, Subject_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * A notification about an event or task in Kylo.
     */
    var KyloNotification = /** @class */ (function () {
        /**
         * Constructs a {@code KyloNotification} with the specified unique identifier and subject for property changes.
         */
        function KyloNotification(id, subject) {
            this.createTime_ = new Date();
            this.subject = new Subject_1.Subject();
            this.id_ = id;
            this.subject = subject;
        }
        Object.defineProperty(KyloNotification.prototype, "id", {
            /**
             * A unique identifier for this notification.
             */
            get: function () {
                return this.id_;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(KyloNotification.prototype, "callback", {
            /**
             * Called when the notification is clicked.
             */
            get: function () {
                return this.callback_;
            },
            set: function (value) {
                this.callback_ = value;
                this.trigger();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(KyloNotification.prototype, "icon", {
            /**
             * Icon name.
             */
            get: function () {
                return this.icon_;
            },
            set: function (value) {
                this.icon_ = value;
                this.trigger();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(KyloNotification.prototype, "loading", {
            /**
             * Indicates a progress spinner should be displayed for this notification.
             */
            get: function () {
                return this.loading_;
            },
            set: function (value) {
                this.loading_ = value;
                this.trigger();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(KyloNotification.prototype, "message", {
            /**
             * Title.
             */
            get: function () {
                return this.message_;
            },
            set: function (value) {
                this.message_ = value;
                this.trigger();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(KyloNotification.prototype, "createTime", {
            /**
             * Time when the notification was created.
             */
            get: function () {
                return this.createTime_;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Triggers a property change event.
         */
        KyloNotification.prototype.trigger = function () {
            this.subject.next(this);
        };
        return KyloNotification;
    }());
    exports.KyloNotification = KyloNotification;
    var NotificationService = /** @class */ (function () {
        function NotificationService($timeout, $mdToast) {
            var _this = this;
            this.$timeout = $timeout;
            this.$mdToast = $mdToast;
            this.alerts = {};
            this.connectionErrors = 0;
            /**
             * List of notifications.
             */
            this.kyloNotifications = [];
            /**
             * Subject for notification property changes.
             */
            this.kyloNotificationSubject = new Subject_1.Subject();
            /**
             * Callbacks for notification events.
             */
            this.notificationEventSubject = new Subject_1.Subject();
            this.lastConnectionError = 0;
            // Listen for changes to KyloNotifications and create NotificationEvents
            this.kyloNotificationSubject
                .auditTime(10)
                .subscribe(function () { return _this.notificationEventSubject.next({ added: [], removed: [] }); });
        }
        Object.defineProperty(NotificationService.prototype, "notifications", {
            /**
             * Gets a copy of all notifications.
             */
            get: function () {
                return this.kyloNotifications.slice();
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Creates a new notification.
         *
         * @param message - title
         * @param icon - icon name
         * @returns the notification
         */
        NotificationService.prototype.addNotification = function (message, icon) {
            var notification = new KyloNotification(IDGenerator.generateId("notification"), this.kyloNotificationSubject);
            notification.message = message;
            notification.icon = icon;
            this.kyloNotifications.push(notification);
            this.notificationEventSubject.next({ added: [notification], removed: [] });
            return notification;
        };
        /**
         * Removes the specified notification.
         */
        NotificationService.prototype.removeNotification = function (notification) {
            this.kyloNotifications = this.notifications.filter(function (item) { return notification.id === item.id; });
            this.notificationEventSubject.next({ added: [], removed: [notification] });
        };
        /**
         * Subscribes to notification events.
         */
        NotificationService.prototype.subscribe = function (listener) {
            return this.notificationEventSubject.subscribe(listener.onNotificationEvent.bind(listener));
        };
        NotificationService.prototype.addAlert = function (errorType, message, detailMsg, type, timeout, groupKey) {
            if (groupKey === void 0) { groupKey = null; }
            var id = IDGenerator.generateId("alert");
            var alert = { type: type, msg: message, id: id, detailMsg: detailMsg, errorType: errorType, hasDetail: false };
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
        };
        NotificationService.prototype.toastAlert = function (alert, timeout) {
            var _this = this;
            var options = { hideDelay: false, msg: alert.msg };
            if (timeout) {
                options.hideDelay = timeout;
            }
            if (alert.hasDetail) {
                options.msg += " " + alert.detailMsg;
            }
            var alertId = alert.id;
            var toast = this.$mdToast.simple()
                .textContent(options.msg)
                .action('Ok')
                .highlightAction(true)
                .hideDelay(options.hideDelay);
            // .position(pinTo);
            this.$mdToast.show(toast).then(function (response) {
                if (response == 'ok') {
                    _this.$mdToast.hide();
                    _this.removeAlert(alertId);
                }
            });
            if (timeout) {
                this.$timeout(function () {
                    _this.removeAlert(alertId);
                }, timeout);
            }
        };
        NotificationService.prototype.getAlertWithGroupKey = function (groupKey) {
            var returnedAlert = null;
            angular.forEach(this.alerts, function (alert) {
                if (returnedAlert == null && alert.groupKey && alert.groupKey == groupKey) {
                    returnedAlert = alert;
                }
            });
            return returnedAlert;
        };
        NotificationService.prototype.success = function (message, timeout) {
            return this.addAlert("Success", message, undefined, "success", timeout);
        };
        NotificationService.prototype.error = function (message, timeout) {
            //  console.error("ERROR ",message)
            return this.addAlert("Error", message, undefined, "danger", timeout);
        };
        NotificationService.prototype.errorWithErrorType = function (errorType, message, timeout) {
            // console.error("ERROR ",message)
            return this.addAlert(errorType, message, undefined, "danger", timeout);
        };
        NotificationService.prototype.errorWithDetail = function (errorType, message, detailMsg, timeout) {
            //  console.error("ERROR ",message, detailMsg)
            return this.addAlert(errorType, message, detailMsg, "danger", timeout);
        };
        NotificationService.prototype.errorWithGroupKey = function (errorType, message, groupKey, detailMsg) {
            //   console.error("ERROR ",message, detailMsg)
            //Only add the error if it doesnt already exist
            if (groupKey != undefined) {
                if (this.getAlertWithGroupKey(groupKey) == null) {
                    var alert_1 = false;
                    if (groupKey == "Connection Error") {
                        this.connectionErrors++;
                        //reset the connection error check if > 1 min
                        if ((new Date().getTime() - this.lastConnectionError) > 60000) {
                            this.connectionErrors = 0;
                        }
                        this.lastConnectionError = new Date().getTime();
                        if (this.connectionErrors > NotificationService.CONNECTION_ERRORS_ALERT_THRESHOLD) {
                            this.connectionErrors = 0;
                            alert_1 = true;
                        }
                    }
                    else {
                        alert_1 = true;
                    }
                    if (alert_1) {
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
        };
        NotificationService.prototype.removeAlert = function (id) {
            delete this.alerts[id];
        };
        NotificationService.prototype.getAlerts = function () {
            return this.alerts;
        };
        NotificationService.CONNECTION_ERRORS_ALERT_THRESHOLD = 5;
        NotificationService.$inject = ["$timeout", "$mdToast"];
        return NotificationService;
    }());
    exports.NotificationService = NotificationService;
    angular.module(require("services/module-name"))
        .service("NotificationService", NotificationService);
});
//# sourceMappingURL=notification.service.js.map