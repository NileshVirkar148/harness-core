package software.wings.service.impl;

import static org.mongodb.morphia.mapping.Mapper.ID_KEY;
import static software.wings.beans.ServiceInstance.Builder.aServiceInstance;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import com.mongodb.DuplicateKeyException;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.Host;
import software.wings.beans.ServiceInstance;
import software.wings.beans.ServiceTemplate;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.ServiceInstanceService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.executable.ValidateOnExecution;

// TODO: Auto-generated Javadoc

/**
 * Created by anubhaw on 5/26/16.
 */
@ValidateOnExecution
@Singleton
public class ServiceInstanceServiceImpl implements ServiceInstanceService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Inject private WingsPersistence wingsPersistence;

  /* (non-Javadoc)
   * @see software.wings.service.intfc.ServiceInstanceService#list(software.wings.dl.PageRequest)
   */
  @Override
  public PageResponse<ServiceInstance> list(PageRequest<ServiceInstance> pageRequest) {
    return wingsPersistence.query(ServiceInstance.class, pageRequest);
  }

  /* (non-Javadoc)
   * @see software.wings.service.intfc.ServiceInstanceService#save(software.wings.beans.ServiceInstance)
   */
  @Override
  public ServiceInstance save(ServiceInstance serviceInstance) {
    return wingsPersistence.saveAndGet(ServiceInstance.class, serviceInstance);
  }

  /* (non-Javadoc)
   * @see software.wings.service.intfc.ServiceInstanceService#update(software.wings.beans.ServiceInstance)
   */
  @Override
  public ServiceInstance update(ServiceInstance serviceInstance) {
    Builder<String, Object> builder =
        ImmutableMap.<String, Object>builder().put("lastDeployedOn", serviceInstance.getLastDeployedOn());
    if (serviceInstance.getArtifact() != null) {
      builder.put("artifact", serviceInstance.getArtifact());
    }
    if (serviceInstance.getRelease() != null) {
      builder.put("release", serviceInstance.getRelease());
    }
    wingsPersistence.updateFields(ServiceInstance.class, serviceInstance.getUuid(), builder.build());
    return get(serviceInstance.getAppId(), serviceInstance.getEnvId(), serviceInstance.getUuid());
  }

  /* (non-Javadoc)
   * @see software.wings.service.intfc.ServiceInstanceService#get(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ServiceInstance get(String appId, String envId, String instanceId) {
    return wingsPersistence.createQuery(ServiceInstance.class)
        .field("appId")
        .equal(appId)
        .field("envId")
        .equal(envId)
        .field(ID_KEY)
        .equal(instanceId)
        .get();
  }

  /* (non-Javadoc)
   * @see
   * software.wings.service.intfc.ServiceInstanceService#updateInstanceMappings(software.wings.beans.ServiceTemplate,
   * java.util.List, java.util.List)
   */
  @Override
  public void updateInstanceMappings(ServiceTemplate template, List<Host> addedHosts, List<Host> deletedHosts) {
    Query<ServiceInstance> deleteQuery = wingsPersistence.createQuery(ServiceInstance.class)
                                             .field("appId")
                                             .equal(template.getAppId())
                                             .field("serviceTemplate")
                                             .equal(template.getUuid())
                                             .field("host")
                                             .hasAnyOf(deletedHosts);
    wingsPersistence.delete(deleteQuery);

    addedHosts.forEach(host -> {
      ServiceInstance serviceInstance =
          aServiceInstance()
              .withAppId(template.getAppId())
              .withEnvId(template.getEnvId()) // Fixme: do it one by one and ignore unique constraints failure
              .withServiceTemplate(template)
              .withHost(host)
              .build();
      try {
        wingsPersistence.save(serviceInstance);
      } catch (DuplicateKeyException ex) {
        logger.warn("Reinserting an existing service instance ignore");
      }
    });
  }

  /* (non-Javadoc)
   * @see software.wings.service.intfc.ServiceInstanceService#delete(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void delete(String appId, String envId, String instanceId) {
    wingsPersistence.delete(wingsPersistence.createQuery(ServiceInstance.class)
                                .field("appId")
                                .equal(appId)
                                .field("envId")
                                .equal(envId)
                                .field(ID_KEY)
                                .equal(instanceId));
  }

  @Override
  public void deleteByEnv(String appId, String envId) {
    wingsPersistence.createQuery(ServiceInstance.class)
        .field("appId")
        .equal(appId)
        .field("envId")
        .equal(envId)
        .asList()
        .forEach(serviceInstance -> delete(appId, envId, serviceInstance.getUuid()));
  }

  @Override
  public void deleteByServiceTemplate(String appId, String envId, String templateId) {
    wingsPersistence.createQuery(ServiceInstance.class)
        .field("appId")
        .equal(appId)
        .field("envId")
        .equal(envId)
        .field("serviceTemplate")
        .equal(templateId)
        .asList()
        .forEach(serviceInstance -> delete(appId, envId, serviceInstance.getUuid()));
  }
}
