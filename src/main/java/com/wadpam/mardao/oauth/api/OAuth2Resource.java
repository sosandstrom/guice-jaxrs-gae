/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.mardao.oauth.api;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.wadpam.mardao.oauth.dao.DConnectionDao;
import com.wadpam.mardao.oauth.dao.DFactoryDao;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDao;
import com.wadpam.mardao.oauth.domain.DConnection;
import com.wadpam.mardao.oauth.domain.DFactory;
import com.wadpam.mardao.oauth.domain.DOAuth2User;
import com.wadpam.mardao.oauth.web.OAuth2Filter;
import com.wadpam.mardao.social.SocialProfile;
import com.wadpam.mardao.social.SocialTemplate;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osandstrom
 */
@Path("oauth/federated")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OAuth2Resource {
    private static final boolean autoCreateUser = true;
    static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Resource.class);
    
    private final DConnectionDao connectionDao;
    private final DFactoryDao factoryDao;
    private final DOAuth2UserDao userDao;
    
    @Inject
    public OAuth2Resource(DConnectionDao connectionDao, DFactoryDao factoryDao, 
            DOAuth2UserDao userDao) {
        this.connectionDao = connectionDao;
        this.userDao = userDao;
        this.factoryDao = factoryDao;
        
        if (SystemProperty.Environment.Value.Development == SystemProperty.environment.value()) {
            factoryDao.persist(SocialTemplate.PROVIDER_ID_FACEBOOK, "https://graph.facebook.com", "255653361131262", "43801e00b5f2e540b672b19943e164ba");
        }
    }
    
//    @POST
//    public Response registerFederatedPost(DConnection conn) {
//        try {
//            connectionDao.persist(conn);
//            
//            URI location = new URI("connection/" + conn.getId());
//            return Response.created(location).entity(conn).build();
//        } catch (URISyntaxException ex) {
//            return Response.status(Status.BAD_REQUEST).build();
//        }
//    }
//    
//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    public Response registerFederatedForm(
//        @FormParam("providerId") String providerId,
//        @FormParam("providerUserId") String providerUserId,
//        @FormParam("access_token") String access_token,
//        @FormParam("secret") String secret,
//        @FormParam("expires_in") String expiresInString
//            ) {
//        
//        DConnection conn = new DConnection();
//        conn.setAccessToken(access_token);
//        conn.setProviderId(providerId);
//        conn.setProviderUserId(providerUserId);
//
//        return registerFederated(conn);
//    }
//    
    @GET
    public Response registerFederatedGet(
        @QueryParam("providerId") String providerId,
        @QueryParam("providerUserId") String providerUserId,
        @QueryParam("access_token") String access_token,
        @QueryParam("secret") String secret,
        @QueryParam("expires_in") @DefaultValue("4601") String expiresInString,
        @QueryParam("appArg0") String appArg0
            ) {
        return registerFederated(access_token, providerId, providerUserId, 
                secret, Integer.parseInt(expiresInString), appArg0);
    }
    
    @GET
    @Path("{providerId}")
    public Response registerFederatedGetPath(
        @PathParam("providerId") String providerId,
        @QueryParam("providerUserId") String providerUserId,
        @QueryParam("access_token") String access_token,
        @QueryParam("secret") String secret,
        @QueryParam("expires_in") @DefaultValue("4601") String expiresInString,
        @QueryParam("appArg0") String appArg0
            ) {
        return registerFederated(access_token, providerId, providerUserId, 
                secret, Integer.parseInt(expiresInString), appArg0);
    }

    @GET
    @Path("logout")
    public Response logout() throws URISyntaxException {
        NewCookie cookie = new NewCookie(OAuth2Filter.NAME_ACCESS_TOKEN, null, "/api", null, null, 0, false);
        return Response
                .temporaryRedirect(new URI("/"))
                .cookie(cookie)
                .build();
        
    }
    
    /**
     * 
     * @param access_token
     * @param providerId
     * @param providerUserId
     * @param secret
     * @param expiresInSeconds
     * @return the userId associated with the Connection, null if new Connection
     */
    protected Response registerFederated(
            String access_token, 
            String providerId,
            String providerUserId,
            String secret,
            Integer expiresInSeconds,
            String appArg0) {
        
        // use the connectionFactory
        final SocialTemplate socialTemplate = SocialTemplate.create(
                providerId, access_token, appArg0, null);

        SocialProfile profile = null;
        try {
            profile = socialTemplate.getProfile();

            if (null == profile) {
                throw new IllegalArgumentException("Invalid connection");
            }
        } catch (IOException unauthorized) {
            throw new IllegalArgumentException("Unauthorized federated side");
        }
        
        // providerUserId is optional, fetch it if necessary:
        final String realProviderUserId = profile.getId();
        if (null == providerUserId) {
            providerUserId = realProviderUserId;
        }
        else if (!providerUserId.equals(realProviderUserId)) {
            throw new IllegalArgumentException("Unauthorized federated side mismatch");
        }
        // load connection from db async style (likely case is new token for existing user)
        final Iterable<DConnection> conns = connectionDao.queryByProviderUserId(providerUserId);
        
        // extend short-lived token?
        if (expiresInSeconds < 4601) {
          SocialTemplate extendTemplate = SocialTemplate.create(providerId, null, socialTemplate.getBaseUrl(), null);
          DFactory client = factoryDao.findByPrimaryKey(providerId);
          if (null != client) {
            final Map.Entry<String, Integer> extended = extendTemplate.extend(providerId, client.getClientId(), client.getClientSecret(), access_token);
            if (null != extended) {
              access_token = extended.getKey();
              expiresInSeconds = extended.getValue();
            }
          }
        }
        
        // load existing conn for token
        DConnection conn = connectionDao.findByAccessToken(access_token);
        final boolean isNewConnection = (null == conn);
        Object userKey = null;

        // find other connections for this user, discard expired
        final ArrayList<Long> expiredTokens = new ArrayList<Long>();
        final Date now = new Date();
        for (DConnection dc : conns) {
            if (providerId.equals(dc.getProviderId())) {
                userKey = dc.getUserKey();

                // expired?
                if (null != dc.getExpireTime() && now.after(dc.getExpireTime())) {
                    expiredTokens.add(dc.getId());
                }
            }
        }
        final boolean isNewUser = (null == userKey);

        conn = createConnection(isNewConnection, isNewUser, profile, providerId, providerUserId,
          userKey, conn, access_token, secret, expiresInSeconds, appArg0, expiredTokens);

        NewCookie cookie = new NewCookie(OAuth2Filter.NAME_ACCESS_TOKEN, conn.getAccessToken(), "/api", null, null, expiresInSeconds, false);
        return Response.status(isNewUser ? Status.CREATED : Status.OK)
                .cookie(cookie)
                .entity(conn).build();
    }
    
    public static String convertRoles(Iterable<String> from) {
        if (null == from) {
            return null;
        }
        final StringBuffer to = new StringBuffer();
        boolean first = true;
        for (String s : from) {
            if (!first) {
                to.append(DConnection.ROLE_SEPARATOR);
            }
            to.append(s.trim());
            first = false;
        }
        return to.toString();
    }

    @Transactional
    protected DConnection createConnection(final boolean isNewConnection, final boolean isNewUser, SocialProfile profile, 
            String providerId, String providerUserId, Object userKey, DConnection conn, 
            String access_token, String secret, Integer expiresInSeconds, String appArg0, 
            final ArrayList<Long> expiredTokens) {
        
        DOAuth2User user = null;
        
        // create connection?
        if (isNewConnection) {

            // create user?
            if (isNewUser && autoCreateUser && null != userDao) {
                user = userDao.persist(null, profile.getDisplayName(), profile.getEmail(), 
                        profile.getProfileUrl(), null, profile.getThumbnailUrl());
                userKey = userDao.getPrimaryKey(user);
            }

            conn = new DConnection();
            conn.setAccessToken(access_token);
            conn.setDisplayName(profile.getDisplayName());
            conn.setProviderId(providerId);
            conn.setProviderUserId(providerUserId);
            conn.setSecret(secret);
            conn.setUserKey(userKey);
            if (null != expiresInSeconds) {
                conn.setExpireTime(new Date(System.currentTimeMillis() + expiresInSeconds*1000L));
            }
            connectionDao.persist(conn);
        }
        else {
            userKey = conn.getUserKey();
        }
        // update connection values
        conn.setAppArg0(appArg0);
        if (null != userDao) {

            // existing user
            if (null == user) {
                user = userDao.findByPrimaryKey(userKey);
            }

            // copy roles to Connection
            if (null != user) {
                Collection<String> userRoles = user.getRoles();
                conn.setUserRoles(convertRoles(userRoles));

              // update thumbnail Url
              user.setThumbnailUrl(profile.getThumbnailUrl());
              userDao.update(user);
            }
            LOGGER.debug("Roles set to {} from user {}", conn.getUserRoles(), user);
        }
        connectionDao.update(conn);
        connectionDao.delete(userKey, expiredTokens);
        return conn;
    }
}
