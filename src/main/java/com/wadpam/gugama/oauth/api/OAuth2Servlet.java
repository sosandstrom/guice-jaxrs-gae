/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.gugama.oauth.api;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.wadpam.gugama.crud.CrudServlet;
import com.wadpam.gugama.oauth.dao.DConnectionDao;
import com.wadpam.gugama.oauth.dao.DFactoryDao;
import com.wadpam.gugama.oauth.dao.DOAuth2UserDao;
import com.wadpam.gugama.oauth.domain.DConnection;
import com.wadpam.gugama.oauth.domain.DFactory;
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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.mardao.core.dao.DaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osandstrom
 */
@Singleton
public class OAuth2Servlet extends HttpServlet {
    private static final boolean autoCreateUser = true;
    public static final String NAME_REGISTER_URI = "OAuth2Servlet.register_uri";
    static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Servlet.class);
    
    private final DConnectionDao connectionDao;
    private final DFactoryDao factoryDao;
    private final DOAuth2UserDao userDao;
    private String registerUri;
    
    @Inject
    public OAuth2Servlet(DConnectionDao connectionDao, DFactoryDao factoryDao, 
            DOAuth2UserDao userDao) {
        this.connectionDao = connectionDao;
        this.factoryDao = factoryDao;
        this.userDao = userDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String providerId = CrudServlet.getFileString("/federated/", request.getRequestURI());
        final String code = request.getParameter("code");
        if (null != code) {
            StringBuilder sb = new StringBuilder()
                    .append(request.getScheme())
                    .append("://")
                    .append(request.getHeader("Host"))
                    .append(request.getRequestURI());
            String redirectURI = exchangeCode(response, providerId, code, sb.toString());
            CrudServlet.writeResponse(response, HttpServletResponse.SC_MOVED_TEMPORARILY, redirectURI);
        }
        else {
            CrudServlet.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String providerId = CrudServlet.getFileString("/federated/", request.getRequestURI());
        final String providerUserId = request.getParameter("providerUserId");
        final String access_token = request.getParameter("access_token");
        final String secret = request.getParameter("secret");
        final String expiresInString = CrudServlet.getParameter(request, "expires_in", "3600");
        final String appArg0 = request.getParameter("appArg0");

        Map.Entry<Integer, DConnection> res = registerFederated(response, access_token, providerId, providerUserId, 
                                                secret, Integer.parseInt(expiresInString), appArg0);
        
        CrudServlet.writeResponse(response, res.getKey(), res.getValue());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        if (SystemProperty.Environment.Value.Development == SystemProperty.environment.value()) {
            factoryDao.persist(SocialTemplate.PROVIDER_ID_FACEBOOK, "https://graph.facebook.com", "1392532967689787", "06ae8184f58bb9348b2d8404b05879cb");
        }
        
        registerUri = config.getInitParameter(NAME_REGISTER_URI);
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
            HttpServletResponse response,
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

        CrudServlet.addCookie(response, OAuth2Filter.NAME_ACCESS_TOKEN, conn.getAccessToken(), "/api", expiresInSeconds);
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
        DaoImpl.setPrincipalName(profile.getDisplayName());
 
        
        // create connection?
        if (isNewConnection) {

            // create user?
            if (isNewUser && autoCreateUser && null != userDao) {
                user = userDao.persist(null, profile.getDisplayName(), profile.getEmail(), 
                        profile.getProfileUrl(), null, null);
                LOGGER.debug("Created OAuth2User for {}, id={}", profile.getDisplayName(), user.getId());
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

    protected String exchangeCode(HttpServletResponse response, String providerId, 
            String code, String redirectURI) throws ServletException, IOException {
        // load factory
        DFactory factory = factoryDao.findByPrimaryKey(providerId);
        if (null == factory) {
            throw new ServletException("No such provider " + providerId);
        }
        
        SocialTemplate template = SocialTemplate.create(providerId, factory.getClientId(), factory.getClientSecret());
        Map.Entry<String, Integer> tokenExpires = template.exchangeCode(code, redirectURI);
        Map.Entry<Integer, DConnection> statusConnection = 
                registerFederated(response, tokenExpires.getKey(), providerId, null, null, tokenExpires.getValue(), null);
        return HttpServletResponse.SC_CREATED == statusConnection.getKey() ?
                registerUri : "/home.html";
    }
}
