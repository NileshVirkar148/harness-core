package io.harness.pms.sdk.core.steps.io;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.serializer.KryoSerializer;
import io.harness.tasks.ResponseData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;

@OwnedBy(CDC)
@Singleton
public class ResponseDataMapper {
  @Inject private KryoSerializer kryoSerializer;

  public Map<String, ResponseData> fromResponseDataProto(Map<String, ByteString> byteStringMap) {
    Map<String, ResponseData> responseDataMap = new HashMap<>();
    if (EmptyPredicate.isNotEmpty(byteStringMap)) {
      byteStringMap.forEach(
          (k, v) -> responseDataMap.put(k, (ResponseData) kryoSerializer.asInflatedObject(v.toByteArray())));
    }
    return responseDataMap;
  }

  public Map<String, ByteString> toResponseDataProto(Map<String, ResponseData> responseDataMap) {
    Map<String, ByteString> byteStringMap = new HashMap<>();
    if (EmptyPredicate.isNotEmpty(responseDataMap)) {
      responseDataMap.forEach((k, v) -> byteStringMap.put(k, ByteString.copyFrom(kryoSerializer.asDeflatedBytes(v))));
    }
    return byteStringMap;
  }
}
