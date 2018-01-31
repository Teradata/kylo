define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    //const moduleName = require('../module-name');
    var ClusterController = /** @class */ (function () {
        function ClusterController($scope, $http, $mdDialog, $mdToast, $interval, AccessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$interval = $interval;
            this.AccessControlService = AccessControlService;
            this.latestSimpleMessage = {};
            this.sentMessages = [];
            this.receivedMessages = [];
            this.receivedMessageIds = [];
            this.messageCheckerInterval = null;
            this.members = [];
            this.isClustered = false;
            this.getMembers = function () {
                _this.$http.get("/proxy/v1/admin/cluster/members").then(function (response) {
                    if (response.data) {
                        _this.members = response.data;
                    }
                });
            };
            this.ngOnInit();
        }
        ClusterController.prototype.sendMessage = function () {
            var _this = this;
            var simpleMessage = this.simpleMessage;
            var successFn = function (response) {
                if (response.data && response.data.status == 'success') {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Sent the message')
                        .hideDelay(3000));
                    _this.sentMessages.push(simpleMessage);
                }
            };
            var errorFn = function (err) {
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent('Error sending the message')
                    .hideDelay(3000));
            };
            var promise = this.$http({
                url: "/proxy/v1/admin/cluster/simple",
                method: "POST",
                data: this.simpleMessage
            }).then(successFn, errorFn);
        };
        ClusterController.prototype.messageChecker = function () {
            var _this = this;
            this.$http.get("/proxy/v1/admin/cluster/simple").then(function (response) {
                if (response.data) {
                    _this.latestSimpleMessage = response.data;
                    if (response.data.type != "NULL" && _.indexOf(_this.receivedMessageIds, response.data.id) < 0) {
                        _this.receivedMessages.push(response.data);
                        _this.receivedMessageIds.push(response.data.id);
                    }
                }
            });
        };
        ClusterController.prototype.setIsClustered = function () {
            var _this = this;
            this.$http.get("/proxy/v1/admin/cluster/is-clustered").then(function (response) {
                if (response.data && response.data.status == 'success') {
                    _this.isClustered = true;
                }
                else {
                    _this.isClustered = false;
                }
            });
        };
        ClusterController.prototype.startMessageChecker = function () {
            var _this = this;
            this.messageCheckerInterval = this.$interval(function () {
                _this.messageChecker();
            }, 2000);
        };
        ClusterController.prototype.ngOnInit = function () {
            this.startMessageChecker();
            this.setIsClustered();
            this.getMembers();
        };
        return ClusterController;
    }());
    exports.ClusterController = ClusterController;
    angular.module(module_name_1.moduleName).controller("ClusterController", ["$scope", "$http", "$mdDialog", "$mdToast", "$interval", "AccessControlService", ClusterController]);
});
//# sourceMappingURL=ClusterController.js.map