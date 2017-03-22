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
import uesocc.tpi.lib.PasoRequisito;
import uesocc.tpi.lib.PasoRequisitoPK;

/**
 *
 * @author Kira Luis
 */
@Stateless
@Path("uesocc.tpi.lib.pasorequisito")
public class PasoRequisitoFacadeREST extends AbstractFacade<PasoRequisito> {

    @PersistenceContext(unitName = "PU")
    private EntityManager em;

    private PasoRequisitoPK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;idPaso=idPasoValue;idRequisito=idRequisitoValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        uesocc.tpi.lib.PasoRequisitoPK key = new uesocc.tpi.lib.PasoRequisitoPK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> idPaso = map.get("idPaso");
        if (idPaso != null && !idPaso.isEmpty()) {
            key.setIdPaso(new java.lang.Integer(idPaso.get(0)));
        }
        java.util.List<String> idRequisito = map.get("idRequisito");
        if (idRequisito != null && !idRequisito.isEmpty()) {
            key.setIdRequisito(new java.lang.Integer(idRequisito.get(0)));
        }
        return key;
    }

    public PasoRequisitoFacadeREST() {
        super(PasoRequisito.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(PasoRequisito entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") PathSegment id, PasoRequisito entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.PasoRequisitoPK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PasoRequisito find(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.PasoRequisitoPK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PasoRequisito> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PasoRequisito> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
