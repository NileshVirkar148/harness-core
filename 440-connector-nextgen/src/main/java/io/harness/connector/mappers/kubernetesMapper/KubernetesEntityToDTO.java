package io.harness.connector.mappers.kubernetesMapper;

import static io.harness.delegate.beans.connector.k8Connector.KubernetesAuthType.CLIENT_KEY_CERT;
import static io.harness.delegate.beans.connector.k8Connector.KubernetesAuthType.OPEN_ID_CONNECT;
import static io.harness.delegate.beans.connector.k8Connector.KubernetesAuthType.SERVICE_ACCOUNT;
import static io.harness.delegate.beans.connector.k8Connector.KubernetesAuthType.USER_PASSWORD;
import static io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialType.INHERIT_FROM_DELEGATE;
import static io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialType.MANUAL_CREDENTIALS;

import io.harness.connector.entities.embedded.kubernetescluster.K8sClientKeyCert;
import io.harness.connector.entities.embedded.kubernetescluster.K8sOpenIdConnect;
import io.harness.connector.entities.embedded.kubernetescluster.K8sServiceAccount;
import io.harness.connector.entities.embedded.kubernetescluster.K8sUserNamePassword;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesAuth;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesClusterConfig;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesClusterDetails;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesDelegateDetails;
import io.harness.connector.mappers.ConnectorEntityToDTOMapper;
import io.harness.delegate.beans.connector.k8Connector.KubernetesAuthDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesClientKeyCertDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesClusterConfigDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesClusterDetailsDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialType;
import io.harness.delegate.beans.connector.k8Connector.KubernetesDelegateDetailsDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesOpenIdConnectDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesServiceAccountDTO;
import io.harness.delegate.beans.connector.k8Connector.KubernetesUserNamePasswordDTO;
import io.harness.encryption.SecretRefData;
import io.harness.exception.UnexpectedException;
import io.harness.exception.UnknownEnumTypeException;
import io.harness.ng.service.SecretRefService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;

@Singleton
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class KubernetesEntityToDTO
    implements ConnectorEntityToDTOMapper<KubernetesClusterConfigDTO, KubernetesClusterConfig> {
  private SecretRefService secretRefService;
  private KubernetesConfigCastHelper kubernetesConfigCastHelper;

  @Override
  public KubernetesClusterConfigDTO createConnectorDTO(KubernetesClusterConfig connector) {
    KubernetesClusterConfig kubernetesClusterConfig = (KubernetesClusterConfig) connector;
    if (kubernetesClusterConfig.getCredentialType() == INHERIT_FROM_DELEGATE) {
      KubernetesDelegateDetails kubernetesDelegateDetails =
          kubernetesConfigCastHelper.castToKubernetesDelegateCredential(kubernetesClusterConfig.getCredential());
      return createInheritFromDelegateCredentialsDTO(kubernetesDelegateDetails);
    } else if (kubernetesClusterConfig.getCredentialType() == KubernetesCredentialType.MANUAL_CREDENTIALS) {
      KubernetesClusterDetails kubernetesClusterDetails =
          kubernetesConfigCastHelper.castToManualKubernetesCredentials(kubernetesClusterConfig.getCredential());
      return createManualKubernetessCredentialsDTO(kubernetesClusterDetails);
    } else {
      throw new UnknownEnumTypeException("Kubernetes credential type",
          kubernetesClusterConfig.getCredentialType() == null
              ? null
              : kubernetesClusterConfig.getCredentialType().getDisplayName());
    }
  }

  private KubernetesClusterConfigDTO createInheritFromDelegateCredentialsDTO(
      KubernetesDelegateDetails delegateCredential) {
    KubernetesCredentialDTO k8sCredentials =
        KubernetesCredentialDTO.builder()
            .config(KubernetesDelegateDetailsDTO.builder()
                        .delegateSelectors(delegateCredential.getDelegateSelectors())
                        .build())
            .kubernetesCredentialType(INHERIT_FROM_DELEGATE)
            .build();
    return KubernetesClusterConfigDTO.builder().credential(k8sCredentials).build();
  }

  private KubernetesClusterConfigDTO createManualKubernetessCredentialsDTO(
      KubernetesClusterDetails kubernetesClusterDetails) {
    KubernetesAuthDTO manualCredentials = null;
    switch (kubernetesClusterDetails.getAuthType()) {
      case USER_PASSWORD:
        K8sUserNamePassword k8sUserNamePassword = castToUserNamePassowordDTO(kubernetesClusterDetails.getAuth());
        manualCredentials = createUserPasswordDTO(k8sUserNamePassword);
        break;
      case CLIENT_KEY_CERT:
        K8sClientKeyCert k8sClientKeyCert = castToClientKeyCertDTO(kubernetesClusterDetails.getAuth());
        manualCredentials = createClientKeyCertDTO(k8sClientKeyCert);
        break;
      case SERVICE_ACCOUNT:
        K8sServiceAccount k8sServiceAccount = castToServiceAccountDTO(kubernetesClusterDetails.getAuth());
        manualCredentials = createServiceAccountDTO(k8sServiceAccount);
        break;
      case OPEN_ID_CONNECT:
        K8sOpenIdConnect k8sOpenIdConnect = castToOpenIdConnectDTO(kubernetesClusterDetails.getAuth());
        manualCredentials = createOpenIdConnectDTO(k8sOpenIdConnect);
        break;
      default:
        throw new UnknownEnumTypeException("Kubernetes Manual Credential type",
            kubernetesClusterDetails.getAuthType() == null ? null
                                                           : kubernetesClusterDetails.getAuthType().getDisplayName());
    }
    KubernetesCredentialDTO k8sCredentials = KubernetesCredentialDTO.builder()
                                                 .kubernetesCredentialType(MANUAL_CREDENTIALS)
                                                 .config(KubernetesClusterDetailsDTO.builder()
                                                             .masterUrl(kubernetesClusterDetails.getMasterUrl())
                                                             .auth(manualCredentials)
                                                             .build())
                                                 .build();
    return KubernetesClusterConfigDTO.builder().credential(k8sCredentials).build();
  }

  private KubernetesAuthDTO createUserPasswordDTO(K8sUserNamePassword userNamePasswordCredential) {
    KubernetesUserNamePasswordDTO kubernetesUserNamePasswordDTO =
        KubernetesUserNamePasswordDTO.builder()
            .username(userNamePasswordCredential.getUserName())
            .usernameRef(secretRefService.createSecretRef(userNamePasswordCredential.getUserNameRef()))
            .passwordRef(secretRefService.createSecretRef(userNamePasswordCredential.getPasswordRef()))
            .build();
    return KubernetesAuthDTO.builder().authType(USER_PASSWORD).credentials(kubernetesUserNamePasswordDTO).build();
  }

  private KubernetesAuthDTO createClientKeyCertDTO(K8sClientKeyCert k8SClientKeyCert) {
    KubernetesClientKeyCertDTO kubernetesClientKeyCertDTO =
        KubernetesClientKeyCertDTO.builder()
            .clientKeyRef(secretRefService.createSecretRef(k8SClientKeyCert.getClientKeyRef()))
            .clientCertRef(secretRefService.createSecretRef(k8SClientKeyCert.getClientCertRef()))
            .clientKeyPassphraseRef(secretRefService.createSecretRef(k8SClientKeyCert.getClientKeyPassphraseRef()))
            .clientKeyAlgo(k8SClientKeyCert.getClientKeyAlgo())
            .caCertRef(secretRefService.createSecretRef(k8SClientKeyCert.getCaCertRef()))
            .build();
    return KubernetesAuthDTO.builder().authType(CLIENT_KEY_CERT).credentials(kubernetesClientKeyCertDTO).build();
  }

  private KubernetesAuthDTO createServiceAccountDTO(K8sServiceAccount k8SServiceAccount) {
    KubernetesServiceAccountDTO kubernetesServiceAccountDTO =
        KubernetesServiceAccountDTO.builder()
            .serviceAccountTokenRef(new SecretRefData(k8SServiceAccount.getServiceAcccountTokenRef()))
            .build();
    return KubernetesAuthDTO.builder().authType(SERVICE_ACCOUNT).credentials(kubernetesServiceAccountDTO).build();
  }

  private KubernetesAuthDTO createOpenIdConnectDTO(K8sOpenIdConnect k8SOpenIdConnect) {
    KubernetesOpenIdConnectDTO kubernetesOpenIdConnectDTO =
        KubernetesOpenIdConnectDTO.builder()
            .oidcClientIdRef(secretRefService.createSecretRef(k8SOpenIdConnect.getOidcClientIdRef()))
            .oidcIssuerUrl(k8SOpenIdConnect.getOidcIssuerUrl())
            .oidcPasswordRef(secretRefService.createSecretRef(k8SOpenIdConnect.getOidcPasswordRef()))
            .oidcScopes(k8SOpenIdConnect.getOidcScopes())
            .oidcSecretRef(secretRefService.createSecretRef(k8SOpenIdConnect.getOidcSecretRef()))
            .oidcUsername(k8SOpenIdConnect.getOidcUsername())
            .oidcUsernameRef(secretRefService.createSecretRef(k8SOpenIdConnect.getOidcUsernameRef()))
            .build();
    return KubernetesAuthDTO.builder().authType(OPEN_ID_CONNECT).credentials(kubernetesOpenIdConnectDTO).build();
  }

  private K8sUserNamePassword castToUserNamePassowordDTO(KubernetesAuth kubernetesAuth) {
    try {
      return (K8sUserNamePassword) kubernetesAuth;
    } catch (ClassCastException ex) {
      throw new UnexpectedException(
          String.format("The credential type and credentials doesn't match, expected [%s] credentials", USER_PASSWORD),
          ex);
    }
  }

  private K8sClientKeyCert castToClientKeyCertDTO(KubernetesAuth kubernetesAuth) {
    try {
      return (K8sClientKeyCert) kubernetesAuth;
    } catch (ClassCastException ex) {
      throw new UnexpectedException(
          String.format("The credential type and credentials doesn't match, expected [%s] credentials", USER_PASSWORD),
          ex);
    }
  }

  private K8sServiceAccount castToServiceAccountDTO(KubernetesAuth kubernetesAuth) {
    try {
      return (K8sServiceAccount) kubernetesAuth;
    } catch (ClassCastException ex) {
      throw new UnexpectedException(
          String.format("The credential type and credentials doesn't match, expected [%s] credentials", USER_PASSWORD),
          ex);
    }
  }

  private K8sOpenIdConnect castToOpenIdConnectDTO(KubernetesAuth kubernetesAuth) {
    try {
      return (K8sOpenIdConnect) kubernetesAuth;
    } catch (ClassCastException ex) {
      throw new UnexpectedException(
          String.format("The credential type and credentials doesn't match, expected [%s] credentials", USER_PASSWORD),
          ex);
    }
  }
}
