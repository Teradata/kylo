package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupsEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Form;

/**
 * Implements a {@link NiFiProcessGroupsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiProcessGroupsRestClientV0 extends AbstractNiFiProcessGroupsRestClient {

    /**
     * Base path for process group requests
     */
    private static final String BASE_PATH = "/controller/process-groups";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiProcessGroupsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessGroupsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public ProcessGroupDTO create(@Nonnull final String parentGroupId, @Nonnull final String name) {
        ProcessGroupEntity entity = new ProcessGroupEntity();
        ProcessGroupDTO group = new ProcessGroupDTO();
        group.setName(name);
        client.updateEntityForSave(entity);
        try {
            entity.setProcessGroup(group);
            ProcessGroupEntity
                    returnedGroup =
                    client.post(BASE_PATH + "/" + parentGroupId + "/process-group-references", entity, ProcessGroupEntity.class);
            return returnedGroup.getProcessGroup();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ConnectionDTO createConnection(@Nonnull final String processGroupId, @Nonnull final ConnectableDTO source, @Nonnull final ConnectableDTO dest) {
        try {
            ConnectionDTO connectionDTO = new ConnectionDTO();
            connectionDTO.setSource(source);
            connectionDTO.setDestination(dest);
            connectionDTO.setName(source.getName() + " - " + dest.getName());
            ConnectionEntity connectionEntity = new ConnectionEntity();
            connectionEntity.setConnection(connectionDTO);
            client.updateEntityForSave(connectionEntity);
            return client.post(BASE_PATH + "/" + processGroupId + "/connections", connectionEntity, ConnectionEntity.class).getConnection();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ProcessGroupDTO> findAll(@Nonnull final String parentGroupId) {
        try {
            return client.get(BASE_PATH + "/" + parentGroupId + "/process-group-references", null, ProcessGroupsEntity.class).getProcessGroups();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Optional<ProcessGroupDTO> findById(@Nonnull final String processGroupId, final boolean recursive, final boolean verbose) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("recursive", recursive);
            params.put("verbose", verbose);
            return Optional.of(client.get(BASE_PATH + "/" + processGroupId, params, ProcessGroupEntity.class).getProcessGroup());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Set<ConnectionDTO> getConnections(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/connections", null, ConnectionsEntity.class).getConnections();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<PortDTO> getInputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/input-ports/", null, InputPortsEntity.class).getInputPorts();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<PortDTO> getOutputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + "/" + processGroupId + "/output-ports", null, OutputPortsEntity.class).getOutputPorts();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public FlowSnippetDTO instantiateTemplate(@Nonnull final String processGroupId, @Nonnull final String templateId) {
        try {
            Entity status = client.getControllerRevision();
            String clientId = status.getRevision().getClientId();
            String originX = "10";
            String originY = "10";
            Form form = new Form();
            form.param("templateId", templateId);
            form.param("clientId", clientId);
            form.param("originX", originX);
            form.param("originY", originY);
            form.param("version", status.getRevision().getVersion().toString());
            FlowSnippetEntity response = client.postForm(BASE_PATH + "/" + processGroupId + "/template-instance", form, FlowSnippetEntity.class);
            return response.getContents();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Unable to create Template instance for templateId: " + templateId + " under Process group " + processGroupId
                                                     + ".  Unable find the processGroup or template");
        } catch (ClientErrorException e) {
            final String msg = e.getResponse().readEntity(String.class);
            throw new NifiComponentNotFoundException("Unable to create Template instance for templateId: " + templateId + " under Process group " + processGroupId + ". " + msg);
        }
    }

    @Nonnull
    @Override
    public ProcessGroupDTO update(@Nonnull final ProcessGroupDTO processGroupEntity) {
        try {
            final ProcessGroupEntity entity = new ProcessGroupEntity();
            entity.setProcessGroup(processGroupEntity);
            client.updateEntityForSave(entity);
            return client.put(BASE_PATH + "/" + processGroupEntity.getId(), processGroupEntity, ProcessGroupEntity.class).getProcessGroup();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupEntity.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    protected Optional<ProcessGroupDTO> doDelete(@Nonnull final ProcessGroupDTO processGroup) {
        Map<String, Object> params = client.getUpdateParams();
        try {
            ProcessGroupEntity entity = client.delete(BASE_PATH + "/" + processGroup.getParentGroupId() + "/process-group-references/" + processGroup.getId(), params,
                                                      ProcessGroupEntity.class);
            return Optional.of(entity.getProcessGroup());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
