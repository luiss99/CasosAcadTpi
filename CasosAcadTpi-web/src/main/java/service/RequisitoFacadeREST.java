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
import uesocc.tpi.lib.Requisito;
import uesocc.tpi.lib.RequisitoPK;

/**
 *
 * @author Kira Luis
 */
@Stateless
@Path("uesocc.tpi.lib.requisito")
public class RequisitoFacadeREST extends AbstractFacade<Requisito> {

    @PersistenceContext(unitName = "PU")
    private EntityManager em;

    private RequisitoPK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;idRequisito=idRequisitoValue;idTipoRequisito=idTipoRequisitoValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        uesocc.tpi.lib.RequisitoPK key = new uesocc.tpi.lib.RequisitoPK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> idRequisito = map.get("idRequisito");
        if (idRequisito != null && !idRequisito.isEmpty()) {
            key.setIdRequisito(new java.lang.Integer(idRequisito.get(0)));
        }
        java.util.List<String> idTipoRequisito = map.get("idTipoRequisito");
        if (idTipoRequisito != null && !idTipoRequisito.isEmpty()) {
            key.setIdTipoRequisito(new java.lang.Integer(idTipoRequisito.get(0)));
        }
        return key;
    }

    public RequisitoFacadeREST() {
        super(Requisito.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(Requisito entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") PathSegment id, Requisito entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.RequisitoPK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Requisito find(@PathParam("id") PathSegment id) {
        uesocc.tpi.lib.RequisitoPK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Requisito> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Requisito> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
