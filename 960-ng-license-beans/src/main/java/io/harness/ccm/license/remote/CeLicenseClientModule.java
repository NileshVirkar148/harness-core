package io.harness.ccm.license.remote;

import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.security.ServiceTokenGenerator;
import io.harness.serializer.kryo.KryoConverterFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public class CeLicenseClientModule extends AbstractModule {
  private final ServiceHttpClientConfig managerClientConfig;
  private final String serviceSecret;
  private final String clientId;
  private static CeLicenseClientModule instance;

  private CeLicenseClientModule(ServiceHttpClientConfig managerClientConfig, String serviceSecret, String clientId) {
    this.managerClientConfig = managerClientConfig;
    this.serviceSecret = serviceSecret;
    this.clientId = clientId;
  }

  public static CeLicenseClientModule getInstance(
      ServiceHttpClientConfig ngManagerClientConfig, String serviceSecret, String clientId) {
    if (instance == null) {
      instance = new CeLicenseClientModule(ngManagerClientConfig, serviceSecret, clientId);
    }
    return instance;
  }

  @Provides
  private CeLicenseClientFactory ngLicenseHttpClientFactory(KryoConverterFactory kryoConverterFactory) {
    return new CeLicenseClientFactory(
        this.managerClientConfig, this.serviceSecret, new ServiceTokenGenerator(), kryoConverterFactory, clientId);
  }

  @Override
  protected void configure() {
    this.bind(CeLicenseClient.class).toProvider(CeLicenseClientFactory.class).in(Scopes.SINGLETON);
  }
}