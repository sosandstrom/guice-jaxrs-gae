package com.wadpam.mardao.oauth.api;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.wadpam.mardao.crud.CrudResource;
import com.wadpam.mardao.oauth.dao.DConnectionDao;
import com.wadpam.mardao.oauth.domain.DConnection;


/**
 * Created with IntelliJ IDEA.
 *
 * @author osandstrom
 * Date: 1/18/14 Time: 8:07 PM
 */
@Path("api/connection")
@Produces(MediaType.APPLICATION_JSON)
public class DConnectionResource extends CrudResource<DConnection, Long, DConnectionDao> {

  @Inject
  public DConnectionResource(DConnectionDao dao) {
    super(dao);
    LOGGER.info("<init>");
  }

}
