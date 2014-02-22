package com.wadpam.mardao.crud;

import com.google.inject.persist.Transactional;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.mardao.core.CursorPage;
import net.sf.mardao.core.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map this Resource with a
 *
 * @Path("path/{parentId}/path").
 * @author osandstrom
 */
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class ParentedCrudResource<PT, PID extends Serializable, P extends Dao<PT, PID>, T, ID extends Serializable, D extends Dao<T, ID>> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CrudResource.class);
    protected final D dao;
    protected final P parentDao;

    public ParentedCrudResource(P parentDao, D dao) {
        this.parentDao = parentDao;
        this.dao = dao;
    }

    @POST
    @Transactional
    public Response create(@PathParam("parentId") PID parentId, T entity) throws URISyntaxException {
        // Objects such as parentKey cannot be properly JSONed:
        final Object parentKey = parentDao.getPrimaryKey(null, parentId);
        dao.setParentKey(entity, parentKey);

        final ID id = dao.persist(entity);
        URI uri = new URI(id.toString());
        return Response.created(uri).entity(id).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("parentId") PID parentId, @PathParam("id") ID id) {
        final Object parentKey = parentDao.getPrimaryKey(null, parentId);
        final boolean found = dao.delete(parentKey, id);

        if (!found) {
            // app engine always returns false...
//            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    @GET
    @Path("{id}")
    public Response read(@PathParam("parentId") PID parentId, @PathParam("id") ID id) {
        final Object parentKey = parentDao.getPrimaryKey(null, parentId);
        final T entity = dao.findByPrimaryKey(parentKey, id);
        if (null == entity) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entity).build();
    }

    @GET
    public Response readPage(@PathParam("parentId") PID parentId,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("cursorKey") String cursorKey) {
        final Object parentKey = parentDao.getPrimaryKey(null, parentId);
        final CursorPage<T> page = dao.queryPage(parentKey, pageSize, cursorKey);
        return Response.ok(page).build();
    }

    @POST
    @Path("{id}")
    public Response update(@PathParam("parentId") PID parentId, @PathParam("id") ID id, T entity) throws URISyntaxException {
        // Objects such as parentKey cannot be properly JSONed:
        final Object parentKey = parentDao.getPrimaryKey(null, parentId);
        dao.setParentKey(entity, parentKey);

        final ID eId = (ID) dao.getPrimaryKey(entity);
        if (!id.equals(eId)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        dao.update(entity);
        URI uri = new URI(id.toString());
        return Response.ok().contentLocation(uri).build();
    }
}
