package software.wings.resources;

import static com.google.common.collect.ImmutableMap.of;
import static software.wings.beans.Base.GLOBAL_APP_ID;
import static software.wings.beans.SearchFilter.Operator.EQ;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import org.hibernate.validator.constraints.NotEmpty;
import software.wings.beans.RestResponse;
import software.wings.beans.User;
import software.wings.beans.UserInvite;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.security.UserThreadLocal;
import software.wings.security.annotations.AuthRule;
import software.wings.security.annotations.PublicApi;
import software.wings.service.intfc.UserService;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Users Resource class.
 *
 * @author Rishi
 */
@Api("users")
@Path("/users")
@Timed
@ExceptionMetered
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  private UserService userService;

  @Inject
  public UserResource(UserService userService) {
    this.userService = userService;
  }

  /**
   * List.
   *
   * @param pageRequest the page request
   * @return the rest response
   */
  @GET
  @AuthRule("USER:READ")
  public RestResponse<PageResponse<User>> list(
      @BeanParam PageRequest<User> pageRequest, @QueryParam("accountId") @NotEmpty String accountId) {
    pageRequest.addFilter("appId", GLOBAL_APP_ID, EQ);
    return new RestResponse<>(userService.list(pageRequest));
  }

  /**
   * Register.
   *
   * @param user the user
   * @return the rest response
   */
  @POST
  @PublicApi
  public RestResponse<User> register(User user) {
    user.setAppId(GLOBAL_APP_ID);
    return new RestResponse<>(userService.register(user));
  }

  /**
   * Update.
   *
   * @param userId the user id
   * @param user   the user
   * @return the rest response
   */
  @PUT
  @AuthRule("USER:WRITE")
  @Path("{userId}")
  public RestResponse<User> update(@PathParam("userId") String userId, User user) {
    user.setUuid(userId);
    return new RestResponse<>(userService.update(user));
  }

  /**
   * Delete.
   *
   * @param userId the user id
   * @return the rest response
   */
  @DELETE
  @Path("{userId}")
  @AuthRule("USER:WRITE")
  public RestResponse delete(@PathParam("userId") String userId) {
    userService.delete(userId);
    return new RestResponse();
  }

  /**
   * Get rest response.
   *
   * @return the rest response
   */
  @GET
  @AuthRule("USER:READ")
  @Path("user")
  public RestResponse<User> get() {
    return new RestResponse<>(UserThreadLocal.get().getPublicUser());
  }

  /**
   * Login.
   *
   * @param user the user
   * @return the rest response
   */
  @GET
  @Path("login")
  @PublicApi
  public RestResponse<User> login(@Auth User user) {
    return new RestResponse<>(user);
  }

  /**
   * Verify email rest response.
   *
   * @param token the token
   * @return the rest response
   * @throws URISyntaxException the uri syntax exception
   */
  @GET
  @Path("verify/{token}")
  @PublicApi
  public RestResponse<Map<String, Object>> verifyEmail(@PathParam("token") String token) throws URISyntaxException {
    return new RestResponse<>(of("success", userService.verifyEmail(token)));
  }

  /**
   * Assign role.
   *
   * @param userId the user id
   * @param roleId the role id
   * @return the rest response
   */
  @PUT
  @AuthRule("USER:WRITE")
  @Path("{userId}/role/{roleId}")
  public RestResponse<User> assignRole(@PathParam("userId") String userId, @PathParam("roleId") String roleId) {
    return new RestResponse<>(userService.addRole(userId, roleId));
  }

  /**
   * Revoke role.
   *
   * @param userId the user id
   * @param roleId the role id
   * @return the rest response
   */
  @DELETE
  @AuthRule("USER:WRITE")
  @Path("{userId}/role/{roleId}")
  public RestResponse<User> revokeRole(@PathParam("userId") String userId, @PathParam("roleId") String roleId) {
    return new RestResponse<>(userService.revokeRole(userId, roleId));
  }

  @GET
  @Path("invites")
  public RestResponse<PageResponse<UserInvite>> listInvites(@BeanParam PageRequest<UserInvite> pageRequest) {
    return new RestResponse<>(userService.listInvites(pageRequest));
  }

  @POST
  @Path("invites")
  public RestResponse<List<UserInvite>> inviteUsers(@NotNull UserInvite userInvite) {
    userInvite.setAppId(GLOBAL_APP_ID);
    return new RestResponse<>(userService.inviteUsers(userInvite));
  }

  @DELETE
  @Path("invites/{inviteId}")
  public RestResponse<UserInvite> deleteInvite(
      @PathParam("inviteId") @NotEmpty String inviteId, @QueryParam("accountId") @NotEmpty String accountId) {
    return new RestResponse<>(userService.deleteInvite(accountId, inviteId));
  }
}
