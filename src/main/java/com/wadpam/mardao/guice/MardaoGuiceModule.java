package com.wadpam.mardao.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.wadpam.mardao.oauth.api.DConnectionResource;
import com.wadpam.mardao.oauth.api.DFactoryResource;
import com.wadpam.mardao.oauth.api.DOAuth2UserResource;
import com.wadpam.mardao.oauth.api.OAuth2Resource;
import com.wadpam.mardao.oauth.dao.DConnectionDaoBean;
import com.wadpam.mardao.oauth.dao.DFactoryDaoBean;
import com.wadpam.mardao.oauth.dao.DOAuth2UserDaoBean;

import net.sf.mardao.dao.Supplier;

/**
 * Binds {@link UnitOfWork}, {@link PersistService} and {@link MardaoTransactionManager}.
 *
 * @author osandstrom 
 * Date: 1/19/14 Time: 8:59 PM
 */
public class MardaoGuiceModule extends AbstractModule {
    
    @Override
    protected void configure() {
          bind(UnitOfWork.class).to(MardaoGuiceUnitOfWork.class);
          bind(PersistService.class).to(MardaoGuicePersistService.class);
          
          bind(DConnectionDaoBean.class);
          bind(DOAuth2UserDaoBean.class);
          bind(DFactoryDaoBean.class);

          bind(DConnectionResource.class);
          bind(DFactoryResource.class);
          bind(DOAuth2UserResource.class);
          bind(OAuth2Resource.class);

          MardaoTransactionManager transactionManager = new MardaoTransactionManager(getProvider(Supplier.class));
          requestInjection(transactionManager);
          bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionManager);
    }
}
