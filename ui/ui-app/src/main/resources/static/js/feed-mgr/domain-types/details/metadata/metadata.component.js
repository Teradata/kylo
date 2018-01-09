var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "angular", "../abstract-section.component"], function (require, exports, angular, abstract_section_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Metadata section of the {@link DomainTypeDetailsComponent}.
     */
    var DomainTypeMetadataDetailsComponent = /** @class */ (function (_super) {
        __extends(DomainTypeMetadataDetailsComponent, _super);
        function DomainTypeMetadataDetailsComponent($mdDialog, DomainTypeDetailsService, FeedTagService) {
            var _this = _super.call(this, DomainTypeDetailsService) || this;
            _this.$mdDialog = $mdDialog;
            _this.FeedTagService = FeedTagService;
            return _this;
        }
        Object.defineProperty(DomainTypeMetadataDetailsComponent.prototype, "canDelete", {
            /**
             * Indicates if the domain type can be deleted. The main requirement is that the domain type exists.
             */
            get: function () {
                return !this.isNew;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Saves changes to the model.
         */
        DomainTypeMetadataDetailsComponent.prototype.onSave = function () {
            this.onUpdate({
                title: this.editModel.title,
                description: this.editModel.description,
                icon: this.editModel.icon,
                iconColor: this.editModel.iconColor
            });
        };
        /**
         * Shows the icon picker dialog.
         */
        DomainTypeMetadataDetailsComponent.prototype.showIconPicker = function () {
            var _this = this;
            this.$mdDialog.show({
                controller: "IconPickerDialog",
                templateUrl: "js/common/icon-picker-dialog/icon-picker-dialog.html",
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: this.editModel
                }
            }).then(function (msg) {
                if (msg) {
                    _this.editModel.icon = msg.icon;
                    _this.editModel.iconColor = msg.color;
                }
            });
        };
        DomainTypeMetadataDetailsComponent.$inject = ["$mdDialog", "DomainTypeDetailsService", "FeedTagService"];
        return DomainTypeMetadataDetailsComponent;
    }(abstract_section_component_1.AbstractSectionComponent));
    exports.DomainTypeMetadataDetailsComponent = DomainTypeMetadataDetailsComponent;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .component("domainTypeMetadataDetails", {
        bindings: {
            allowEdit: "<",
            model: "<"
        },
        controller: DomainTypeMetadataDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/metadata/metadata.component.html"
    });
});
//# sourceMappingURL=metadata.component.js.map