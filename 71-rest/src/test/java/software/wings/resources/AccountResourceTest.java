package software.wings.resources;

import static io.harness.rule.OwnerRule.HANTANG;
import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Provider;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.marketplace.gcp.GcpMarketPlaceApiHandler;
import io.harness.rest.RestResponse;
import io.harness.rule.Owner;
import io.harness.scheduler.PersistentScheduler;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.wings.features.api.FeatureService;
import software.wings.licensing.LicenseService;
import software.wings.service.intfc.AccountService;
import software.wings.service.intfc.UserService;
import software.wings.utils.AccountPermissionUtils;
import software.wings.utils.ResourceTestRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

public class AccountResourceTest extends CategoryTest {
  private String accountId = "ACCOUNT_ID";

  private static AccountService accountService = mock(AccountService.class);
  private static UserService userService = mock(UserService.class);
  private static LicenseService licenseService = mock(LicenseService.class);
  private static Provider<LicenseService> licenseServiceProvider = (Provider<LicenseService>) mock(Provider.class);
  private static AccountPermissionUtils accountPermissionUtils = mock(AccountPermissionUtils.class);
  private static FeatureService featureService = mock(FeatureService.class);
  private static PersistentScheduler jobScheduler = mock(PersistentScheduler.class);
  private static GcpMarketPlaceApiHandler gcpMarketPlaceApiHandler = mock(GcpMarketPlaceApiHandler.class);

  @ClassRule
  public static ResourceTestRule RESOURCES =
      ResourceTestRule.builder()
          .instance(new AccountResource(accountService, userService, licenseServiceProvider, accountPermissionUtils,
              featureService, jobScheduler, gcpMarketPlaceApiHandler))
          .build();
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    when(licenseServiceProvider.get()).thenReturn(licenseService);
  }

  @Test
  @Owner(developers = HANTANG)
  @Category(UnitTests.class)
  public void shouldStartCeTrial() {
    RESOURCES.client()
        .target(format("/account/continuous-efficiency/%s/startTrial", accountId))
        .request()
        .post(entity(Entity.json(""), MediaType.APPLICATION_JSON), new GenericType<RestResponse<Boolean>>() {});
    verify(licenseService).startCeLimitedTrial(eq(accountId));
  }
}
