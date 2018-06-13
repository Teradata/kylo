/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
 /**
 * ui-router service.  Controllers that link/navigate to other controllers/pages use this service.
 * See the corresponding name references in app.js
 */
import * as angular from 'angular';
import {moduleName} from './module-name';

import "./module"; // ensure module is loaded first

export default class StateService{
    Auth: any;
    FeedManager: any;
    OpsManager: any;
    Search: any;
    Tables: any;
    Categories: any;
    static $inject=["$state"];
    constructor (private $state: any){
        var AuthStates = function(){
            var data: any = {}
            /**
             * Navigates to the Groups page.
             */
            data.navigateToGroups = ()=> {
                $state.go("groups");
            };
            /**
             * Navigates to the Group Details page.
             *
             * @param {string} [opt_groupId] the system name of the group
             */
            data.navigateToGroupDetails = (opt_groupId: string)=> {
                var safeGroupId: any = angular.isString(opt_groupId) ? encodeURIComponent(opt_groupId) : null;
                $state.go("group-details", {groupId: safeGroupId});
            };

            /**
             * Navigates to the Users page.
             */
            data.navigateToUsers = ()=> {
                $state.go("users");
            };

            /**
             * Navigates to the User Details page.
             *
             * @param {string} [opt_userId] the system name of the user
             */
            data.navigateToUserDetails = (opt_userId: string)=> {
                var safeUserId: any = angular.isString(opt_userId) ? encodeURIComponent(opt_userId) : null;
                $state.go("user-details", {userId: safeUserId});
            };
            return data;
        }
        var TemplateStates = function () {
            var data: any = {};
            data.navigateToRegisterNewTemplate = function () {
                $state.go('register-new-template');
            }

            data.navigateToRegisterTemplateComplete = function (message: any, templateModel: any, error: any) {
                $state.go('register-template-complete', {message: message, templateModel: templateModel, error: error});
            }

            data.navigateToImportTemplate = function () {
                $state.go('import-template');
            }

            data.navigateToRegisterNifiTemplate = function () {
                $state.go('register-template', {registeredTemplateId: null, nifiTemplateId: null});
            }

            data.navigateToRegisteredTemplate = function (templateId: any, nifiTemplateId: any) {
                $state.go('register-template', {registeredTemplateId: templateId, nifiTemplateId: nifiTemplateId});
            }
            /**
             * Navigates to the Templates page.
             */
            data.navigateToRegisteredTemplates = function () {
                $state.go("registered-templates");
            };
            return data;
        }

        var FeedStates = function () {
            var data: any = {};
            data.navigateToFeedDetails = function (feedId: any, tabIndex: any) {
                if (tabIndex == null || tabIndex == undefined) {
                    tabIndex = 0;
                }
                $state.go('feed-details', {feedId: feedId, tabIndex: tabIndex});
            }

            data.navigateToEditFeedInStepper = function (feedId: any) {
                $state.go('edit-feed', {feedId: feedId});
            }

            data.navigateToDefineFeed = function (templateId: any) {
                $state.go('define-feed', {templateId: templateId});
            }

            data.navigateToCloneFeed = function (feedName: any) {
                $state.go('define-feed', {templateId: null,bcExclude_cloning:true,bcExclude_cloneFeedName:feedName});
            }

            data.navigateToDefineFeedComplete = function (feedModel: any, error: any) {
                $state.go('define-feed-complete', {feedModel: feedModel, error: error});
            }

            data.navigateToFeeds = function () {
                $state.go('feeds');
            }

            data.navigatetoImportFeed = function () {
                $state.go('import-feed');
            }
            return data;
        }

        var ProfileStates = function () {
            var data: any = {};
            data.navigateToProfileSummary = function (feedId: any) {
                $state.go('feed-details.profile-summary', {feedId: feedId})
            }
            data.navigateToProfileValidResults = function (feedId: any, processingdttm: any) {
                $state.go('feed-details.profile-valid', {feedId: feedId, processingdttm: processingdttm})
            }
            data.navigateToProfileInvalidResults = function (feedId: any, processingdttm: any) {
                $state.go('feed-details.profile-invalid', {feedId: feedId, processingdttm: processingdttm})
            }
            data.navigateToProfileStats = function (feedId: any, processingdttm: any) {
                $state.go('feed-details.profile-stats', {feedId: feedId, processingdttm: processingdttm})
            }
            return data;

        }

        var TableStates = function () {
            var data: any = {};
            data.navigateToSchemas = function (datasource: any) {
                $state.go('schemas', {datasource: datasource});
            };
            data.navigateToTables = function (datasource: any, schema: any) {
                $state.go('schemas-schema', {datasource: datasource, schema: schema});
            };
            data.navigateToTable = function (datasource: any, schema: any, table: any) {
                $state.go('schemas-schema-table', {datasource: datasource, schema: schema, tableName: table});
            };
            return data;
        };

        var SlaStates = function () {
            var data: any = {};
            data.navigateToServiceLevelAgreements = function () {
                $state.go('service-level-agreements');
            }
            data.navigateToServiceLevelAgreement = function (slaId: any) {
                $state.go('service-level-agreements',{slaId:slaId});
            }
            data.navigateToNewEmailTemplate = function (templateId: any) {
                $state.go('sla-email-template',{emailTemplateId:templateId});
            }
            data.navigateToEmailTemplates = function () {
                $state.go('sla-email-templates');
            }
            return data;
        }

        var CategoryStates = function () {
            var data: any = {};
            data.navigateToCategoryDetails = function (categoryId: any) {
                $state.go('category-details', {categoryId: categoryId});
            }

            data.navigateToCategories = function () {
                $state.go('categories');
            }
            return data;
        }

        var SearchStates = function () {
            var data: any = {};
            data.navigateToSearch = function (resetPaging: any) {
                if (angular.isUndefined(resetPaging)) {
                    resetPaging = false;
                }
                $state.go('search', {"bcExclude_globalSearchResetPaging":resetPaging});
            }
            return data;
        }

        var DatasourceStates = function() {
            return {
                navigateToDatasourceDetails: function(opt_datasourceId: any) {
                    var safeDatasourceId = angular.isString(opt_datasourceId) ? encodeURIComponent(opt_datasourceId) : null;
                    $state.go("datasource-details", {datasourceId: safeDatasourceId});
                },

                navigateToDatasources: function() {
                    $state.go("datasources");
                }
            };
        };

        var DomainTypeStates = function () {
            return {
                navigateToDomainTypeDetails: function (opt_domainTypeId: any) {
                    var safeDomainTypeId : any= angular.isString(opt_domainTypeId) ? encodeURIComponent(opt_domainTypeId) : null;
                    $state.go("domain-type-details", {domainTypeId: safeDomainTypeId});
                },

                navigateToDomainTypes: function () {
                    $state.go("domain-types");
                }
            }
        };

        var FeedManagerStates = function () {
            var data: any = {};
            data.Category = CategoryStates;
            data.Feed = FeedStates;
            data.Sla = SlaStates;
            data.Template = TemplateStates;
            data.Table = TableStates;
            data.Profile = ProfileStates;
            data.Datasource = DatasourceStates;
            data.DomainType = DomainTypeStates;
            return data;
        }

        var OpsManagerJobStates = function () {
            var data: any = {};
            data.navigateToJobDetails = function (executionId: any) {
                $state.go('job-details', {executionId: executionId});
            }
            data.navigateToJobs = function (tab: any,filter: any) {
                $state.go('jobs', {tab:tab,filter: filter});
            }
            return data;
        }

        var OpsManagerFeedStates = function () {
            var data: any = {};
            data.navigateToFeedDetails = function (feedName: any) {
                $state.go('ops-feed-details', {feedName: feedName});
            }
            data.navigateToFeedStats = function (feedName: any) {
                $state.go('feed-stats', {feedName: feedName});
            }
            return data;
        }

        var OpsManagerServiceStates = function () {
            var data: any = {};
            data.navigateToServiceDetails = function (serviceName: any) {
                $state.go('service-details', {serviceName: serviceName});
            }

            data.navigateToServiceComponentDetails = function (serviceName: any, componentName: any) {
                $state.go('service-component-details', {serviceName: serviceName, componentName: componentName});
            }
            return data;
        }

        var AlertStates = function () {
            var data: any = {};
            /**
             * Navigates to the details page for the specified alert.
             * @param {string} alertId the id of the alert
             */
            data.navigateToAlertDetails = function (alertId: any) {
                $state.go("alert-details", {alertId: alertId});
            };
            data.navigateToAlerts = function (query: any) {
                $state.go("alerts", {query: query});
            };
            return data;
        }

        var SlaAssessmentStates = function () {
            var data: any = {};
            data.navigateToServiceLevelAssessments = function (filter: any) {
                filter = angular.isUndefined(filter) ? '' : filter;
                $state.go('service-level-assessments',{filter:filter});
            }
            data.navigateToServiceLevelAssessment = function (assessmentId: any) {
                $state.go('service-level-assessment',{assessmentId:assessmentId});
            }
            return data;
        }

        var OpsManagerStates = function () {
            var data: any = {};
            data.dashboard = function () {
                $state.go('dashboard');
            }
            data.Feed = OpsManagerFeedStates;
            data.Job = OpsManagerJobStates;
            data.ServiceStatus = OpsManagerServiceStates
            data.Alert = AlertStates;
            data.Sla = SlaAssessmentStates;
            return data;
        }

        var States: any = ()=> {
            var data: any = {};
            data.Auth = AuthStates;
           // this.Auth = AuthStates;
            data.Search = SearchStates;
            data.FeedManager = FeedManagerStates;
            data.OpsManager = OpsManagerStates;
            data.Tables = TableStates;
            data.Categories = CategoryStates;
            data.go = function (state: any, params: any) {
                $state.go(state, params);
            }
            /**
             * Navigates to the Home page.
             */
            data.navigateToHome = function () {
                $state.go("home");
            };
            return data;
        }
        this.Auth = AuthStates;
        this.FeedManager = FeedManagerStates;
        this.OpsManager = OpsManagerStates;
        this.Search = SearchStates;
        this.Tables = TableStates;
        this.Categories = CategoryStates;
        return new States;
} 
}
angular.module(moduleName).service('StateService',StateService);