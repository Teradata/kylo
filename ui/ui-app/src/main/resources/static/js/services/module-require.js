define(['angular',
        'services/module-name',
        'jquery',
        'services/AccessControlService',
        'services/StateService',
        'services/ElasticSearchService',
        'services/broadcast-service',
        'services/CommonRestUrlService',
        'services/PaginationDataService',
        'services/SideNavService',
        'services/TableOptionsService',
        'services/ConfigurationService',
        'services/AddButtonService',
        'services/FileUploadService',
        'services/WindowUnloadService',
        'services/Utils',
        'services/HttpService',
        'services/NotificationService',
'services/UserGroupService'], function (angular,moduleName) {
   return angular.module(moduleName);
});

//'services/AngularHttpInterceptor'

