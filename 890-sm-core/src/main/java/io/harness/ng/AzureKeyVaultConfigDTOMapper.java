package io.harness.ng;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.ConnectorDTO;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.delegate.beans.connector.azurekeyvaultconnector.AzureKeyVaultConnectorDTO;
import io.harness.secretmanagerclient.dto.azurekeyvault.AzureKeyVaultConfigDTO;
import io.harness.secretmanagerclient.dto.azurekeyvault.AzureKeyVaultConfigUpdateDTO;
import io.harness.security.encryption.EncryptionType;

import lombok.experimental.UtilityClass;

@OwnedBy(PL)
@UtilityClass
public class AzureKeyVaultConfigDTOMapper {
  public static AzureKeyVaultConfigUpdateDTO getAzureKeyVaultConfigUpdateDTO(
      ConnectorDTO connectorRequestDTO, AzureKeyVaultConnectorDTO azureKeyVaultConnectorDTO) {
    azureKeyVaultConnectorDTO.validate();
    ConnectorInfoDTO connector = connectorRequestDTO.getConnectorInfo();
    AzureKeyVaultConfigUpdateDTO azureKeyVaultConfigUpdateDTO =
        AzureKeyVaultConfigUpdateDTO.builder()
            .clientId(azureKeyVaultConnectorDTO.getClientId())
            .subscription(azureKeyVaultConnectorDTO.getSubscription())
            .tenantId(azureKeyVaultConnectorDTO.getTenantId())
            .azureEnvironmentType(azureKeyVaultConnectorDTO.getAzureEnvironmentType())
            .vaultName(azureKeyVaultConnectorDTO.getVaultName())
            .isDefault(false)
            .name(connector.getName())
            .encryptionType(EncryptionType.AZURE_VAULT)
            .tags(connector.getTags())
            .description(connector.getDescription())
            .build();
    if (null != azureKeyVaultConnectorDTO.getSecretKey().getDecryptedValue()) {
      azureKeyVaultConfigUpdateDTO.setSecretKey(
          String.valueOf(azureKeyVaultConnectorDTO.getSecretKey().getDecryptedValue()));
    }
    return azureKeyVaultConfigUpdateDTO;
  }

  public static AzureKeyVaultConfigDTO getAzureKeyVaultConfigDTO(
      String accountIdentifier, ConnectorDTO connectorRequestDTO, AzureKeyVaultConnectorDTO azureKeyVaultConnectorDTO) {
    azureKeyVaultConnectorDTO.validate();
    ConnectorInfoDTO connector = connectorRequestDTO.getConnectorInfo();
    AzureKeyVaultConfigDTO azureKeyVaultConfigDTO =
        AzureKeyVaultConfigDTO.builder()
            .clientId(azureKeyVaultConnectorDTO.getClientId())
            .subscription(azureKeyVaultConnectorDTO.getSubscription())
            .tenantId(azureKeyVaultConnectorDTO.getTenantId())
            .azureEnvironmentType(azureKeyVaultConnectorDTO.getAzureEnvironmentType())
            .vaultName(azureKeyVaultConnectorDTO.getVaultName())
            .isDefault(false)
            .encryptionType(EncryptionType.AZURE_VAULT)

            .name(connector.getName())
            .accountIdentifier(accountIdentifier)
            .orgIdentifier(connector.getOrgIdentifier())
            .projectIdentifier(connector.getProjectIdentifier())
            .tags(connector.getTags())
            .identifier(connector.getIdentifier())
            .description(connector.getDescription())
            .build();
    if (null != azureKeyVaultConnectorDTO.getSecretKey().getDecryptedValue()) {
      azureKeyVaultConfigDTO.setSecretKey(String.valueOf(azureKeyVaultConnectorDTO.getSecretKey().getDecryptedValue()));
    }
    return azureKeyVaultConfigDTO;
  }
}
