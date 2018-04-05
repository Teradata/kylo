import * as angular from 'angular';
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import AccessControlService from "../../services/AccessControlService";

export class ClusterController implements ng.IComponentController{
        simpleMessage: any;
        latestSimpleMessage: any = {}
        sentMessages: any[] = [];
        receivedMessages: any[] = [];
        receivedMessageIds: any[] = [];
        messageCheckerInterval: any = null;

        members: any[] = [];
        isClustered: boolean = false;
        static readonly $inject = ["$scope", "$http","$mdDialog", "$mdToast","$interval","AccessControlService"];
        constructor(private $scope: angular.IScope,
                    private $http: angular.IHttpService, 
                    private $mdDialog: angular.material.IDialogService,
                    private $mdToast: angular.material.IToastService,
                    private $interval: angular.IIntervalService,
                    private AccessControlService:AccessControlService)
                    {
                        this.ngOnInit();
                    }

       sendMessage(){
            var simpleMessage = this.simpleMessage;
            var successFn = (response: any)=> {
                if (response.data && response.data.status == 'success') {
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Sent the message')
                            .hideDelay(3000)
                    );
                    this.sentMessages.push(simpleMessage);
                }
            }
            var errorFn= (err: any)=> {
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Error sending the message')
                        .hideDelay(3000)
                );
            }
               var promise = this.$http({
                url: "/proxy/v1/admin/cluster/simple",
                method: "POST",
                data: this.simpleMessage
            }).then(successFn, errorFn);
       }

       messageChecker() {
            this.$http.get("/proxy/v1/admin/cluster/simple").then((response: any)=>{
                if(response.data){
                    this.latestSimpleMessage = response.data;
                    if(response.data.type != "NULL" && _.indexOf(this.receivedMessageIds,response.data.id) < 0){
                        this.receivedMessages.push(response.data);
                        this.receivedMessageIds.push(response.data.id);
                    }
                }
            });
        }

        getMembers = ()=> {
            this.$http.get("/proxy/v1/admin/cluster/members").then((response: any)=>{
                if(response.data){
                   this.members = response.data;
                }
            });
        }


         setIsClustered() {// function isClustered() {
            this.$http.get("/proxy/v1/admin/cluster/is-clustered").then((response: any)=>{
                if(response.data && response.data.status == 'success'){
                    this.isClustered = true;
                }
                else {
                    this.isClustered = false;
                }
            });
        }

        startMessageChecker(){
         this.messageCheckerInterval =  this.$interval(()=>{
                this.messageChecker();
            },2000);
        }

        ngOnInit(){
                this.startMessageChecker();
                this.setIsClustered();
                this.getMembers();
            }
}

  angular.module(moduleName).component("clusterController", {
        controller: ClusterController,
        controllerAs: "vm",
        templateUrl: "js/admin/cluster/cluster-test.html"
    });
  //...controller("ClusterController", [ClusterController]);
