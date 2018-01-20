define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Type formats for the date format dialog.
     */
    var DateFormatType;
    (function (DateFormatType) {
        /**
         * String input / output type.
         */
        DateFormatType["STRING"] = "STRING";
        /**
         * Timestamp input / output type.
         */
        DateFormatType["TIMESTAMP"] = "TIMESTAMP";
    })(DateFormatType = exports.DateFormatType || (exports.DateFormatType = {}));
    /**
     * Timestamp unit for the date format dialog.
     */
    var DateFormatUnit;
    (function (DateFormatUnit) {
        /**
         * Number of seconds since 1970-01-01.
         */
        DateFormatUnit["SECONDS"] = "SECONDS";
        /**
         * Number of milliseconds since 1970-01-01.
         */
        DateFormatUnit["MILLISECONDS"] = "MILLISECONDS";
    })(DateFormatUnit = exports.DateFormatUnit || (exports.DateFormatUnit = {}));
});
//# sourceMappingURL=dialog.service.js.map