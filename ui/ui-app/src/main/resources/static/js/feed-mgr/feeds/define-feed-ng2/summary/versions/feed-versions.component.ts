import {Component, Inject, OnInit} from "@angular/core";
import {VERSIONS_LINK} from "../../model/feed-link-constants";
import {AbstractLoadFeedComponent} from "../../shared/AbstractLoadFeedComponent";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {DefineFeedService} from "../../services/define-feed.service";
import {StateService} from "@uirouter/angular";
import {KyloIcons} from "../../../../../kylo-utils/kylo-icons";
import {FeedService} from "../../../../services/FeedService";
import {fromPromise} from "rxjs/observable/fromPromise";
import * as _ from "underscore";
import {forkJoin} from "rxjs/observable/forkJoin";
import {CloneUtil} from "../../../../../common/utils/clone-util";
import {HttpClient} from "@angular/common/http";
import {RestUrlService} from "../../../../services/RestUrlService";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {Feed} from "../../../../model/feed/feed.model";
import {FeedNifiPropertiesService} from "../../services/feed-nifi-properties.service";
import {DomainType, DomainTypesService} from "../../../../services/DomainTypesService";
import {FeedFieldPolicyRuleService} from "../../../../shared/feed-field-policy-rules/FeedFieldPolicyRuleDialog";

@Component({
    selector: "feed-versions",
    styleUrls: ["./feed-versions.component.scss"],
    templateUrl: "./feed-versions.component.html"
})

export class FeedVersionsComponent extends AbstractLoadFeedComponent implements OnInit {

    static LINK_NAME = VERSIONS_LINK;
    public kyloIcons_Links_versions= KyloIcons.Links.versions;
    historyVersions: any;
    feedService: FeedService;
    restUrlService: RestUrlService;
    feedNifiPropertiesService: FeedNifiPropertiesService;
    domainTypesService: DomainTypesService;
    feedFieldPolicyRuleService: FeedFieldPolicyRuleService;
    http: HttpClient;
    leftVersion: any;
    rightVersion: any;
    loading: any;
    leftFeed: any;
    rightFeed: any;
    userProperties: any[];
    isClustered: boolean = true;
    supportsExecutionNode: boolean = true;
    toolTipPosition: string = 'left';
    securityGroupsEnabled: boolean = false;
    fieldNameMap: any;
    propertiesMap: any[];

    availableDomainTypes: Array<DomainType> = [];

    constructor(http: HttpClient,
                feedLoadingService: FeedLoadingService,
                stateService: StateService,
                defineFeedService: DefineFeedService,
                feedSideNavService: FeedSideNavService,
                feedNifiPropertiesService: FeedNifiPropertiesService,
                @Inject("FeedService") feedService: FeedService,
                @Inject("RestUrlService") restUrlService: RestUrlService,
                domainTypesService: DomainTypesService,
                @Inject("FeedFieldPolicyRuleService") feedFieldPolicyRuleService: FeedFieldPolicyRuleService) {
        super(feedLoadingService, stateService, defineFeedService, feedSideNavService);
        this.feedService = feedService;
        this.restUrlService = restUrlService;
        this.http = http;
        this.feedNifiPropertiesService = feedNifiPropertiesService;
        this.domainTypesService = domainTypesService;
        this.feedFieldPolicyRuleService = feedFieldPolicyRuleService;
        this.loading = false;
    }

    getLinkName() {
        return FeedVersionsComponent.LINK_NAME;
    }

    init() {
        //console.log(this.feed);
        this.loadHistoryVersions();
        this.populateFieldNameMap();
    }

    loadHistoryVersions() {
        return fromPromise(this.feedService.getFeedVersions(this.feed.id))
            .subscribe((result: any) => {
                this.historyVersions = result.versions;
                this.leftVersion = this.getCurrentVersion();
                //console.log('Total history versions loaded: ' + this.historyVersions.length);
            }, (err: any) => {
                console.log("Error retrieving history versions of feed");
            });
    }

    getCurrentVersion(): any {
        return this.historyVersions[0];
    }

    changeRightVersion(version: any) {
        this.rightVersion = version;
        this.loading = true;
        this.propertiesMap = [];

        let diffObservable = fromPromise(this.feedService.diffFeedVersions(this.feed.id, this.rightVersion.id, this.leftVersion.id));
        let nifiClusterStatusObservable = this.http.get(this.restUrlService.NIFI_STATUS);
        let securityGroupsEnabledObservable = this.http.get(this.restUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled");
        let domainTypesObservable = fromPromise(this.domainTypesService.findAll());

        forkJoin([diffObservable, nifiClusterStatusObservable, securityGroupsEnabledObservable, domainTypesObservable])
            .subscribe(([diffResult, nifiClusterStatusResult, securityGroupsEnabledResult, domainTypesResult]) => {

                this.availableDomainTypes = domainTypesResult;
                domainTypesResult.forEach((domainType: any) => {
                    if (domainType && domainType.field) {
                        domainType.field.derivedDataType = null;
                        domainType.field.precisionScale = null;
                    }
                });

                if (!_.isUndefined(securityGroupsEnabledResult)
                    && !_.isUndefined(securityGroupsEnabledResult.data)
                    && !_.isUndefined(securityGroupsEnabledResult.data[0])) {
                    this.securityGroupsEnabled = securityGroupsEnabledResult.data[0].enabled;
                }

                this.isClustered = !_.isUndefined(nifiClusterStatusResult.clustered) && nifiClusterStatusResult.clustered;
                this.supportsExecutionNode = this.isClustered && !_.isUndefined(nifiClusterStatusResult.version) && !nifiClusterStatusResult.version.match(/^0\.|^1\.0/);

                this.feedService.versionFeedModelDiff = [];
                _.each(diffResult.difference.patch, (patch: any) => {
                    this.feedService.versionFeedModelDiff[patch.path] = patch;
                });

                this.leftFeed = diffResult.fromVersion.entity;
                this.rightFeed = diffResult.toVersion.entity;
                this.populatePropertiesMap();

                this.feedService.versionFeedModel = this.rightFeed;
                this.feedService.versionFeedModel.version = this.rightVersion;

                this.userProperties = [];
                _.each(this.feedService.versionFeedModel.userProperties, (versionedProp) => {
                    let property: any = {};
                    property.versioned = CloneUtil.deepObjectCopy(versionedProp);
                    property.op = 'no-op';
                    property.systemName = property.versioned.systemName;
                    property.displayName = property.versioned.displayName;
                    property.description = property.versioned.description;
                    property.current = CloneUtil.deepObjectCopy(property.versioned);
                    this.userProperties.push(property);
                });

                _.each(_.values(this.feedService.versionFeedModelDiff), (diff) => {
                    if (diff.path.startsWith("/userProperties")) {
                        if (diff.path.startsWith("/userProperties/")) {
                            //individual versioned indexed action
                            let remainder = diff.path.substring("/userProperties/".length, diff.path.length);
                            let indexOfSlash = remainder.indexOf("/");
                            let versionedPropIdx = remainder.substring(0, indexOfSlash > 0 ? indexOfSlash : remainder.length);
                            if ("replace" === diff.op) {
                                let property = this.userProperties[versionedPropIdx];
                                property.op = diff.op;
                                let replacedPropertyName = remainder.substring(remainder.indexOf("/") + 1, remainder.length);
                                property.current[replacedPropertyName] = diff.value;
                                property[replacedPropertyName] = diff.value;
                            } else if ("add" === diff.op) {
                                if (_.isArray(diff.value)) {
                                    _.each(diff.value, (prop) => {
                                        this.userProperties.push(this.createProperty(prop, diff.op));
                                    });
                                } else {
                                    this.userProperties.unshift(this.createProperty(diff.value, diff.op));
                                }
                            } else if ("remove" === diff.op) {
                                let property = this.userProperties[versionedPropIdx];
                                property.op = diff.op;
                                property.current = {};
                            }
                        } else {
                            //group versioned action, can be either "add" or "remove"
                            if ("add" === diff.op) {
                                if (_.isArray(diff.value)) {
                                    _.each(diff.value, (prop) => {
                                        this.userProperties.push(this.createProperty(prop, diff.op));
                                    });
                                } else {
                                    this.userProperties.push(this.createProperty(diff.value, diff.op));
                                }
                            } else if ("remove" === diff.op) {
                                _.each(this.userProperties, (prop: any) => {
                                    prop.op = diff.op;
                                    prop.current = {};
                                });
                            }
                        }
                    }
                });

                this.loading = false;
            }, (([diffError, nifiClusterStatusError, securityGroupsEnabledError, domainTypesError]) => {
                console.log("Error obtaining feed version differences");
                this.loading = false;
            }));
    }

    historyVersionsAvailable(): boolean {
        return (this.historyVersions != undefined);
    }

    diff(path: any) {
        return this.feedService.diffOperation(path);
    }

    diffCollection(path: any) {
        return this.feedService.diffCollectionOperation(path);
    }

    diffPolicies(policyIdx: any) {
        return this.feedService.joinVersionOperations(this.feedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/standardization'),
            this.feedService.diffCollectionOperation('/table/fieldPolicies/' + policyIdx + '/validation'));
    }

    createProperty(original: any, operation: any) {
        let property: any = {};
        property.versioned = {};
        property.current = CloneUtil.deepObjectCopy(original);
        property.systemName = property.current.systemName;
        property.displayName = property.current.displayName;
        property.description = property.current.description;
        property.op = operation;
        return property;
    }

    interpretDiff(diffResult: string, isSensitive?: boolean): string {
        if (isSensitive) {
            return "This is a sensitive property";
        }
        if (diffResult) {
            if (diffResult === 'add') {
                return 'Value added';
            } else if (diffResult === 'remove') {
                return 'Value removed';
            } else if (diffResult === 'replace') {
                return 'Value updated';
            }
        }
        return '';
    }


    mergeStrategyDisplayName(feed: any): any {
        return this.leftFeed.table.targetMergeStrategy;
    }

    populateFieldNameMap() {
        this.fieldNameMap = {};
        _.each(this.feed.table.tableSchema.fields, (field) => {
            this.fieldNameMap[field['name']] = field;
        });
        //console.log('Field map populated. Total entries: ' + Object.keys(this.fieldNameMap).length);
    }

    getDomainType(domainTypeId: any): any {
        return _.find(this.availableDomainTypes, (domainType: any) => {
            return (domainType.id === domainTypeId);
        });
    }

    getAllFieldPolicies(fieldWithPolicies: any): any {
        return this.feedFieldPolicyRuleService.getAllPolicyRules(fieldWithPolicies);
    }

    getAllVersionedFieldPolicies(policyIndex: any): any {
        return this.getAllFieldPolicies(this.findVersionedPolicy(policyIndex));
    }

    private findVersionedPolicy(policyIndex: any) {
        if (this.feedService.versionFeedModel && this.feedService.versionFeedModel.table && this.feedService.versionFeedModel.table.fieldPolicies) {
            return this.feedService.versionFeedModel.table.fieldPolicies[policyIndex];
        }
        return '';
    }

    getInputProperties(inputProcessorName: any, inputProcessorType: any) {
        return this.propertiesMap.filter(p => ((p.processorName == inputProcessorName) && (p.processorType == inputProcessorType)));
    }

    getNonInputProperties(inputProcessorName: any, inputProcessorType: any) {
        return this.propertiesMap.filter(p => ((p.processorName != inputProcessorName) || (p.processorType != inputProcessorType)));
    }

    populatePropertiesMap() {
        _.each(this.leftFeed.properties, (property: any) => {
            let item: any = {};
            item.nameKey = property.nameKey;
            item.key = property.key;
            item.processorName = property.processorName;
            item.processorType = property.processorType;
            item.processGroupName = property.processGroupName;
            item.sensitive = property.sensitive;
            item.leftValue = property.value;
            item.rightValue = null;
            this.propertiesMap.push(item);
        });

        _.each(this.rightFeed.properties, (property: any) => {
            let existingItem:any = this.propertiesMap.find(item => item.nameKey == property.nameKey);
            if (existingItem == null) {
                let item: any = {};
                item.nameKey = property.nameKey;
                item.key = property.key;
                item.processorName = property.processorName;
                item.processorType = property.processorType;
                item.processGroupName = property.processGroupName;
                item.sensitive = property.sensitive;
                item.leftValue = null;
                item.rightValue = property.value;
                this.propertiesMap.push(item);
            } else {
                existingItem.rightValue = property.value;
            }
        });

        _.each(this.propertiesMap, (propertyMapItem) => {
           if (((propertyMapItem.leftValue == null) && (propertyMapItem.rightValue == null)) || (propertyMapItem.leftValue == propertyMapItem.rightValue)) {
               propertyMapItem.op = "no-change";
           } else if (((propertyMapItem.leftValue == null) || (propertyMapItem.leftValue == ""))
               && ((propertyMapItem.rightValue != null) || (propertyMapItem.rightValue != ""))) {
               propertyMapItem.op = "remove";
           } else if (((propertyMapItem.leftValue != null) || (propertyMapItem.leftValue != ""))
               && ((propertyMapItem.rightValue == null) || (propertyMapItem.rightValue == ""))) {
               propertyMapItem.op = "add";
           } else if ((propertyMapItem.leftValue != propertyMapItem.rightValue)) {
               propertyMapItem.op = "replace";
           }
        });
    }
}