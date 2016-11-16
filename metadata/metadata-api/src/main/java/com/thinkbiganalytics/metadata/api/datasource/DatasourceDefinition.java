package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by sr186054 on 11/14/16.
 */
public interface DatasourceDefinition {

    interface ID extends Serializable {

    }

    ;

    ID getId();


    public enum ConnectionType {
        SOURCE, DESTINATION;
    }

    ConnectionType getConnectionType();

    void setConnectionType(ConnectionType type);

    String getDatasourceType();

    void setDatasourceType(String dsType);


    String getProcessorType();

    void setProcessorType(String processorType);

    Set<String> getDatasourcePropertyKeys();

    void setDatasourcePropertyKeys(Set<String> properties);

    void setIdentityString(String identityString);

    String getIdentityString();

    String getDescription();

    void setDescription(String desc);

    String getTitle();

    void setTile(String title);


}
