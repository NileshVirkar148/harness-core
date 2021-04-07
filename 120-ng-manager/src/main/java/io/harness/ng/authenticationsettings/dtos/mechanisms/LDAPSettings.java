package io.harness.ng.authenticationsettings.dtos.mechanisms;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import software.wings.beans.sso.LdapConnectionSettings;
import software.wings.beans.sso.LdapGroupSettings;
import software.wings.beans.sso.LdapUserSettings;
import software.wings.security.authentication.AuthenticationMechanism;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("LDAP")
@OwnedBy(HarnessTeam.PL)
public class LDAPSettings extends NGAuthSettings {
  @NotNull @Valid LdapConnectionSettings connectionSettings;

  @Valid List<LdapUserSettings> userSettingsList;

  @Valid List<LdapGroupSettings> groupSettingsList;

  public LDAPSettings(@JsonProperty("connectionSettings") LdapConnectionSettings connectionSettings,
      @JsonProperty("userSettingsList") List<LdapUserSettings> userSettingsList,
      @JsonProperty("groupSettingsList") List<LdapGroupSettings> groupSettingsList) {
    super(AuthenticationMechanism.LDAP);
    this.connectionSettings = connectionSettings;
    this.userSettingsList = userSettingsList;
    this.groupSettingsList = groupSettingsList;
  }

  @Override
  public AuthenticationMechanism getSettingsType() {
    return AuthenticationMechanism.LDAP;
  }
}
