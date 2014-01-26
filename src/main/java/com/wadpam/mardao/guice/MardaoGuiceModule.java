package com.wadpam.mardao.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.wadpam.mardao.oauth.dao.DConnectionDao;
import com.wadpam.mardao.oauth.dao.DConnectionDaoBean;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDao;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDaoBean;

/**
 * Binds {@link UnitOfWork}, {@link PersistService} and {@link MardaoTransactionManager}.
 *
 * @author osandstrom 
 * Date: 1/19/14 Time: 8:59 PM
 */
public class MardaoGuiceModule extends AbstractModule {
    
    public static final String JERSEY_PACKAGES = "com.wadpam.mardao.oauth.api";

    @Override
    protected void configure() {
          bind(UnitOfWork.class).to(MardaoGuiceUnitOfWork.class);
          bind(PersistService.class).to(MardaoGuicePersistService.class);
          
          bind(DConnectionDao.class).to(DConnectionDaoBean.class);
          bind(DOAuth2UserDao.class).to(DOAuth2UserDaoBean.class);

          MardaoTransactionManager transactionManager = new MardaoTransactionManager();
          requestInjection(transactionManager);
          bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionManager);
    }
}
