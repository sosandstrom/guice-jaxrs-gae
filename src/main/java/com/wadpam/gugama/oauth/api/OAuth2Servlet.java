/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.gugama.oauth.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.wadpam.gugama.crud.CrudServlet;
import com.wadpam.gugama.oauth.dao.DConnectionDao;
import com.wadpam.gugama.oauth.dao.DOAuth2UserDao;
import com.wadpam.gugama.oauth.domain.DConnection;
import com.wadpam.gugama.oauth.domain.DOAuth2User;
import com.wadpam.gugama.oauth.web.OAuth2Filter;
import com.wadpam.gugama.social.SocialProfile;
import com.wadpam.gugama.social.SocialTemplate;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osandstrom
 */
@Singleton
public class OAuth2Servlet extends CrudServlet<DConnection, String> {
    private static final boolean autoCreateUser = true;
    static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Servlet.class);
    
    private final DConnectionDao connectionDao;
    private final DOAuth2UserDao userDao;
    
    @Inject
    public OAuth2Servlet(DConnectionDao connectionDao, DOAuth2UserDao userDao) {
        super(DConnection.class, String.class, "/federated/");
        this.connectionDao = connectionDao;
        this.userDao = userDao;
    }

    @Override
    protected DConnection get(HttpServletRequest request, String providerId) {
        final String providerUserId = request.getParameter("providerUserId");
        final String access_token = request.getParameter("access_token");
        final String secret = request.getParameter("secret");
        final String expiresInString = getParameter(request, "expires_in", "3600");
        final String appArg0 = request.getParameter("appArg0");
        
        Map.Entry<Integer, DConnection> response = registerFederated(access_token, providerId, providerUserId, 
                                                secret, Integer.parseInt(expiresInString), appArg0);
        return response.getValue();
    }
    
    
    /**
     * 
     * @param access_token
     * @param providerId
     * @param providerUserId
     * @param secret
     * @param expires_in
     * @return the userId associated with the Connection, null if new Connection
     */
    protected Map.Entry<Integer, DConnection> registerFederated(
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

        conn = createConnection(isNewConnection, isNewUser, profile, providerId, providerUserId, userKey, conn, access_token, secret, expiresInSeconds, appArg0, expiredTokens);

        addCookie(OAuth2Filter.NAME_ACCESS_TOKEN, conn.getAccessToken(), "/api", expiresInSeconds);
        return new AbstractMap.SimpleImmutableEntry<>(isNewUser ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK, conn);
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
                        profile.getProfileUrl(), null, null);
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
            }
            LOGGER.debug("Roles set to {} from user {}", conn.getUserRoles(), user);
        }
        connectionDao.update(conn);
        connectionDao.delete(userKey, expiredTokens);
        return conn;
    }
}
