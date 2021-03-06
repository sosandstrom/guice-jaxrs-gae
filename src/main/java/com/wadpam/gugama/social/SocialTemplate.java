/*
 * INSERT COPYRIGHT HERE
 */

package com.wadpam.gugama.social;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.wadpam.gugama.oauth.domain.DConnection;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;


/**
 *
 * @author sosandstrom
 */
public class SocialTemplate extends NetworkTemplate {

    public static final String BASE_URL_FACEBOOK = "https://graph.facebook.com";
    public static final String PROVIDER_ID_FACEBOOK = "facebook";

    protected final String access_token;
    protected final String clientId;
    protected final String clientSecret;

    public SocialTemplate(String access_token) {
        this(access_token, BASE_URL_FACEBOOK);
    }
    
    public SocialTemplate(String access_token, String baseUrl) {
        super(baseUrl);
        this.access_token = access_token;
        this.clientId = null;
        this.clientSecret = null;
    }
    
    public SocialTemplate(String clientId, String clientSecret, String baseUrl) {
        super(baseUrl);
        this.access_token = null;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    
    public static SocialTemplate create(String providerId, String access_token, 
            String baseUrl, String domain) {
        if (PROVIDER_ID_FACEBOOK.equals(providerId)) {
            return new SocialTemplate(access_token);
        }
//        if ("itest".equals(providerId) && "itest".equals(domain)) {
//            return new ITestTemplate(access_token);
//        }
        throw new IllegalArgumentException(String.format("No such provider %s.", providerId));
    }
    
    public static SocialTemplate create(String providerId, String clientId, String clientSecret) {
        if (PROVIDER_ID_FACEBOOK.equals(providerId)) {
            return new SocialTemplate(clientId, clientSecret, BASE_URL_FACEBOOK);
        }
        throw new IllegalArgumentException(String.format("No such provider %s.", providerId));
    }
    
    public Map.Entry<String,Integer> exchangeCode(String code, String redirectURI) throws IOException {
        if (BASE_URL_FACEBOOK.equals(getBaseUrl())) {
            Map<String,String> requestBody = ImmutableMap.of(
                    "client_id", clientId,  
                    "client_secret", clientSecret,
                    "code", code,
                    "redirect_uri", redirectURI);

            String res = get(getBaseUrl() + "/oauth/access_token", String.class, requestBody);
            if (null == res) {
                throw new IOException("Malformed code response");
            }
            
            Map<String, String> resMap = NetworkTemplate.parseQueryString(res);
            return new AbstractMap.SimpleImmutableEntry<>(
                    resMap.get("access_token"), Integer.parseInt(resMap.get("expires")));
        }
        
        throw new IllegalArgumentException(String.format("No such provider for %s.", getBaseUrl()));
    }

    public SocialProfile getProfile() throws IOException {
        Map<String, Object> props = get(getBaseUrl() + "/me", Map.class);
        return parseProfile(props);
    }

    @Override
    public <J> J exchange(String method, String url, 
            Map<String,String> requestHeaders, 
            Object requestBody, Class<J> responseClass) {
        
        // OAuth access_token
        if (null != access_token) {
            url = String.format("%s%saccess_token=%s",
                    url, url.contains("?") ? "&" : "?", access_token);
        }
        
        return super.exchange(method, url, 
                requestHeaders, requestBody, responseClass);
    }
    
    /**
     * Property names for Facebook - Override to customize
     * @param props
     * @return 
     */
    protected SocialProfile parseProfile(Map<String, Object> props) {
        if (!props.containsKey("id")) {
            throw new IllegalArgumentException("No id in profile");
        }
        return SocialProfile.with(props)
                .displayName("name")
                .first("first_name")
                .last("last_name")
                .id("id")
                .username("username")
                .profileUrl("link")
                .build();
    }
}
