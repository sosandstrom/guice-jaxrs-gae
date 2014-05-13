/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.mardao.oauth.web;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.wadpam.mardao.oauth.dao.DConnectionDao;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDao;
import com.wadpam.mardao.oauth.domain.DConnection;
import java.io.IOException;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
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
public class OAuth2Filter implements Filter {
    public static final String NAME_ACCESS_TOKEN = "access_token";
    public static final String NAME_USER_ID = "oauth2user.id";
    public static final String NAME_USER_KEY = "oauth2user.key";
    public static final String NAME_CONNECTION = "oauth2connection";
    
    static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Filter.class);

    private final Provider<DConnectionDao> connectionDaoProvider;
    private final Provider<DOAuth2UserDao> userDaoProvider;

    @Inject
    public OAuth2Filter(Provider<DConnectionDao> connectionDaoProvider, 
            Provider<DOAuth2UserDao> userDaoProvider) {
        this.connectionDaoProvider = connectionDaoProvider;
        this.userDaoProvider = userDaoProvider;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        final String accessToken = getAccessToken(request);
        if (null == accessToken) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        final DConnection conn = verifyAccessToken(accessToken);
        if (null == conn) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        request.setAttribute(NAME_ACCESS_TOKEN, accessToken);
        request.setAttribute(NAME_CONNECTION, conn);
        request.setAttribute(NAME_USER_KEY, conn.getUserKey());
        Long userId = userDaoProvider.get().getSimpleKeyByPrimaryKey(conn.getUserKey());
        request.setAttribute(NAME_USER_ID, userId);
        DaoImpl.setPrincipalName(null != userId ? userId.toString() : null);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private static String getAccessToken(HttpServletRequest request) {
        String accessToken = request.getParameter(NAME_ACCESS_TOKEN);
        // check for cookie:
        if (null == accessToken && null != request.getCookies()) {
            for (Cookie c : request.getCookies()) {
                if (NAME_ACCESS_TOKEN.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        
        return accessToken;
    }
    
    public static Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(NAME_USER_ID);
    }

    private DConnection verifyAccessToken(String accessToken) {
        final DConnection conn = connectionDaoProvider.get().findByAccessToken(accessToken);
        if (null == conn) {
            LOGGER.debug("No such access_token {}", accessToken);
            return null;
        }
        
        // expired?
        if (null != conn.getExpireTime() && conn.getExpireTime().before(new Date())) {
            LOGGER.debug("access_token expired {}", conn.getExpireTime());
            return null;
        }
        
        return conn;
    }
    
}
