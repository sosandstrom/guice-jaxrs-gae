/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wadpam.mardao.oauth.api;

import com.google.inject.Inject;
import com.wadpam.mardao.crud.CrudResource;
import com.wadpam.mardao.oauth.dao.DFactoryDaoBean;
import com.wadpam.mardao.oauth.domain.DFactory;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author osandstrom
 */
@Path("_adm/DFactory")
@Produces(MediaType.APPLICATION_JSON)
public class DFactoryResource extends CrudResource<DFactory, String, DFactoryDaoBean> {

    @Inject
    public DFactoryResource(DFactoryDaoBean dao) {
        super(dao);
    }
    
}
