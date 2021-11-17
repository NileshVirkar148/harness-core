package io.harness.ngaggregate.remote;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.remote.client.AbstractHttpClientFactory;
import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.security.ServiceTokenGenerator;
import io.harness.serializer.kryo.KryoConverterFactory;
import io.harness.userng.remote.UserNGClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Singleton
@OwnedBy(HarnessTeam.PL)
public class NGAggregateClientFactory extends AbstractHttpClientFactory implements Provider<NGAggregateClient> {
    public NGAggregateClientFactory(ServiceHttpClientConfig secretManagerConfig, String serviceSecret,
                                    ServiceTokenGenerator tokenGenerator, KryoConverterFactory kryoConverterFactory, String clientId,
                                    ClientMode clientMode) {
        super(secretManagerConfig, serviceSecret, tokenGenerator, kryoConverterFactory, clientId, false, clientMode);
    }
@Override
    public NGAggregateClient get() {
    return getRetrofit().create(NGAggregateClient.class);
}
}
