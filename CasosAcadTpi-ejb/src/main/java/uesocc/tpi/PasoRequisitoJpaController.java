/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Paso;
import uesocc.tpi.lib.PasoRequisito;
import uesocc.tpi.lib.PasoRequisitoPK;
import uesocc.tpi.lib.Requisito;

/**
 *
 * @author Kira Luis
 */
public class PasoRequisitoJpaController implements Serializable {

    public PasoRequisitoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PasoRequisito pasoRequisito) throws PreexistingEntityException, Exception {
        if (pasoRequisito.getPasoRequisitoPK() == null) {
            pasoRequisito.setPasoRequisitoPK(new PasoRequisitoPK());
        }
        pasoRequisito.getPasoRequisitoPK().setIdRequisito(pasoRequisito.getRequisito().getRequisitoPK().getIdRequisito());
        pasoRequisito.getPasoRequisitoPK().setIdPaso(pasoRequisito.getPaso().getPasoPK().getIdPaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Paso paso = pasoRequisito.getPaso();
            if (paso != null) {
                paso = em.getReference(paso.getClass(), paso.getPasoPK());
                pasoRequisito.setPaso(paso);
            }
            Requisito requisito = pasoRequisito.getRequisito();
            if (requisito != null) {
                requisito = em.getReference(requisito.getClass(), requisito.getRequisitoPK());
                pasoRequisito.setRequisito(requisito);
            }
            em.persist(pasoRequisito);
            if (paso != null) {
                paso.getPasoRequisitoList().add(pasoRequisito);
                paso = em.merge(paso);
            }
            if (requisito != null) {
                requisito.getPasoRequisitoList().add(pasoRequisito);
                requisito = em.merge(requisito);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPasoRequisito(pasoRequisito.getPasoRequisitoPK()) != null) {
                throw new PreexistingEntityException("PasoRequisito " + pasoRequisito + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PasoRequisito pasoRequisito) throws NonexistentEntityException, Exception {
        pasoRequisito.getPasoRequisitoPK().setIdRequisito(pasoRequisito.getRequisito().getRequisitoPK().getIdRequisito());
        pasoRequisito.getPasoRequisitoPK().setIdPaso(pasoRequisito.getPaso().getPasoPK().getIdPaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PasoRequisito persistentPasoRequisito = em.find(PasoRequisito.class, pasoRequisito.getPasoRequisitoPK());
            Paso pasoOld = persistentPasoRequisito.getPaso();
            Paso pasoNew = pasoRequisito.getPaso();
            Requisito requisitoOld = persistentPasoRequisito.getRequisito();
            Requisito requisitoNew = pasoRequisito.getRequisito();
            if (pasoNew != null) {
                pasoNew = em.getReference(pasoNew.getClass(), pasoNew.getPasoPK());
                pasoRequisito.setPaso(pasoNew);
            }
            if (requisitoNew != null) {
                requisitoNew = em.getReference(requisitoNew.getClass(), requisitoNew.getRequisitoPK());
                pasoRequisito.setRequisito(requisitoNew);
            }
            pasoRequisito = em.merge(pasoRequisito);
            if (pasoOld != null && !pasoOld.equals(pasoNew)) {
                pasoOld.getPasoRequisitoList().remove(pasoRequisito);
                pasoOld = em.merge(pasoOld);
            }
            if (pasoNew != null && !pasoNew.equals(pasoOld)) {
                pasoNew.getPasoRequisitoList().add(pasoRequisito);
                pasoNew = em.merge(pasoNew);
            }
            if (requisitoOld != null && !requisitoOld.equals(requisitoNew)) {
                requisitoOld.getPasoRequisitoList().remove(pasoRequisito);
                requisitoOld = em.merge(requisitoOld);
            }
            if (requisitoNew != null && !requisitoNew.equals(requisitoOld)) {
                requisitoNew.getPasoRequisitoList().add(pasoRequisito);
                requisitoNew = em.merge(requisitoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                PasoRequisitoPK id = pasoRequisito.getPasoRequisitoPK();
                if (findPasoRequisito(id) == null) {
                    throw new NonexistentEntityException("The pasoRequisito with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(PasoRequisitoPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PasoRequisito pasoRequisito;
            try {
                pasoRequisito = em.getReference(PasoRequisito.class, id);
                pasoRequisito.getPasoRequisitoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pasoRequisito with id " + id + " no longer exists.", enfe);
            }
            Paso paso = pasoRequisito.getPaso();
            if (paso != null) {
                paso.getPasoRequisitoList().remove(pasoRequisito);
                paso = em.merge(paso);
            }
            Requisito requisito = pasoRequisito.getRequisito();
            if (requisito != null) {
                requisito.getPasoRequisitoList().remove(pasoRequisito);
                requisito = em.merge(requisito);
            }
            em.remove(pasoRequisito);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PasoRequisito> findPasoRequisitoEntities() {
        return findPasoRequisitoEntities(true, -1, -1);
    }

    public List<PasoRequisito> findPasoRequisitoEntities(int maxResults, int firstResult) {
        return findPasoRequisitoEntities(false, maxResults, firstResult);
    }

    private List<PasoRequisito> findPasoRequisitoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PasoRequisito.class));
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

    public PasoRequisito findPasoRequisito(PasoRequisitoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PasoRequisito.class, id);
        } finally {
            em.close();
        }
    }

    public int getPasoRequisitoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PasoRequisito> rt = cq.from(PasoRequisito.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
