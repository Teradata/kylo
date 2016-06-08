package com.thinkbiganalytics.metadata.modeshape.datasource;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 6/7/16.
 */
public class JcrDatasourceProvider extends BaseJcrProvider<Datasource, Datasource.ID> implements DatasourceProvider {

    @Override
    public Class<? extends Datasource> getEntityClass() {
        return JcrDatasource.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrDatasource.class;
    }

    @Override
    public String getNodeType() {
        return JcrDatasource.NODE_TYPE;
    }

    @Override
    public DatasourceCriteria datasetCriteria() {
        return null;
    }

    @Override
    public Datasource ensureDatasource(String name, String descr) {
        String path = EntityUtil.pathForDataSource();
        Map<String, Object> props = new HashMap<>();
        props.put(JcrDatasource.SYSTEM_NAME, name);
        JcrDatasource datasource = (JcrDatasource) findOrCreateEntity(path, name, props);
        datasource.setDescription(descr);
        return datasource;
    }

    @Override
    public Datasource getDatasource(Datasource.ID id) {
        return findById(id);
    }

    @Override
    public List<Datasource> getDatasources() {
        return findAll();
    }

    @Override
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        return null;
    }

    @Override
    public Datasource.ID resolve(Serializable id) {
        return resolveId(id);
    }


    @Override
    public DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir) {
        return null;
    }

    @Override
    public HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table) {
        return null;
    }

    @Override
    public DirectoryDatasource asDirectoryDatasource(Datasource.ID dsId, Path dir) {
        return null;
    }

    @Override
    public HiveTableDatasource asHiveTableDatasource(Datasource.ID dsId, String database, String table) {
        return null;
    }


}
