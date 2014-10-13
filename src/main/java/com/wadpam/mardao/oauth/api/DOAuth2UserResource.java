package com.wadpam.mardao.oauth.api;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.wadpam.mardao.crud.CrudResource;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDaoBean;
import com.wadpam.mardao.oauth.domain.DOAuth2User;
import com.wadpam.mardao.oauth.web.OAuth2Filter;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;


/**
 * Created with IntelliJ IDEA.
 *
 * @author osandstrom
 * Date: 1/18/14 Time: 8:07 PM
 */
@Path("api/oauth2user")
@Produces(MediaType.APPLICATION_JSON)
public class DOAuth2UserResource extends CrudResource<DOAuth2User, Long, DOAuth2UserDaoBean> {

  @Inject  
  private HttpServletRequest request;  
    
  @Inject
  public DOAuth2UserResource(DOAuth2UserDaoBean dao) {
    super(dao);
  }

    @GET
    @Path("me")
    public Response readMe() throws IOException {
        Long id = (Long) request.getAttribute(OAuth2Filter.NAME_USER_ID);
        return read(id);
    }

    @POST
    @Path("me")
    public Response updateMe(DOAuth2User entity) throws URISyntaxException, IOException {
        Long id = (Long) request.getAttribute(OAuth2Filter.NAME_USER_ID);
        if (null == entity.getId()) {
            entity.setId(id);
        }
        else if (!entity.getId().equals(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return update(entity);
    }

  
}
