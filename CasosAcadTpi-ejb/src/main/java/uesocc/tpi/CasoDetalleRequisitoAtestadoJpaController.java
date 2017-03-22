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
import uesocc.tpi.lib.CasoDetalleRequisito;
import uesocc.tpi.lib.CasoDetalleRequisitoAtestado;

/**
 *
 * @author Kira Luis
 */
public class CasoDetalleRequisitoAtestadoJpaController implements Serializable {

    public CasoDetalleRequisitoAtestadoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestado) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalleRequisito idCasoDetalleRequisito = casoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
            if (idCasoDetalleRequisito != null) {
                idCasoDetalleRequisito = em.getReference(idCasoDetalleRequisito.getClass(), idCasoDetalleRequisito.getCasoDetalleRequisitoPK());
                casoDetalleRequisitoAtestado.setIdCasoDetalleRequisito(idCasoDetalleRequisito);
            }
            em.persist(casoDetalleRequisitoAtestado);
            if (idCasoDetalleRequisito != null) {
                idCasoDetalleRequisito.getCasoDetalleRequisitoAtestadoList().add(casoDetalleRequisitoAtestado);
                idCasoDetalleRequisito = em.merge(idCasoDetalleRequisito);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findCasoDetalleRequisitoAtestado(casoDetalleRequisitoAtestado.getIdCasoDetalleRequisitoAtestado()) != null) {
                throw new PreexistingEntityException("CasoDetalleRequisitoAtestado " + casoDetalleRequisitoAtestado + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestado) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalleRequisitoAtestado persistentCasoDetalleRequisitoAtestado = em.find(CasoDetalleRequisitoAtestado.class, casoDetalleRequisitoAtestado.getIdCasoDetalleRequisitoAtestado());
            CasoDetalleRequisito idCasoDetalleRequisitoOld = persistentCasoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
            CasoDetalleRequisito idCasoDetalleRequisitoNew = casoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
            if (idCasoDetalleRequisitoNew != null) {
                idCasoDetalleRequisitoNew = em.getReference(idCasoDetalleRequisitoNew.getClass(), idCasoDetalleRequisitoNew.getCasoDetalleRequisitoPK());
                casoDetalleRequisitoAtestado.setIdCasoDetalleRequisito(idCasoDetalleRequisitoNew);
            }
            casoDetalleRequisitoAtestado = em.merge(casoDetalleRequisitoAtestado);
            if (idCasoDetalleRequisitoOld != null && !idCasoDetalleRequisitoOld.equals(idCasoDetalleRequisitoNew)) {
                idCasoDetalleRequisitoOld.getCasoDetalleRequisitoAtestadoList().remove(casoDetalleRequisitoAtestado);
                idCasoDetalleRequisitoOld = em.merge(idCasoDetalleRequisitoOld);
            }
            if (idCasoDetalleRequisitoNew != null && !idCasoDetalleRequisitoNew.equals(idCasoDetalleRequisitoOld)) {
                idCasoDetalleRequisitoNew.getCasoDetalleRequisitoAtestadoList().add(casoDetalleRequisitoAtestado);
                idCasoDetalleRequisitoNew = em.merge(idCasoDetalleRequisitoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = casoDetalleRequisitoAtestado.getIdCasoDetalleRequisitoAtestado();
                if (findCasoDetalleRequisitoAtestado(id) == null) {
                    throw new NonexistentEntityException("The casoDetalleRequisitoAtestado with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestado;
            try {
                casoDetalleRequisitoAtestado = em.getReference(CasoDetalleRequisitoAtestado.class, id);
                casoDetalleRequisitoAtestado.getIdCasoDetalleRequisitoAtestado();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The casoDetalleRequisitoAtestado with id " + id + " no longer exists.", enfe);
            }
            CasoDetalleRequisito idCasoDetalleRequisito = casoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
            if (idCasoDetalleRequisito != null) {
                idCasoDetalleRequisito.getCasoDetalleRequisitoAtestadoList().remove(casoDetalleRequisitoAtestado);
                idCasoDetalleRequisito = em.merge(idCasoDetalleRequisito);
            }
            em.remove(casoDetalleRequisitoAtestado);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CasoDetalleRequisitoAtestado> findCasoDetalleRequisitoAtestadoEntities() {
        return findCasoDetalleRequisitoAtestadoEntities(true, -1, -1);
    }

    public List<CasoDetalleRequisitoAtestado> findCasoDetalleRequisitoAtestadoEntities(int maxResults, int firstResult) {
        return findCasoDetalleRequisitoAtestadoEntities(false, maxResults, firstResult);
    }

    private List<CasoDetalleRequisitoAtestado> findCasoDetalleRequisitoAtestadoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CasoDetalleRequisitoAtestado.class));
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

    public CasoDetalleRequisitoAtestado findCasoDetalleRequisitoAtestado(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CasoDetalleRequisitoAtestado.class, id);
        } finally {
            em.close();
        }
    }

    public int getCasoDetalleRequisitoAtestadoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CasoDetalleRequisitoAtestado> rt = cq.from(CasoDetalleRequisitoAtestado.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
