define(["angular", "admin/module-name"], function (angular, moduleName) {

    var ClusterController = function ($scope, $http, $mdDialog, $mdToast,$interval,AccessControlService) {
        var self = this;


        this.simpleMessage;

        this.latestSimpleMessage = {}
        this.sentMessages = [];
        this.receivedMessages = [];
        this.receivedMessageIds = [];
        self.messageCheckerInterval = null;

        self.members = [];
        self.isClustered = false;


        this.sendMessage = function(){
            var simpleMessage = self.simpleMessage;
            var successFn = function (response) {
                if (response.data && response.data.status == 'success') {
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Sent the message')
                            .hideDelay(3000)
                    );
                    self.sentMessages.push(simpleMessage);
                }
            }
            var errorFn = function (err) {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Error sending the message')
                        .hideDelay(3000)
                );
            }

            var promise = $http({
                url: "/proxy/v1/admin/cluster/simple",
                method: "POST",
                data: self.simpleMessage
            }).then(successFn, errorFn);
        }

        function messageChecker() {
            $http.get("/proxy/v1/admin/cluster/simple").then(function(response){
                if(response.data){
                    self.latestSimpleMessage = response.data;
                    if(response.data.type != "NULL" && _.indexOf(self.receivedMessageIds,response.data.id) < 0){
                        self.receivedMessages.push(response.data);
                        self.receivedMessageIds.push(response.data.id);
                    }
                }
            });
        }

        self.getMembers = function() {
            $http.get("/proxy/v1/admin/cluster/members").then(function(response){
                if(response.data){
                   self.members = response.data;
                }
            });
        }

        function isClustered() {
            $http.get("/proxy/v1/admin/cluster/is-clustered").then(function(response){
                if(response.data && response.data.status == 'success'){
                    self.isClustered = true;
                }
                else {
                    self.isClustered = false;
                }
            });
        }

        function startMessageChecker(){
         self.messageCheckerInterval =   $interval(function(){
                messageChecker();
            },2000);
        }

        $scope.$on('$destroy', function () {
            if(self.messageCheckerInterval != null) {
                $interval.cancel(self.messageCheckerInterval);
            }
        });

        function init() {
            startMessageChecker();
            isClustered();
            self.getMembers();
        }

        init();


    };

    angular.module(moduleName).controller("ClusterController", ["$scope", "$http","$mdDialog", "$mdToast","$interval","AccessControlService",ClusterController]);
});
