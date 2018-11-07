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
import { moduleName } from './module-name';
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME, FEED_OVERVIEW_STATE_NAME} from "../feed-mgr/model/feed/feed-constants";

export class StateService {
    Auth: any;
    FeedManager: any;
    OpsManager: any;
    Search: any;
    Tables: any;
    Categories: any;
    static $inject = ["$state"];
    constructor(private $state: any) {

        this.Auth = this.AuthStates();
        this.FeedManager = this.FeedManagerStates;
        this.OpsManager = this.OpsManagerStates;
        this.Search = this.SearchStates;
        this.Tables = this.TableStates;
        this.Categories = this.CategoryStates;
    }
    AuthStates = () => {
        var data: any = {}
        /**
         * Navigates to the Groups page.
         */
        data.navigateToGroups = ()=> {
            this.$state.go("groups");
        };
        /**
         * Navigates to the Group Details page.
         *
         * @param {string} [opt_groupId] the system name of the group
         */
        data.navigateToGroupDetails = (opt_groupId: string)=> {
            var safeGroupId: any = angular.isString(opt_groupId) ? encodeURIComponent(opt_groupId) : null;
            this.$state.go("group-details", {groupId: safeGroupId});
        };

        /**
         * Navigates to the Users page.
         */
        data.navigateToUsers = ()=> {
            this.$state.go("users");
        };

        /**
         * Navigates to the User Details page.
         *
         * @param {string} [opt_userId] the system name of the user
         */
        data.navigateToUserDetails = (opt_userId: string)=> {
            var safeUserId: any = angular.isString(opt_userId) ? encodeURIComponent(opt_userId) : null;
            this.$state.go("user-details", {userId: safeUserId});
        };
        return data;
    }
    TemplateStates = () => {
        var data: any = {};
        data.navigateToRegisterNewTemplate = () => {
            this.$state.go('register-new-template');
        }

        data.navigateToRegisterTemplateComplete = (message: any, templateModel: any, error: any) => {
            this.$state.go('register-template-complete', {message: message, templateModel: templateModel, error: error});
        }

        data.navigateToImportTemplate = () => {
            this.$state.go('import-template');
        }

        data.navigateToRegisterNifiTemplate = () => {
            this.$state.go('register-template', {registeredTemplateId: null, nifiTemplateId: null});
        }

        data.navigateToRegisteredTemplate = (templateId: any, nifiTemplateId: any) => {
            this.$state.go('register-template', {registeredTemplateId: templateId, nifiTemplateId: nifiTemplateId});
        }

        data.navigateToTemplateInfo = (templateId: any, nifiTemplateId: any) => {
            this.$state.go('template-info', {registeredTemplateId: templateId, nifiTemplateId: nifiTemplateId});
        }
        /**
         * Navigates to the Templates page.
         */
        data.navigateToRegisteredTemplates = () => {
            this.$state.go("registered-templates");
        };
        return data;
    }

    FeedStates = () => {
        var data: any = {};
        data.navigateToFeedDefinition = (feedId:string) => {
            this.$state.go(FEED_DEFINITION_SUMMARY_STATE_NAME,{feedId:feedId, refresh:true});
        }


        data.navigateToFeedDetails = (feedId: string, tabIndex: any) => {
            if (tabIndex == null || tabIndex == undefined) {
                tabIndex = 0;
            }
            data.navigateToFeedDefinition(feedId)
        }



        data.navigateToFeedImport = () => {
            this.$state.go(FEED_DEFINITION_STATE_NAME+".import-feed");
        }

        data.navigateToEditFeedInStepper = (feedId: any) => {
            data.navigateToFeedDefinition(feedId)
          //  this.$state.go('edit-feed', {feedId: feedId});
        }

        data.navigateToDefineFeed = (templateId: any) => {
            this.$state.go(FEED_DEFINITION_STATE_NAME, {templateId: templateId});
           // this.$state.go('define-feed', {templateId: templateId});
        }

        data.navigateToNewFeed = (templateId: string) => {
            if(templateId == undefined){
                this.$state.go(FEED_DEFINITION_STATE_NAME+".select-template")
            }
            else {
                this.$state.go(FEED_DEFINITION_STATE_NAME, {templateId: templateId});
            }
        }

        /**
         * Deprecated.. redirecting to new feed screen
         * @param feedName
         */
        data.navigateToCloneFeed = (feedName: any) => {
            console.warn("You are using a deprecated state navigation.  'navigateToClonedFeed' is no longer available");
            data.navigateToNewFeed()
           // this.$state.go('define-feed', {templateId: null,bcExclude_cloning:true,bcExclude_cloneFeedName:feedName});
        }

        data.navigateToDefineFeedComplete = (feedModel: any, error: any) => {
            this.$state.go('define-feed-complete', {feedModel: feedModel, error: error});
        }

        data.navigateToFeeds = () => {
            this.$state.go('feeds');
        }

        data.navigatetoImportFeed = () => {
            this.$state.go(FEED_DEFINITION_STATE_NAME+".import-feed");
           // this.$state.go('import-feed');
        }
        return data;
    }

    ProfileStates = () => {
        var data: any = {};
        data.navigateToProfileSummary = (feedId: any) => {
            this.$state.go('feed-details.profile-summary', {feedId: feedId})
        }
        data.navigateToProfileValidResults = (feedId: any, processingdttm: any) => {
            this.$state.go('feed-details.profile-valid', {feedId: feedId, processingdttm: processingdttm})
        }
        data.navigateToProfileInvalidResults = (feedId: any, processingdttm: any) => {
            this.$state.go('feed-details.profile-invalid', {feedId: feedId, processingdttm: processingdttm})
        }
        data.navigateToProfileStats = (feedId: any, processingdttm: any) => {
            this.$state.go('feed-details.profile-stats', {feedId: feedId, processingdttm: processingdttm})
        }
        return data;

    }

    TableStates = () => {
        var data: any = {};
        data.navigateToSchemas = (datasource: any) => {
            this.$state.go('schemas', {datasource: datasource});
        };
        data.navigateToTables = (datasource: any, schema: any) => {
            this.$state.go('schemas-schema', {datasource: datasource, schema: schema});
        };
        data.navigateToTable = (datasource: any, schema: any, table: any) => {
            this.$state.go('schemas-schema-table', {datasource: datasource, schema: schema, tableName: table});
        };
        return data;
    };

    SlaStates = () => {
        var data: any = {};
        data.navigateToServiceLevelAgreements = () => {
            this.$state.go('sla');
        }
        data.navigateToServiceLevelAgreement = (slaId: any) => {
            this.$state.go('sla.edit',{slaId:slaId});
        }
        data.navigateToNewEmailTemplate = (templateId: any) => {
            this.$state.go('sla-email-template.edit',{emailTemplateId:templateId});
        }
        data.navigateToEmailTemplates = () => {
            this.$state.go('sla-email-template.list');
        }
        return data;
    }

    CategoryStates = () => {
        var data: any = {};
        data.navigateToCategoryDetails = (categoryId: any) => {
            this.$state.go('category-details', {categoryId: categoryId});
        }

        data.navigateToCategories = () => {
            this.$state.go('categories');
        }
        return data;
    }

    SearchStates = () => {
        var data: any = {};
        data.navigateToSearch = (resetPaging: any) => {
            if (angular.isUndefined(resetPaging)) {
                resetPaging = false;
            }
            this.$state.go('search', {"bcExclude_globalSearchResetPaging":resetPaging});
        }
        return data;
    }

    DatasourceStates = () => {
        return {
            navigateToDatasourceDetails: (opt_datasourceId: any) => {
                var safeDatasourceId = angular.isString(opt_datasourceId) ? encodeURIComponent(opt_datasourceId) : null;
                this.$state.go("datasource-details", {datasourceId: safeDatasourceId});
            },

            navigateToDatasources: () => {
                this.$state.go("datasources");
            }
        };
    };

    DomainTypeStates = () => {
        return {
            navigateToDomainTypeDetails: (opt_domainTypeId: any) => {
                var safeDomainTypeId : any= angular.isString(opt_domainTypeId) ? encodeURIComponent(opt_domainTypeId) : null;
                this.$state.go("domain-type-details", {domainTypeId: safeDomainTypeId});
            },

            navigateToDomainTypes: () => {
                this.$state.go("domain-types");
            }
        }
    };

    FeedManagerStates = () => {
        var data: any = {};
        data.Category = this.CategoryStates;
        data.Feed = this.FeedStates;
        data.Sla = this.SlaStates;
        data.Template = this.TemplateStates;
        data.Table = this.TableStates;
        data.Profile = this.ProfileStates;
        data.Datasource = this.DatasourceStates;
        data.DomainType = this.DomainTypeStates;
        return data;
    }

    OpsManagerJobStates = () => {
        var data: any = {};
        data.navigateToJobDetails = (executionId: any) => {
            this.$state.go('job-details', {executionId: executionId});
        }
        data.navigateToJobs = (tab: any,filter: any) => {
            this.$state.go('jobs', {tab:tab,filter: filter});
        }
        return data;
    }

    OpsManagerFeedStates = () => {
        var data: any = {};
        data.navigateToFeedDetails = (feedName: any) => {
            this.$state.go('ops-feed-details', {feedName: feedName});
        }
        data.navigateToFeedStats = (feedName: any) => {
            this.$state.go('feed-stats', {feedName: feedName});
        }
        return data;
    }

    OpsManagerServiceStates = () => {
        var data: any = {};
        data.navigateToServiceDetails = (serviceName: any) => {
            this.$state.go('service-details', {serviceName: serviceName});
        }

        data.navigateToServiceComponentDetails = (serviceName: any, componentName: any) => {
            this.$state.go('service-component-details', {serviceName: serviceName, componentName: componentName});
        }
        return data;
    }

    AlertStates = () => {
        var data: any = {};
        /**
         * Navigates to the details page for the specified alert.
         * @param {string} alertId the id of the alert
         */
        data.navigateToAlertDetails = (alertId: any) => {
            this.$state.go("alert-details", {alertId: alertId});
        };
        data.navigateToAlerts = (query: any) => {
            this.$state.go("alerts", {query: query});
        };
        return data;
    }

    SlaAssessmentStates = () => {
        var data: any = {};
        data.navigateToServiceLevelAssessments = (filter: any) => {
            filter = angular.isUndefined(filter) ? '' : filter;
            this.$state.go('service-level-assessments',{filter:filter});
        }
        data.navigateToServiceLevelAssessment = (assessmentId: any) => {
            this.$state.go('service-level-assessment',{assessmentId:assessmentId});
        }
        return data;
    }

    OpsManagerStates = () => {
        var data: any = {};
        data.dashboard = () => {
            this.$state.go('dashboard');
        }
        data.Feed = this.OpsManagerFeedStates;
        data.Job = this.OpsManagerJobStates;
        data.ServiceStatus = this.OpsManagerServiceStates
        data.Alert = this.AlertStates;
        data.Sla = this.SlaAssessmentStates;
        return data;
    }
    go = (state: any, params: any) => {
        this.$state.go(state, params);
    }
    navigateToHome = () => {
        this.$state.go("home");
    };
}
angular.module(moduleName).service('StateService', StateService);
