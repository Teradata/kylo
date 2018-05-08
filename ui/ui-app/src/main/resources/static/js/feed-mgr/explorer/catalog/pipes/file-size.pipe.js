var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/core"], function (require, exports, core_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var FileSizePipe = /** @class */ (function () {
        function FileSizePipe() {
        }
        FileSizePipe.prototype.transform = function (value) {
            if (value < 1000) {
                return value + " B";
            }
            else if (value < 1000000) {
                return (value / 1000).toFixed(2) + " KB";
            }
            else if (value < 1000000000) {
                return (value / 1000000).toFixed(2) + " MB";
            }
        };
        FileSizePipe = __decorate([
            core_1.Pipe({ name: "fileSize" })
        ], FileSizePipe);
        return FileSizePipe;
    }());
    exports.FileSizePipe = FileSizePipe;
});
//# sourceMappingURL=file-size.pipe.js.map