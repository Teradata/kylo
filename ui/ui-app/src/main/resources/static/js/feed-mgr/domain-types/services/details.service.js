define(["require", "exports", "angular", "rxjs/Subject"], function (require, exports, angular, Subject_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Manages communication between {@link DomainTypeDetailsComponent} and its individual sections.
     */
    var DomainTypeDetailsService = /** @class */ (function () {
        function DomainTypeDetailsService() {
            /**
             * Event bus for cancel button notifications.
             */
            this.cancelSubject = new Subject_1.Subject();
            /**
             * Event bus for delete button notifications.
             */
            this.deleteSubject = new Subject_1.Subject();
            /**
             * Event bus for save failure notifications.
             */
            this.failureSubject = new Subject_1.Subject();
            /**
             * Event bus for save notifications.
             */
            this.saveSubject = new Subject_1.Subject();
        }
        Object.defineProperty(DomainTypeDetailsService.prototype, "cancelled", {
            /**
             * Observable for cancel button notifications.
             */
            get: function () {
                return this.cancelSubject.asObservable();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DomainTypeDetailsService.prototype, "deleted", {
            /**
             * Observable for delete button notifications.
             */
            get: function () {
                return this.deleteSubject.asObservable();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DomainTypeDetailsService.prototype, "failed", {
            /**
             * Observable for save failure notifications.
             */
            get: function () {
                return this.failureSubject.asObservable();
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DomainTypeDetailsService.prototype, "saved", {
            /**
             * Observable for save notifications.
             */
            get: function () {
                return this.saveSubject.asObservable();
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Called when the user cancels an edit operation.
         */
        DomainTypeDetailsService.prototype.cancelEdit = function (domainType) {
            this.cancelSubject.next(domainType);
        };
        /**
         * Called when the user deletes a domain type.
         */
        DomainTypeDetailsService.prototype.deleteDomainType = function (domainType) {
            this.deleteSubject.next(domainType);
        };
        /**
         * Called when a save operation results in a failure.
         */
        DomainTypeDetailsService.prototype.fail = function (error) {
            this.failureSubject.next(error);
        };
        /**
         * Called when the user saves a domain type.
         */
        DomainTypeDetailsService.prototype.saveEdit = function (domainType) {
            return this.saveSubject.next(domainType);
        };
        return DomainTypeDetailsService;
    }());
    exports.DomainTypeDetailsService = DomainTypeDetailsService;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .service("DomainTypeDetailsService", DomainTypeDetailsService);
});
//# sourceMappingURL=details.service.js.map