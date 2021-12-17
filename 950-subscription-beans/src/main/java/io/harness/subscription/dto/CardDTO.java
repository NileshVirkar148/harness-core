package io.harness.subscription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDTO {
  private String id;
  private String name;
  private String last4;
  private String funding;
  private Long expireMonth;
  private Long expireYear;
  private String cvcCheck;
  private String brand;
  private String addressCity;
  private String addressCountry;
  private String addressState;
  private String addressZip;
  private String addressLine1;
  private String addressLine2;
}
