/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.Datasource.ID;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;

/**
 *
 * @author Sean Felten
 */
public class DatasourceProviderImpl implements DatasourceProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#datasetCriteria()
     */
    @Override
    public DatasourceCriteria datasetCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureDatasource(java.lang.String, java.lang.String)
     */
    @Override
    public Datasource ensureDatasource(String name, String descr) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureDirectoryDatasource(java.lang.String, java.lang.String, java.nio.file.Path)
     */
    @Override
    public DirectoryDatasource ensureDirectoryDatasource(String name, String descr, Path dir) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#ensureHiveTableDatasource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HiveTableDatasource ensureHiveTableDatasource(String name, String descr, String database, String table) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#asDirectoryDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, java.nio.file.Path)
     */
    @Override
    public DirectoryDatasource asDirectoryDatasource(ID dsId, Path dir) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#asHiveTableDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID, java.lang.String, java.lang.String)
     */
    @Override
    public HiveTableDatasource asHiveTableDatasource(ID dsId, String database, String table) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource.ID)
     */
    @Override
    public Datasource getDatasource(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasources()
     */
    @Override
    public Set<Datasource> getDatasources() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#getDatasources(com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria)
     */
    @Override
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider#resolve(java.io.Serializable)
     */
    @Override
    public ID resolve(Serializable id) {
        // TODO Auto-generated method stub
        return null;
    }

}
