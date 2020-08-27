package io.harness.perpetualtask.k8s.watch;

import static com.google.common.base.MoreObjects.firstNonNull;
import static io.harness.ccm.health.HealthStatusService.CLUSTER_ID_IDENTIFIER;
import static io.harness.perpetualtask.k8s.watch.PVEvent.EventType.EVENT_TYPE_EXPANSION;
import static io.harness.perpetualtask.k8s.watch.PVEvent.EventType.EVENT_TYPE_START;
import static io.harness.perpetualtask.k8s.watch.PVEvent.EventType.EVENT_TYPE_STOP;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.protobuf.Timestamp;

import io.harness.event.client.EventPublisher;
import io.harness.grpc.utils.HTimestamps;
import io.harness.perpetualtask.k8s.informer.ClusterDetails;
import io.kubernetes.client.informer.EventType;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.StorageV1Api;
import io.kubernetes.client.openapi.models.V1PersistentVolume;
import io.kubernetes.client.openapi.models.V1PersistentVolumeList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeSpec;
import io.kubernetes.client.util.CallGeneratorParams;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class PVWatcher implements ResourceEventHandler<V1PersistentVolume> {
  private final String clusterId;
  private final Set<String> publishedPVs;
  private final EventPublisher eventPublisher;
  private final StorageV1Api storageV1Api;

  private final PVInfo pvInfoPrototype;
  private final PVEvent pvEventPrototype;

  private static final String EVENT_LOG_MSG = "V1PersistentVolume: {}, action: {}";
  private static final String ERROR_PUBLISH_LOG_MSG = "Error publishing V1PersistentVolume.{} event.";

  @Inject
  public PVWatcher(@Assisted ApiClient apiClient, @Assisted ClusterDetails params,
      @Assisted SharedInformerFactory sharedInformerFactory, EventPublisher eventPublisher) {
    logger.info(
        "Creating new PVWatcher for cluster with id: {} name: {} ", params.getClusterId(), params.getClusterName());

    this.clusterId = params.getClusterId();
    this.publishedPVs = new ConcurrentSkipListSet<>();
    this.eventPublisher = eventPublisher;

    this.pvInfoPrototype = PVInfo.newBuilder()
                               .setCloudProviderId(params.getCloudProviderId())
                               .setClusterId(clusterId)
                               .setClusterName(params.getClusterName())
                               .setKubeSystemUid(params.getKubeSystemUid())
                               .build();

    this.pvEventPrototype = PVEvent.newBuilder()
                                .setCloudProviderId(params.getCloudProviderId())
                                .setClusterId(clusterId)
                                .setKubeSystemUid(params.getKubeSystemUid())
                                .build();

    this.storageV1Api = new StorageV1Api(apiClient);
    CoreV1Api coreV1Api = new CoreV1Api(apiClient);
    sharedInformerFactory
        .sharedIndexInformerFor((CallGeneratorParams callGeneratorParams)
                                    -> coreV1Api.listPersistentVolumeCall(null, null, null, null, null, null,
                                        callGeneratorParams.resourceVersion, callGeneratorParams.timeoutSeconds,
                                        callGeneratorParams.watch, null),
            V1PersistentVolume.class, V1PersistentVolumeList.class)
        .addEventHandler(this);
  }

  @Override
  public void onAdd(V1PersistentVolume persistentVolume) {
    try {
      logger.debug(EVENT_LOG_MSG, persistentVolume.getMetadata().getUid(), EventType.ADDED);

      publishPVInfo(persistentVolume);
      publishPVEvent(persistentVolume,
          HTimestamps.fromMillis(persistentVolume.getMetadata().getCreationTimestamp().getMillis()), EVENT_TYPE_START);

      publishedPVs.add(persistentVolume.getMetadata().getUid());
    } catch (Exception ex) {
      logger.error(ERROR_PUBLISH_LOG_MSG, EventType.ADDED, ex);
    }
  }

  @Override
  public void onUpdate(V1PersistentVolume oldPersistentVolume, V1PersistentVolume persistentVolume) {
    try {
      logger.debug(EVENT_LOG_MSG, persistentVolume.getMetadata().getUid(), EventType.MODIFIED);

      if (!publishedPVs.contains(persistentVolume.getMetadata().getUid())) {
        publishPVInfo(persistentVolume);
      }

      long oldVolSize = K8sResourceUtils.getStorageCapacity(oldPersistentVolume.getSpec()).getAmount();
      long newVolSize = K8sResourceUtils.getStorageCapacity(persistentVolume.getSpec()).getAmount();

      if (oldVolSize != newVolSize) {
        logger.debug("Volume change observed from {} to {}", oldVolSize, newVolSize);
        publishPVEvent(persistentVolume, HTimestamps.fromMillis(DateTime.now().getMillis()), EVENT_TYPE_EXPANSION);
      }
    } catch (Exception ex) {
      logger.error(ERROR_PUBLISH_LOG_MSG, EventType.MODIFIED, ex);
    }
  }

  @Override
  public void onDelete(V1PersistentVolume persistentVolume, boolean deletedFinalStateUnknown) {
    try {
      logger.debug(EVENT_LOG_MSG, persistentVolume.getMetadata().getUid(), EventType.DELETED);

      publishPVEvent(persistentVolume,
          HTimestamps.fromMillis(
              ofNullable(persistentVolume.getMetadata().getDeletionTimestamp()).orElse(DateTime.now()).getMillis()),
          EVENT_TYPE_STOP);

      publishedPVs.remove(persistentVolume.getMetadata().getUid());
    } catch (Exception ex) {
      logger.error(ERROR_PUBLISH_LOG_MSG, EventType.DELETED, ex);
    }
  }

  private void publishPVInfo(V1PersistentVolume persistentVolume) {
    Timestamp timestamp = HTimestamps.fromMillis(persistentVolume.getMetadata().getCreationTimestamp().getMillis());

    PVInfo pvInfo =
        PVInfo.newBuilder(pvInfoPrototype)
            .setPvType(getPvType(persistentVolume.getSpec()))
            .setPvUid(persistentVolume.getMetadata().getUid())
            .setPvName(persistentVolume.getMetadata().getName())
            .setCreationTimestamp(timestamp)
            .putAllLabels(firstNonNull(persistentVolume.getMetadata().getLabels(), Collections.emptyMap()))
            .putAllLabels(firstNonNull(persistentVolume.getMetadata().getAnnotations(), Collections.emptyMap()))
            .setClaimName(getClaimName(persistentVolume.getSpec()))
            .setClaimNamespace(getClaimNamespace(persistentVolume.getSpec()))
            .setStorageClassType(getStorageType(
                ofNullable(persistentVolume.getSpec().getStorageClassName()).orElse(""))) // empty class means default
            .setCapacity(K8sResourceUtils.getStorageCapacity(persistentVolume.getSpec()))
            .build();
    eventPublisher.publishMessage(pvInfo, timestamp, ImmutableMap.of(CLUSTER_ID_IDENTIFIER, clusterId));
  }

  private String getClaimNamespace(V1PersistentVolumeSpec spec) {
    if (spec != null && spec.getClaimRef() != null && spec.getClaimRef().getNamespace() != null) {
      return spec.getClaimRef().getNamespace();
    }
    return "";
  }

  private String getClaimName(V1PersistentVolumeSpec spec) {
    if (spec != null && spec.getClaimRef() != null && spec.getClaimRef().getName() != null) {
      return spec.getClaimRef().getName();
    }
    return "";
  }

  public void publishPVEvent(V1PersistentVolume persistentVolume, Timestamp timestamp, PVEvent.EventType type) {
    final String uid = persistentVolume.getMetadata().getUid();

    PVEvent pvEvent = PVEvent.newBuilder(pvEventPrototype)
                          .setPvUid(uid)
                          .setPvName(persistentVolume.getMetadata().getName())
                          .setEventType(type)
                          .setTimestamp(timestamp)
                          .build();

    logger.debug("Publishing : {}", pvEvent.getEventType());
    eventPublisher.publishMessage(pvEvent, timestamp, ImmutableMap.of(CLUSTER_ID_IDENTIFIER, clusterId));
  }

  public PVInfo.PVType getPvType(V1PersistentVolumeSpec spec) {
    if (spec.getGcePersistentDisk() != null) {
      return PVInfo.PVType.PV_TYPE_GCE_PERSISTENT_DISK;
    } else {
      return PVInfo.PVType.PV_TYPE_UNSPECIFIED;
    }
  }

  public String getStorageType(String name) {
    try {
      return this.storageV1Api.readStorageClass(name, null, null, null).getParameters().get("type");
    } catch (Exception ex) {
      logger.warn("Failed to get storageClass type, returning default", ex);
    }
    return "";
  }
}
