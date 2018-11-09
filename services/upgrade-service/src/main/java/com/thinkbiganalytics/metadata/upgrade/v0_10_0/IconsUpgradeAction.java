package com.thinkbiganalytics.metadata.upgrade.v0_10_0;

/*-
 * #%L
 * kylo-upgrade-service
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrProperties;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IconableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

import javax.inject.Inject;


/**
 * Upgrade the icons to the consolidated icon handling.
 */
@Component("modelConsolidationUpgradeAction0.10.0")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class IconsUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(IconsUpgradeAction.class);
    
    @Inject
    private JcrCategoryProvider categoryProvider;
    
    @Inject
    private JcrDatasourceProvider datasourceProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.10", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Migrating icon definitions for version: {}", targetVersion);
        
        this.categoryProvider.findAll().stream()
            .map(JcrCategory.class::cast)
            .forEach(cat -> moveIconFromProperties(cat));
        
        this.datasourceProvider.findAll().stream()
            .map(JcrDatasource.class::cast)
            .forEach(cat -> moveIconFromProperties(cat));
    }
    
    
    private void moveIconFromProperties(PropertiedMixin propMixin) {
        Optional<JcrProperties> propsObj = Optional.ofNullable(JcrUtil.getJcrObject(propMixin.getNode(), PropertiedMixin.PROPERTIES_NAME, JcrProperties.class));
        
        propsObj.ifPresent(props -> {
            String icon = props.getProperty(IconableMixin.ICON, null);
            String iconColor = props.getProperty(IconableMixin.ICON_COLOR, null);
            
            if (icon != null) {
                propMixin.setProperty(IconableMixin.ICON, icon);
                props.removeProperty(IconableMixin.ICON);
            }
            
            if (iconColor != null) {
                propMixin.setProperty(IconableMixin.ICON_COLOR, iconColor);
                props.removeProperty(IconableMixin.ICON_COLOR);
            }
            
            if (props.getProperties().isEmpty()) {
                JcrUtil.removeNode(props.getNode());
            }
        });
    }
}
