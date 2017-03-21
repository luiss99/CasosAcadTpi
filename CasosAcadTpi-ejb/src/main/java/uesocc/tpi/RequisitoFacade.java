/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import uesocc.tpi.lib.Requisito;

/**
 *
 * @author Kira Luis
 */
@Stateless
public class RequisitoFacade extends AbstractFacade<Requisito> implements RequisitoFacadeLocal {

    @PersistenceContext(unitName = "Pu")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RequisitoFacade() {
        super(Requisito.class);
    }
    
}
