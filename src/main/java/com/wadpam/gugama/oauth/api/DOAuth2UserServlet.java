package com.wadpam.gugama.oauth.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.wadpam.gugama.crud.MardaoCrudServlet;
import com.wadpam.gugama.oauth.dao.DOAuth2UserDao;
import com.wadpam.gugama.oauth.domain.DOAuth2User;

/**
 * This is the REST API implementation for entity {@link DOAuth2User}.
 * @author osandstrom
 */
@Singleton
public class DOAuth2UserServlet extends MardaoCrudServlet<DOAuth2User, Long, DOAuth2UserDao>{

    @Inject
    public DOAuth2UserServlet(DOAuth2UserDao dao) {
        super(DOAuth2User.class, Long.class, dao, "/user/");
    }
    
}
