/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import uesocc.tpi.lib.ProcesoDetalle;

/**
 *
 * @author Kira Luis
 */
@Stateless
public class ProcesoDetalleFacade extends AbstractFacade<ProcesoDetalle> {

    @PersistenceContext(unitName = "PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProcesoDetalleFacade() {
        super(ProcesoDetalle.class);
    }
    
}
