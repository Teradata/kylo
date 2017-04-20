package com.thinkbiganalytics.metadata.api.datasource;

/*-
 * #%L
 * thinkbig-metadata-api
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

public interface DatasourceProvider {

    DatasourceCriteria datasetCriteria();

    DerivedDatasource ensureDerivedDatasource(String datasourceType, String identityString, String title, String desc, Map<String, Object> properties);

    DerivedDatasource findDerivedDatasource(String datasourceType, String systemName);


    <D extends Datasource> D ensureDatasource(String name, String descr, Class<D> type);


    DerivedDatasource ensureGenericDatasource(String name, String descr);

    Datasource getDatasource(Datasource.ID id);

    void removeDatasource(Datasource.ID id);

    List<Datasource> getDatasources();

    List<Datasource> getDatasources(DatasourceCriteria criteria);

    Datasource.ID resolve(Serializable id);

    <D extends DatasourceDetails> Optional<D> ensureDatasourceDetails(@Nonnull Datasource.ID id, @Nonnull Class<D> type);
}
