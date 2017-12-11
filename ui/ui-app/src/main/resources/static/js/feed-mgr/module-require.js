/**
 * Include all common services/directives are are used for the kylo.feedmgr module
 */
define(['angular',
        'feed-mgr/services/VisualQueryService',
        'feed-mgr/services/RestUrlService',
        'feed-mgr/services/FeedCreationErrorService',
        'feed-mgr/services/FeedService',
        'feed-mgr/services/RegisterTemplateService',
        'feed-mgr/services/CategoriesService',
        'feed-mgr/services/FeedSecurityGroupsService',
        'feed-mgr/services/FeedInputProcessorPropertiesTemplateFactory',
        'feed-mgr/services/FeedDetailsProcessorRenderingHelper',
        'feed-mgr/services/ImportService',
        'feed-mgr/services/SlaService',
        'feed-mgr/services/FeedPropertyService',
        'feed-mgr/shared/policy-input-form/policy-input-form',
        'feed-mgr/shared/policy-input-form/PolicyInputFormService',
        'feed-mgr/services/HiveService',
        'feed-mgr/shared/hql-editor/hql-editor',
        'feed-mgr/services/DBCPTableSchemaService',
        'feed-mgr/services/EditFeedNifiPropertiesService',
        'feed-mgr/services/FeedTagService',
        'feed-mgr/services/FattableService',
        'feed-mgr/shared/properties-admin/properties-admin',
        'feed-mgr/shared/property-list/property-list',
        'feed-mgr/shared/feed-field-policy-rules/FeedFieldPolicyRuleDialog',
        'feed-mgr/shared/feed-field-policy-rules/inline-field-policy-form',
        'feed-mgr/shared/nifi-property-input/nifi-property-timunit-input',
        'feed-mgr/shared/nifi-property-input/nifi-property-input',
        'feed-mgr/shared/cron-expression-validator/cron-expression-validator',
        'feed-mgr/shared/cron-expression-preview/cron-expression-preview',
        'feed-mgr/services/DatasourcesService',
        'feed-mgr/shared/entity-access-control/entity-access',
        'feed-mgr/shared/entity-access-control/EntityAccessControlService',
        'feed-mgr/shared/profile-stats/ProfileStats',
        'feed-mgr/services/UiComponentsService',
        'feed-mgr/services/DomainTypesService',
        'feed-mgr/shared/apply-domain-type/ApplyDomainTypeDialog'],function() {

});



