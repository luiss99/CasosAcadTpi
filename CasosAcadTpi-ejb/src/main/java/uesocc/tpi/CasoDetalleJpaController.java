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
import uesocc.tpi.lib.Caso;
import uesocc.tpi.lib.CasoDetalleRequisito;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.CasoDetalle;
import uesocc.tpi.lib.CasoDetallePK;

/**
 *
 * @author Kira Luis
 */
public class CasoDetalleJpaController implements Serializable {

    public CasoDetalleJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CasoDetalle casoDetalle) throws PreexistingEntityException, Exception {
        if (casoDetalle.getCasoDetallePK() == null) {
            casoDetalle.setCasoDetallePK(new CasoDetallePK());
        }
        if (casoDetalle.getCasoDetalleRequisitoList() == null) {
            casoDetalle.setCasoDetalleRequisitoList(new ArrayList<CasoDetalleRequisito>());
        }
        casoDetalle.getCasoDetallePK().setIdCaso(casoDetalle.getCaso().getCasoPK().getIdCaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Caso caso = casoDetalle.getCaso();
            if (caso != null) {
                caso = em.getReference(caso.getClass(), caso.getCasoPK());
                casoDetalle.setCaso(caso);
            }
            List<CasoDetalleRequisito> attachedCasoDetalleRequisitoList = new ArrayList<CasoDetalleRequisito>();
            for (CasoDetalleRequisito casoDetalleRequisitoListCasoDetalleRequisitoToAttach : casoDetalle.getCasoDetalleRequisitoList()) {
                casoDetalleRequisitoListCasoDetalleRequisitoToAttach = em.getReference(casoDetalleRequisitoListCasoDetalleRequisitoToAttach.getClass(), casoDetalleRequisitoListCasoDetalleRequisitoToAttach.getCasoDetalleRequisitoPK());
                attachedCasoDetalleRequisitoList.add(casoDetalleRequisitoListCasoDetalleRequisitoToAttach);
            }
            casoDetalle.setCasoDetalleRequisitoList(attachedCasoDetalleRequisitoList);
            em.persist(casoDetalle);
            if (caso != null) {
                caso.getCasoDetalleList().add(casoDetalle);
                caso = em.merge(caso);
            }
            for (CasoDetalleRequisito casoDetalleRequisitoListCasoDetalleRequisito : casoDetalle.getCasoDetalleRequisitoList()) {
                CasoDetalle oldCasoDetalleOfCasoDetalleRequisitoListCasoDetalleRequisito = casoDetalleRequisitoListCasoDetalleRequisito.getCasoDetalle();
                casoDetalleRequisitoListCasoDetalleRequisito.setCasoDetalle(casoDetalle);
                casoDetalleRequisitoListCasoDetalleRequisito = em.merge(casoDetalleRequisitoListCasoDetalleRequisito);
                if (oldCasoDetalleOfCasoDetalleRequisitoListCasoDetalleRequisito != null) {
                    oldCasoDetalleOfCasoDetalleRequisitoListCasoDetalleRequisito.getCasoDetalleRequisitoList().remove(casoDetalleRequisitoListCasoDetalleRequisito);
                    oldCasoDetalleOfCasoDetalleRequisitoListCasoDetalleRequisito = em.merge(oldCasoDetalleOfCasoDetalleRequisitoListCasoDetalleRequisito);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findCasoDetalle(casoDetalle.getCasoDetallePK()) != null) {
                throw new PreexistingEntityException("CasoDetalle " + casoDetalle + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CasoDetalle casoDetalle) throws IllegalOrphanException, NonexistentEntityException, Exception {
        casoDetalle.getCasoDetallePK().setIdCaso(casoDetalle.getCaso().getCasoPK().getIdCaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalle persistentCasoDetalle = em.find(CasoDetalle.class, casoDetalle.getCasoDetallePK());
            Caso casoOld = persistentCasoDetalle.getCaso();
            Caso casoNew = casoDetalle.getCaso();
            List<CasoDetalleRequisito> casoDetalleRequisitoListOld = persistentCasoDetalle.getCasoDetalleRequisitoList();
            List<CasoDetalleRequisito> casoDetalleRequisitoListNew = casoDetalle.getCasoDetalleRequisitoList();
            List<String> illegalOrphanMessages = null;
            for (CasoDetalleRequisito casoDetalleRequisitoListOldCasoDetalleRequisito : casoDetalleRequisitoListOld) {
                if (!casoDetalleRequisitoListNew.contains(casoDetalleRequisitoListOldCasoDetalleRequisito)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain CasoDetalleRequisito " + casoDetalleRequisitoListOldCasoDetalleRequisito + " since its casoDetalle field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (casoNew != null) {
                casoNew = em.getReference(casoNew.getClass(), casoNew.getCasoPK());
                casoDetalle.setCaso(casoNew);
            }
            List<CasoDetalleRequisito> attachedCasoDetalleRequisitoListNew = new ArrayList<CasoDetalleRequisito>();
            for (CasoDetalleRequisito casoDetalleRequisitoListNewCasoDetalleRequisitoToAttach : casoDetalleRequisitoListNew) {
                casoDetalleRequisitoListNewCasoDetalleRequisitoToAttach = em.getReference(casoDetalleRequisitoListNewCasoDetalleRequisitoToAttach.getClass(), casoDetalleRequisitoListNewCasoDetalleRequisitoToAttach.getCasoDetalleRequisitoPK());
                attachedCasoDetalleRequisitoListNew.add(casoDetalleRequisitoListNewCasoDetalleRequisitoToAttach);
            }
            casoDetalleRequisitoListNew = attachedCasoDetalleRequisitoListNew;
            casoDetalle.setCasoDetalleRequisitoList(casoDetalleRequisitoListNew);
            casoDetalle = em.merge(casoDetalle);
            if (casoOld != null && !casoOld.equals(casoNew)) {
                casoOld.getCasoDetalleList().remove(casoDetalle);
                casoOld = em.merge(casoOld);
            }
            if (casoNew != null && !casoNew.equals(casoOld)) {
                casoNew.getCasoDetalleList().add(casoDetalle);
                casoNew = em.merge(casoNew);
            }
            for (CasoDetalleRequisito casoDetalleRequisitoListNewCasoDetalleRequisito : casoDetalleRequisitoListNew) {
                if (!casoDetalleRequisitoListOld.contains(casoDetalleRequisitoListNewCasoDetalleRequisito)) {
                    CasoDetalle oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito = casoDetalleRequisitoListNewCasoDetalleRequisito.getCasoDetalle();
                    casoDetalleRequisitoListNewCasoDetalleRequisito.setCasoDetalle(casoDetalle);
                    casoDetalleRequisitoListNewCasoDetalleRequisito = em.merge(casoDetalleRequisitoListNewCasoDetalleRequisito);
                    if (oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito != null && !oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito.equals(casoDetalle)) {
                        oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito.getCasoDetalleRequisitoList().remove(casoDetalleRequisitoListNewCasoDetalleRequisito);
                        oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito = em.merge(oldCasoDetalleOfCasoDetalleRequisitoListNewCasoDetalleRequisito);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                CasoDetallePK id = casoDetalle.getCasoDetallePK();
                if (findCasoDetalle(id) == null) {
                    throw new NonexistentEntityException("The casoDetalle with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(CasoDetallePK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalle casoDetalle;
            try {
                casoDetalle = em.getReference(CasoDetalle.class, id);
                casoDetalle.getCasoDetallePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The casoDetalle with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<CasoDetalleRequisito> casoDetalleRequisitoListOrphanCheck = casoDetalle.getCasoDetalleRequisitoList();
            for (CasoDetalleRequisito casoDetalleRequisitoListOrphanCheckCasoDetalleRequisito : casoDetalleRequisitoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This CasoDetalle (" + casoDetalle + ") cannot be destroyed since the CasoDetalleRequisito " + casoDetalleRequisitoListOrphanCheckCasoDetalleRequisito + " in its casoDetalleRequisitoList field has a non-nullable casoDetalle field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Caso caso = casoDetalle.getCaso();
            if (caso != null) {
                caso.getCasoDetalleList().remove(casoDetalle);
                caso = em.merge(caso);
            }
            em.remove(casoDetalle);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CasoDetalle> findCasoDetalleEntities() {
        return findCasoDetalleEntities(true, -1, -1);
    }

    public List<CasoDetalle> findCasoDetalleEntities(int maxResults, int firstResult) {
        return findCasoDetalleEntities(false, maxResults, firstResult);
    }

    private List<CasoDetalle> findCasoDetalleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CasoDetalle.class));
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

    public CasoDetalle findCasoDetalle(CasoDetallePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CasoDetalle.class, id);
        } finally {
            em.close();
        }
    }

    public int getCasoDetalleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CasoDetalle> rt = cq.from(CasoDetalle.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
