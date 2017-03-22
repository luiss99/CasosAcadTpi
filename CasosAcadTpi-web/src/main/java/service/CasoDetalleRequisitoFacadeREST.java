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
import uesocc.tpi.lib.CasoDetalleRequisito;
import uesocc.tpi.lib.CasoDetalleRequisitoPK;

/**
 *
 * @author Kira Luis
 */
@Stateless
@Path("uesocc.tpi.lib.casodetallerequisito")
public class CasoDetalleRequisitoFacadeREST extends AbstractFacade<CasoDetalleRequisito> {

    @PersistenceContext(unitName = "PU")
    private EntityManager em;

    private CasoDetalleRequisitoPK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;idCasoDetalleRequisito=idCasoDetalleRequisitoValue;idCasoDetalle=idCasoDetalleValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        uesocc.tpi.lib.CasoDetalleRequisitoPK key = new uesocc.tpi.lib.CasoDetalleRequisitoPK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> idCasoDetalleRequisito = map.get("idCasoDetalleRequisito");
        if (idCasoDetalleRequisito != null && !idCasoDetalleRequisito.isEmpty()) {
            key.setIdCasoDetalleRequisito(new java.lang.Integer(idCasoDetalleRequisito.get(0)));
        }
        java.util.List<String> idCasoDetalle = map.get("idCasoDetalle");
        if (idCasoDetalle != null && !idCasoDetalle.isEmpty()) {
            key.setIdCasoDetalle(new java.lang.Integer(idCasoDetalle.get(0)));
        }
        return key;
    }

    public CasoDetalleRequisitoFacadeREST() {
        super(CasoDetalleRequisito.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(CasoDetalleRequisito entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") PathSegment id, CasoDetalleRequisito entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.CasoDetalleRequisitoPK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CasoDetalleRequisito find(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.CasoDetalleRequisitoPK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<CasoDetalleRequisito> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<CasoDetalleRequisito> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
