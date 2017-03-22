/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import uesocc.tpi.lib.CasoDetalle;
import uesocc.tpi.lib.CasoDetallePK;

/**
 *
 * @author Kira Luis
 */
@Stateless
@Path("uesocc.tpi.lib.casodetalle")
public class CasoDetalleFacadeREST extends AbstractFacade<CasoDetalle> {

    @PersistenceContext(unitName = "PU")
    private EntityManager em;

    private CasoDetallePK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;idCasoDetalle=idCasoDetalleValue;idCaso=idCasoValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        uesocc.tpi.lib.CasoDetallePK key = new uesocc.tpi.lib.CasoDetallePK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> idCasoDetalle = map.get("idCasoDetalle");
        if (idCasoDetalle != null && !idCasoDetalle.isEmpty()) {
            key.setIdCasoDetalle(new java.lang.Integer(idCasoDetalle.get(0)));
        }
        java.util.List<String> idCaso = map.get("idCaso");
        if (idCaso != null && !idCaso.isEmpty()) {
            key.setIdCaso(new java.lang.Integer(idCaso.get(0)));
        }
        return key;
    }

    public CasoDetalleFacadeREST() {
        super(CasoDetalle.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(CasoDetalle entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") PathSegment id, CasoDetalle entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.CasoDetallePK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CasoDetalle find(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.CasoDetallePK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<CasoDetalle> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<CasoDetalle> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
