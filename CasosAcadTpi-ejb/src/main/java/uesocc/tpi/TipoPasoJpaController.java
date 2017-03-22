/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import uesocc.tpi.lib.Paso;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.TipoPaso;

/**
 *
 * @author Kira Luis
 */
public class TipoPasoJpaController implements Serializable {

    public TipoPasoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TipoPaso tipoPaso) throws PreexistingEntityException, Exception {
        if (tipoPaso.getPasoList() == null) {
            tipoPaso.setPasoList(new ArrayList<Paso>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Paso> attachedPasoList = new ArrayList<Paso>();
            for (Paso pasoListPasoToAttach : tipoPaso.getPasoList()) {
                pasoListPasoToAttach = em.getReference(pasoListPasoToAttach.getClass(), pasoListPasoToAttach.getPasoPK());
                attachedPasoList.add(pasoListPasoToAttach);
            }
            tipoPaso.setPasoList(attachedPasoList);
            em.persist(tipoPaso);
            for (Paso pasoListPaso : tipoPaso.getPasoList()) {
                TipoPaso oldTipoPasoOfPasoListPaso = pasoListPaso.getTipoPaso();
                pasoListPaso.setTipoPaso(tipoPaso);
                pasoListPaso = em.merge(pasoListPaso);
                if (oldTipoPasoOfPasoListPaso != null) {
                    oldTipoPasoOfPasoListPaso.getPasoList().remove(pasoListPaso);
                    oldTipoPasoOfPasoListPaso = em.merge(oldTipoPasoOfPasoListPaso);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTipoPaso(tipoPaso.getIdTipoPaso()) != null) {
                throw new PreexistingEntityException("TipoPaso " + tipoPaso + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TipoPaso tipoPaso) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoPaso persistentTipoPaso = em.find(TipoPaso.class, tipoPaso.getIdTipoPaso());
            List<Paso> pasoListOld = persistentTipoPaso.getPasoList();
            List<Paso> pasoListNew = tipoPaso.getPasoList();
            List<String> illegalOrphanMessages = null;
            for (Paso pasoListOldPaso : pasoListOld) {
                if (!pasoListNew.contains(pasoListOldPaso)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Paso " + pasoListOldPaso + " since its tipoPaso field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Paso> attachedPasoListNew = new ArrayList<Paso>();
            for (Paso pasoListNewPasoToAttach : pasoListNew) {
                pasoListNewPasoToAttach = em.getReference(pasoListNewPasoToAttach.getClass(), pasoListNewPasoToAttach.getPasoPK());
                attachedPasoListNew.add(pasoListNewPasoToAttach);
            }
            pasoListNew = attachedPasoListNew;
            tipoPaso.setPasoList(pasoListNew);
            tipoPaso = em.merge(tipoPaso);
            for (Paso pasoListNewPaso : pasoListNew) {
                if (!pasoListOld.contains(pasoListNewPaso)) {
                    TipoPaso oldTipoPasoOfPasoListNewPaso = pasoListNewPaso.getTipoPaso();
                    pasoListNewPaso.setTipoPaso(tipoPaso);
                    pasoListNewPaso = em.merge(pasoListNewPaso);
                    if (oldTipoPasoOfPasoListNewPaso != null && !oldTipoPasoOfPasoListNewPaso.equals(tipoPaso)) {
                        oldTipoPasoOfPasoListNewPaso.getPasoList().remove(pasoListNewPaso);
                        oldTipoPasoOfPasoListNewPaso = em.merge(oldTipoPasoOfPasoListNewPaso);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tipoPaso.getIdTipoPaso();
                if (findTipoPaso(id) == null) {
                    throw new NonexistentEntityException("The tipoPaso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoPaso tipoPaso;
            try {
                tipoPaso = em.getReference(TipoPaso.class, id);
                tipoPaso.getIdTipoPaso();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoPaso with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Paso> pasoListOrphanCheck = tipoPaso.getPasoList();
            for (Paso pasoListOrphanCheckPaso : pasoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TipoPaso (" + tipoPaso + ") cannot be destroyed since the Paso " + pasoListOrphanCheckPaso + " in its pasoList field has a non-nullable tipoPaso field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tipoPaso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TipoPaso> findTipoPasoEntities() {
        return findTipoPasoEntities(true, -1, -1);
    }

    public List<TipoPaso> findTipoPasoEntities(int maxResults, int firstResult) {
        return findTipoPasoEntities(false, maxResults, firstResult);
    }

    private List<TipoPaso> findTipoPasoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TipoPaso.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public TipoPaso findTipoPaso(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TipoPaso.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoPasoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TipoPaso> rt = cq.from(TipoPaso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
