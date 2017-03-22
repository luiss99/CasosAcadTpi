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
import uesocc.tpi.lib.TipoRequisito;
import uesocc.tpi.lib.PasoRequisito;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Requisito;
import uesocc.tpi.lib.RequisitoPK;

/**
 *
 * @author Kira Luis
 */
public class RequisitoJpaController implements Serializable {

    public RequisitoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Requisito requisito) throws PreexistingEntityException, Exception {
        if (requisito.getRequisitoPK() == null) {
            requisito.setRequisitoPK(new RequisitoPK());
        }
        if (requisito.getPasoRequisitoList() == null) {
            requisito.setPasoRequisitoList(new ArrayList<PasoRequisito>());
        }
        requisito.getRequisitoPK().setIdTipoRequisito(requisito.getTipoRequisito().getIdTipoRequisito());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoRequisito tipoRequisito = requisito.getTipoRequisito();
            if (tipoRequisito != null) {
                tipoRequisito = em.getReference(tipoRequisito.getClass(), tipoRequisito.getIdTipoRequisito());
                requisito.setTipoRequisito(tipoRequisito);
            }
            List<PasoRequisito> attachedPasoRequisitoList = new ArrayList<PasoRequisito>();
            for (PasoRequisito pasoRequisitoListPasoRequisitoToAttach : requisito.getPasoRequisitoList()) {
                pasoRequisitoListPasoRequisitoToAttach = em.getReference(pasoRequisitoListPasoRequisitoToAttach.getClass(), pasoRequisitoListPasoRequisitoToAttach.getPasoRequisitoPK());
                attachedPasoRequisitoList.add(pasoRequisitoListPasoRequisitoToAttach);
            }
            requisito.setPasoRequisitoList(attachedPasoRequisitoList);
            em.persist(requisito);
            if (tipoRequisito != null) {
                tipoRequisito.getRequisitoList().add(requisito);
                tipoRequisito = em.merge(tipoRequisito);
            }
            for (PasoRequisito pasoRequisitoListPasoRequisito : requisito.getPasoRequisitoList()) {
                Requisito oldRequisitoOfPasoRequisitoListPasoRequisito = pasoRequisitoListPasoRequisito.getRequisito();
                pasoRequisitoListPasoRequisito.setRequisito(requisito);
                pasoRequisitoListPasoRequisito = em.merge(pasoRequisitoListPasoRequisito);
                if (oldRequisitoOfPasoRequisitoListPasoRequisito != null) {
                    oldRequisitoOfPasoRequisitoListPasoRequisito.getPasoRequisitoList().remove(pasoRequisitoListPasoRequisito);
                    oldRequisitoOfPasoRequisitoListPasoRequisito = em.merge(oldRequisitoOfPasoRequisitoListPasoRequisito);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findRequisito(requisito.getRequisitoPK()) != null) {
                throw new PreexistingEntityException("Requisito " + requisito + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Requisito requisito) throws IllegalOrphanException, NonexistentEntityException, Exception {
        requisito.getRequisitoPK().setIdTipoRequisito(requisito.getTipoRequisito().getIdTipoRequisito());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Requisito persistentRequisito = em.find(Requisito.class, requisito.getRequisitoPK());
            TipoRequisito tipoRequisitoOld = persistentRequisito.getTipoRequisito();
            TipoRequisito tipoRequisitoNew = requisito.getTipoRequisito();
            List<PasoRequisito> pasoRequisitoListOld = persistentRequisito.getPasoRequisitoList();
            List<PasoRequisito> pasoRequisitoListNew = requisito.getPasoRequisitoList();
            List<String> illegalOrphanMessages = null;
            for (PasoRequisito pasoRequisitoListOldPasoRequisito : pasoRequisitoListOld) {
                if (!pasoRequisitoListNew.contains(pasoRequisitoListOldPasoRequisito)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PasoRequisito " + pasoRequisitoListOldPasoRequisito + " since its requisito field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (tipoRequisitoNew != null) {
                tipoRequisitoNew = em.getReference(tipoRequisitoNew.getClass(), tipoRequisitoNew.getIdTipoRequisito());
                requisito.setTipoRequisito(tipoRequisitoNew);
            }
            List<PasoRequisito> attachedPasoRequisitoListNew = new ArrayList<PasoRequisito>();
            for (PasoRequisito pasoRequisitoListNewPasoRequisitoToAttach : pasoRequisitoListNew) {
                pasoRequisitoListNewPasoRequisitoToAttach = em.getReference(pasoRequisitoListNewPasoRequisitoToAttach.getClass(), pasoRequisitoListNewPasoRequisitoToAttach.getPasoRequisitoPK());
                attachedPasoRequisitoListNew.add(pasoRequisitoListNewPasoRequisitoToAttach);
            }
            pasoRequisitoListNew = attachedPasoRequisitoListNew;
            requisito.setPasoRequisitoList(pasoRequisitoListNew);
            requisito = em.merge(requisito);
            if (tipoRequisitoOld != null && !tipoRequisitoOld.equals(tipoRequisitoNew)) {
                tipoRequisitoOld.getRequisitoList().remove(requisito);
                tipoRequisitoOld = em.merge(tipoRequisitoOld);
            }
            if (tipoRequisitoNew != null && !tipoRequisitoNew.equals(tipoRequisitoOld)) {
                tipoRequisitoNew.getRequisitoList().add(requisito);
                tipoRequisitoNew = em.merge(tipoRequisitoNew);
            }
            for (PasoRequisito pasoRequisitoListNewPasoRequisito : pasoRequisitoListNew) {
                if (!pasoRequisitoListOld.contains(pasoRequisitoListNewPasoRequisito)) {
                    Requisito oldRequisitoOfPasoRequisitoListNewPasoRequisito = pasoRequisitoListNewPasoRequisito.getRequisito();
                    pasoRequisitoListNewPasoRequisito.setRequisito(requisito);
                    pasoRequisitoListNewPasoRequisito = em.merge(pasoRequisitoListNewPasoRequisito);
                    if (oldRequisitoOfPasoRequisitoListNewPasoRequisito != null && !oldRequisitoOfPasoRequisitoListNewPasoRequisito.equals(requisito)) {
                        oldRequisitoOfPasoRequisitoListNewPasoRequisito.getPasoRequisitoList().remove(pasoRequisitoListNewPasoRequisito);
                        oldRequisitoOfPasoRequisitoListNewPasoRequisito = em.merge(oldRequisitoOfPasoRequisitoListNewPasoRequisito);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                RequisitoPK id = requisito.getRequisitoPK();
                if (findRequisito(id) == null) {
                    throw new NonexistentEntityException("The requisito with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(RequisitoPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Requisito requisito;
            try {
                requisito = em.getReference(Requisito.class, id);
                requisito.getRequisitoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The requisito with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<PasoRequisito> pasoRequisitoListOrphanCheck = requisito.getPasoRequisitoList();
            for (PasoRequisito pasoRequisitoListOrphanCheckPasoRequisito : pasoRequisitoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Requisito (" + requisito + ") cannot be destroyed since the PasoRequisito " + pasoRequisitoListOrphanCheckPasoRequisito + " in its pasoRequisitoList field has a non-nullable requisito field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            TipoRequisito tipoRequisito = requisito.getTipoRequisito();
            if (tipoRequisito != null) {
                tipoRequisito.getRequisitoList().remove(requisito);
                tipoRequisito = em.merge(tipoRequisito);
            }
            em.remove(requisito);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Requisito> findRequisitoEntities() {
        return findRequisitoEntities(true, -1, -1);
    }

    public List<Requisito> findRequisitoEntities(int maxResults, int firstResult) {
        return findRequisitoEntities(false, maxResults, firstResult);
    }

    private List<Requisito> findRequisitoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Requisito.class));
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

    public Requisito findRequisito(RequisitoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Requisito.class, id);
        } finally {
            em.close();
        }
    }

    public int getRequisitoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Requisito> rt = cq.from(Requisito.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
