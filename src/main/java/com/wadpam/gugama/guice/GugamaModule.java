package com.wadpam.gugama.guice;

import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.servlet.ServletModule;
import com.wadpam.gugama.oauth.api.DConnectionServlet;
import com.wadpam.gugama.oauth.api.DOAuth2UserServlet;
import com.wadpam.gugama.oauth.api.OAuth2Servlet;
import com.wadpam.gugama.oauth.dao.DConnectionDao;
import com.wadpam.gugama.oauth.dao.DConnectionDaoBean;
import com.wadpam.gugama.oauth.dao.DFactoryDao;
import com.wadpam.gugama.oauth.dao.DFactoryDaoBean;
import com.wadpam.gugama.oauth.dao.DOAuth2UserDao;
import com.wadpam.gugama.oauth.dao.DOAuth2UserDaoBean;
import com.wadpam.gugama.oauth.web.OAuth2Filter;

/**
 * Binds {@link UnitOfWork}, {@link PersistService} and {@link MardaoTransactionManager}.
 *
 * @author osandstrom 
 * Date: 1/19/14 Time: 8:59 PM
 */
public class GugamaModule extends ServletModule {
    
    @Override
    protected void configureServlets() {
          bind(UnitOfWork.class).to(MardaoGuiceUnitOfWork.class);
          bind(PersistService.class).to(MardaoGuicePersistService.class);
          
          bind(DConnectionDao.class).to(DConnectionDaoBean.class);
          bind(DOAuth2UserDao.class).to(DOAuth2UserDaoBean.class);
          bind(DFactoryDao.class).to(DFactoryDaoBean.class);

          filter("/api/*").through(OAuth2Filter.class);
          
          serve("/api/connection*").with(DConnectionServlet.class);
          serve("/api/oauth2user*").with(DOAuth2UserServlet.class);
          serve("/oauth/*").with(OAuth2Servlet.class);
          
          MardaoTransactionManager transactionManager = new MardaoTransactionManager();
          requestInjection(transactionManager);
          bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionManager);
    }
}
