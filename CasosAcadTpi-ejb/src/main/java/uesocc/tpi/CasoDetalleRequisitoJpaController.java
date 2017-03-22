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
import uesocc.tpi.lib.CasoDetalle;
import uesocc.tpi.lib.CasoDetalleRequisitoAtestado;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.CasoDetalleRequisito;
import uesocc.tpi.lib.CasoDetalleRequisitoPK;

/**
 *
 * @author Kira Luis
 */
public class CasoDetalleRequisitoJpaController implements Serializable {

    public CasoDetalleRequisitoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CasoDetalleRequisito casoDetalleRequisito) throws PreexistingEntityException, Exception {
        if (casoDetalleRequisito.getCasoDetalleRequisitoPK() == null) {
            casoDetalleRequisito.setCasoDetalleRequisitoPK(new CasoDetalleRequisitoPK());
        }
        if (casoDetalleRequisito.getCasoDetalleRequisitoAtestadoList() == null) {
            casoDetalleRequisito.setCasoDetalleRequisitoAtestadoList(new ArrayList<CasoDetalleRequisitoAtestado>());
        }
        casoDetalleRequisito.getCasoDetalleRequisitoPK().setIdCasoDetalle(casoDetalleRequisito.getCasoDetalle().getCasoDetallePK().getIdCasoDetalle());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalle casoDetalle = casoDetalleRequisito.getCasoDetalle();
            if (casoDetalle != null) {
                casoDetalle = em.getReference(casoDetalle.getClass(), casoDetalle.getCasoDetallePK());
                casoDetalleRequisito.setCasoDetalle(casoDetalle);
            }
            List<CasoDetalleRequisitoAtestado> attachedCasoDetalleRequisitoAtestadoList = new ArrayList<CasoDetalleRequisitoAtestado>();
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestadoToAttach : casoDetalleRequisito.getCasoDetalleRequisitoAtestadoList()) {
                casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestadoToAttach = em.getReference(casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestadoToAttach.getClass(), casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestadoToAttach.getIdCasoDetalleRequisitoAtestado());
                attachedCasoDetalleRequisitoAtestadoList.add(casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestadoToAttach);
            }
            casoDetalleRequisito.setCasoDetalleRequisitoAtestadoList(attachedCasoDetalleRequisitoAtestadoList);
            em.persist(casoDetalleRequisito);
            if (casoDetalle != null) {
                casoDetalle.getCasoDetalleRequisitoList().add(casoDetalleRequisito);
                casoDetalle = em.merge(casoDetalle);
            }
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado : casoDetalleRequisito.getCasoDetalleRequisitoAtestadoList()) {
                CasoDetalleRequisito oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado = casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
                casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado.setIdCasoDetalleRequisito(casoDetalleRequisito);
                casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado = em.merge(casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado);
                if (oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado != null) {
                    oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado.getCasoDetalleRequisitoAtestadoList().remove(casoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado);
                    oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado = em.merge(oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListCasoDetalleRequisitoAtestado);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findCasoDetalleRequisito(casoDetalleRequisito.getCasoDetalleRequisitoPK()) != null) {
                throw new PreexistingEntityException("CasoDetalleRequisito " + casoDetalleRequisito + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CasoDetalleRequisito casoDetalleRequisito) throws IllegalOrphanException, NonexistentEntityException, Exception {
        casoDetalleRequisito.getCasoDetalleRequisitoPK().setIdCasoDetalle(casoDetalleRequisito.getCasoDetalle().getCasoDetallePK().getIdCasoDetalle());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalleRequisito persistentCasoDetalleRequisito = em.find(CasoDetalleRequisito.class, casoDetalleRequisito.getCasoDetalleRequisitoPK());
            CasoDetalle casoDetalleOld = persistentCasoDetalleRequisito.getCasoDetalle();
            CasoDetalle casoDetalleNew = casoDetalleRequisito.getCasoDetalle();
            List<CasoDetalleRequisitoAtestado> casoDetalleRequisitoAtestadoListOld = persistentCasoDetalleRequisito.getCasoDetalleRequisitoAtestadoList();
            List<CasoDetalleRequisitoAtestado> casoDetalleRequisitoAtestadoListNew = casoDetalleRequisito.getCasoDetalleRequisitoAtestadoList();
            List<String> illegalOrphanMessages = null;
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListOldCasoDetalleRequisitoAtestado : casoDetalleRequisitoAtestadoListOld) {
                if (!casoDetalleRequisitoAtestadoListNew.contains(casoDetalleRequisitoAtestadoListOldCasoDetalleRequisitoAtestado)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain CasoDetalleRequisitoAtestado " + casoDetalleRequisitoAtestadoListOldCasoDetalleRequisitoAtestado + " since its idCasoDetalleRequisito field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (casoDetalleNew != null) {
                casoDetalleNew = em.getReference(casoDetalleNew.getClass(), casoDetalleNew.getCasoDetallePK());
                casoDetalleRequisito.setCasoDetalle(casoDetalleNew);
            }
            List<CasoDetalleRequisitoAtestado> attachedCasoDetalleRequisitoAtestadoListNew = new ArrayList<CasoDetalleRequisitoAtestado>();
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestadoToAttach : casoDetalleRequisitoAtestadoListNew) {
                casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestadoToAttach = em.getReference(casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestadoToAttach.getClass(), casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestadoToAttach.getIdCasoDetalleRequisitoAtestado());
                attachedCasoDetalleRequisitoAtestadoListNew.add(casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestadoToAttach);
            }
            casoDetalleRequisitoAtestadoListNew = attachedCasoDetalleRequisitoAtestadoListNew;
            casoDetalleRequisito.setCasoDetalleRequisitoAtestadoList(casoDetalleRequisitoAtestadoListNew);
            casoDetalleRequisito = em.merge(casoDetalleRequisito);
            if (casoDetalleOld != null && !casoDetalleOld.equals(casoDetalleNew)) {
                casoDetalleOld.getCasoDetalleRequisitoList().remove(casoDetalleRequisito);
                casoDetalleOld = em.merge(casoDetalleOld);
            }
            if (casoDetalleNew != null && !casoDetalleNew.equals(casoDetalleOld)) {
                casoDetalleNew.getCasoDetalleRequisitoList().add(casoDetalleRequisito);
                casoDetalleNew = em.merge(casoDetalleNew);
            }
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado : casoDetalleRequisitoAtestadoListNew) {
                if (!casoDetalleRequisitoAtestadoListOld.contains(casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado)) {
                    CasoDetalleRequisito oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado = casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado.getIdCasoDetalleRequisito();
                    casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado.setIdCasoDetalleRequisito(casoDetalleRequisito);
                    casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado = em.merge(casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado);
                    if (oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado != null && !oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado.equals(casoDetalleRequisito)) {
                        oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado.getCasoDetalleRequisitoAtestadoList().remove(casoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado);
                        oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado = em.merge(oldIdCasoDetalleRequisitoOfCasoDetalleRequisitoAtestadoListNewCasoDetalleRequisitoAtestado);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                CasoDetalleRequisitoPK id = casoDetalleRequisito.getCasoDetalleRequisitoPK();
                if (findCasoDetalleRequisito(id) == null) {
                    throw new NonexistentEntityException("The casoDetalleRequisito with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(CasoDetalleRequisitoPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CasoDetalleRequisito casoDetalleRequisito;
            try {
                casoDetalleRequisito = em.getReference(CasoDetalleRequisito.class, id);
                casoDetalleRequisito.getCasoDetalleRequisitoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The casoDetalleRequisito with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<CasoDetalleRequisitoAtestado> casoDetalleRequisitoAtestadoListOrphanCheck = casoDetalleRequisito.getCasoDetalleRequisitoAtestadoList();
            for (CasoDetalleRequisitoAtestado casoDetalleRequisitoAtestadoListOrphanCheckCasoDetalleRequisitoAtestado : casoDetalleRequisitoAtestadoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This CasoDetalleRequisito (" + casoDetalleRequisito + ") cannot be destroyed since the CasoDetalleRequisitoAtestado " + casoDetalleRequisitoAtestadoListOrphanCheckCasoDetalleRequisitoAtestado + " in its casoDetalleRequisitoAtestadoList field has a non-nullable idCasoDetalleRequisito field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            CasoDetalle casoDetalle = casoDetalleRequisito.getCasoDetalle();
            if (casoDetalle != null) {
                casoDetalle.getCasoDetalleRequisitoList().remove(casoDetalleRequisito);
                casoDetalle = em.merge(casoDetalle);
            }
            em.remove(casoDetalleRequisito);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CasoDetalleRequisito> findCasoDetalleRequisitoEntities() {
        return findCasoDetalleRequisitoEntities(true, -1, -1);
    }

    public List<CasoDetalleRequisito> findCasoDetalleRequisitoEntities(int maxResults, int firstResult) {
        return findCasoDetalleRequisitoEntities(false, maxResults, firstResult);
    }

    private List<CasoDetalleRequisito> findCasoDetalleRequisitoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CasoDetalleRequisito.class));
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

    public CasoDetalleRequisito findCasoDetalleRequisito(CasoDetalleRequisitoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CasoDetalleRequisito.class, id);
        } finally {
            em.close();
        }
    }

    public int getCasoDetalleRequisitoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CasoDetalleRequisito> rt = cq.from(CasoDetalleRequisito.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
