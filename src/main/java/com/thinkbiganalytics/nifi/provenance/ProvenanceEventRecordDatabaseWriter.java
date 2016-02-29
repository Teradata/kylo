package com.thinkbiganalytics.nifi.provenance;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.admin.dao.DataAccessException;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.h2.util.JdbcUtils;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sr186054 on 2/25/16.
 * see mysql-schema.sql for db table scripts
 */
public class ProvenanceEventRecordDatabaseWriter implements ProvenanceEventRecordWriter
{

    private DataFieldMaxValueIncrementer nifiProvenanceEventSequencer;
    private DataSource dataSource;
    private ProvenanceEventRecordSerializer serializer;
    private static String NIFI_PROVENANCE_EVENT_TABLE = "NIFI_PROVENANCE_EVENT";
    private String databaseDriverClassName;

    public ProvenanceEventRecordDatabaseWriter(){
        serializer = new ProvenanceEventRecordSerializer();
        String uri = PropertiesLoader.getInstance().getProperty(PropertiesLoader.DATASOURCE_URL);
        String user = PropertiesLoader.getInstance().getProperty(PropertiesLoader.DATASOURCE_USERNAME);
        String password = PropertiesLoader.getInstance().getProperty(PropertiesLoader.DATASOURCE_PASSWORD);
        databaseDriverClassName = PropertiesLoader.getInstance().getProperty(PropertiesLoader.DATABASE_DRIVER_CLASS_NAME);
        try {
            Class.forName(databaseDriverClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
         dataSource =  PoolingDataSourceService.getDataSource(uri,user,password);
        System.out.println("ProvenanceEventRecordDatabaseWriter!!! "+uri);
        nifiProvenanceEventSequencer = new MySQLMaxValueIncrementer(dataSource,"NIFI_PROVENANCE_EVENT_SEQ","ID");
    }



    public DataSource getDataSource(){
        return dataSource;
    }


    @Override
    /**
     * Write the Event to the Database Table
     */
    public Long persistProvenanceEventRecord(ProvenanceEventRecord event) {
        System.out.println("persistProvenanceEventRecord!!! "+event);
        Statement stmt = null;
        Connection conn  = null;
        Long id = null;
        try {
            String json = serializer.getAsJSON(event);
            String attributesJson = serializer.getAttributesAsJSON(event);
            conn = getDataSource().getConnection();
            Long eventId = event.getEventId();
            String sql = "INSERT INTO "+NIFI_PROVENANCE_EVENT_TABLE+"(EVENT_ID,NIFI_EVENT_ID,EVENT_TYPE,FLOW_FILE_UUID,FLOW_FILE_ENTRY_DATE,EVENT_TIME,EVENT_DURATION,LINEAGE_START_DATE,COMPONENT_ID,COMPONENT_TYPE,DETAILS,PARENT_UUIDS,CHILD_UUIDS,ATTRIBUTES_JSON,JSON)" +
                                                                     "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            stmt = preparedStatement;
            id = nifiProvenanceEventSequencer.nextLongValue();
            preparedStatement.setLong(1,id);
            preparedStatement.setLong(2, event.getEventId());
            preparedStatement.setString(3, event.getEventType().toString());
            preparedStatement.setString(4, event.getFlowFileUuid());
            preparedStatement.setLong(5, event.getFlowFileEntryDate());
            preparedStatement.setLong(6, event.getEventTime());
            preparedStatement.setLong(7, event.getEventDuration());
            preparedStatement.setLong(8, event.getLineageStartDate());
            preparedStatement.setString(9, event.getComponentId());
            preparedStatement.setString(10, event.getComponentType());
            preparedStatement.setString(11,event.getDetails());
            preparedStatement.setString(12, StringUtils.join(event.getParentUuids(), ","));
            preparedStatement.setString(13, StringUtils.join(event.getChildUuids(), ","));
            preparedStatement.setString(14,attributesJson);
            preparedStatement.setString(15, json);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }catch (SQLException e){

            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }catch (SQLException e){

            }
        }
        return id;
    }

    @Override
    /**
     * NIFI works of in memory event ids that change.  get the max nifi event id... not our internal event id sequencer
     */
    public Long getLastRecordedEventId() {
        System.out.println("getLastRecordedEventId!!! ");
        Statement stmt = null;
        Connection conn  = null;
        Long eventId = null;
        try {
            conn = getDataSource().getConnection();
            stmt = conn.createStatement();
            String query = "SELECT MAX(NIFI_EVENT_ID) EVENT_ID FROM "+NIFI_PROVENANCE_EVENT_TABLE;
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                eventId = rs.getLong("EVENT_ID");
            }
            System.out.println("getLastRecordedEventId!!! "+eventId);
            conn.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }catch (SQLException e){

            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }catch (SQLException e){

            }
        }
        return eventId;

    }
}
