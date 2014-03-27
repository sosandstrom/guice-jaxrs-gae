package com.wadpam.gugama.oauth.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.wadpam.gugama.crud.MardaoCrudServlet;
import com.wadpam.gugama.oauth.dao.DConnectionDao;
import com.wadpam.gugama.oauth.domain.DConnection;

/**
 * This is the REST API implementation for entity {@link DConnection}.
 * @author osandstrom
 */
@Singleton
public class DConnectionServlet extends MardaoCrudServlet<DConnection, Long, DConnectionDao>{

    @Inject
    public DConnectionServlet(DConnectionDao dao) {
        super(DConnection.class, Long.class, dao, "/connection/");
    }
    
}
