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
import uesocc.tpi.lib.Proceso;
import uesocc.tpi.lib.ProcesoDetalle;
import uesocc.tpi.lib.ProcesoDetallePK;

/**
 *
 * @author Kira Luis
 */
public class ProcesoDetalleJpaController implements Serializable {

    public ProcesoDetalleJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ProcesoDetalle procesoDetalle) throws PreexistingEntityException, Exception {
        if (procesoDetalle.getProcesoDetallePK() == null) {
            procesoDetalle.setProcesoDetallePK(new ProcesoDetallePK());
        }
        procesoDetalle.getProcesoDetallePK().setIdPaso(procesoDetalle.getPaso().getPasoPK().getIdPaso());
        procesoDetalle.getProcesoDetallePK().setIdProceso(procesoDetalle.getProceso().getProcesoPK().getIdProceso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Paso paso = procesoDetalle.getPaso();
            if (paso != null) {
                paso = em.getReference(paso.getClass(), paso.getPasoPK());
                procesoDetalle.setPaso(paso);
            }
            Proceso proceso = procesoDetalle.getProceso();
            if (proceso != null) {
                proceso = em.getReference(proceso.getClass(), proceso.getProcesoPK());
                procesoDetalle.setProceso(proceso);
            }
            em.persist(procesoDetalle);
            if (paso != null) {
                paso.getProcesoDetalleList().add(procesoDetalle);
                paso = em.merge(paso);
            }
            if (proceso != null) {
                proceso.getProcesoDetalleList().add(procesoDetalle);
                proceso = em.merge(proceso);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findProcesoDetalle(procesoDetalle.getProcesoDetallePK()) != null) {
                throw new PreexistingEntityException("ProcesoDetalle " + procesoDetalle + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ProcesoDetalle procesoDetalle) throws NonexistentEntityException, Exception {
        procesoDetalle.getProcesoDetallePK().setIdPaso(procesoDetalle.getPaso().getPasoPK().getIdPaso());
        procesoDetalle.getProcesoDetallePK().setIdProceso(procesoDetalle.getProceso().getProcesoPK().getIdProceso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcesoDetalle persistentProcesoDetalle = em.find(ProcesoDetalle.class, procesoDetalle.getProcesoDetallePK());
            Paso pasoOld = persistentProcesoDetalle.getPaso();
            Paso pasoNew = procesoDetalle.getPaso();
            Proceso procesoOld = persistentProcesoDetalle.getProceso();
            Proceso procesoNew = procesoDetalle.getProceso();
            if (pasoNew != null) {
                pasoNew = em.getReference(pasoNew.getClass(), pasoNew.getPasoPK());
                procesoDetalle.setPaso(pasoNew);
            }
            if (procesoNew != null) {
                procesoNew = em.getReference(procesoNew.getClass(), procesoNew.getProcesoPK());
                procesoDetalle.setProceso(procesoNew);
            }
            procesoDetalle = em.merge(procesoDetalle);
            if (pasoOld != null && !pasoOld.equals(pasoNew)) {
                pasoOld.getProcesoDetalleList().remove(procesoDetalle);
                pasoOld = em.merge(pasoOld);
            }
            if (pasoNew != null && !pasoNew.equals(pasoOld)) {
                pasoNew.getProcesoDetalleList().add(procesoDetalle);
                pasoNew = em.merge(pasoNew);
            }
            if (procesoOld != null && !procesoOld.equals(procesoNew)) {
                procesoOld.getProcesoDetalleList().remove(procesoDetalle);
                procesoOld = em.merge(procesoOld);
            }
            if (procesoNew != null && !procesoNew.equals(procesoOld)) {
                procesoNew.getProcesoDetalleList().add(procesoDetalle);
                procesoNew = em.merge(procesoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                ProcesoDetallePK id = procesoDetalle.getProcesoDetallePK();
                if (findProcesoDetalle(id) == null) {
                    throw new NonexistentEntityException("The procesoDetalle with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(ProcesoDetallePK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ProcesoDetalle procesoDetalle;
            try {
                procesoDetalle = em.getReference(ProcesoDetalle.class, id);
                procesoDetalle.getProcesoDetallePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The procesoDetalle with id " + id + " no longer exists.", enfe);
            }
            Paso paso = procesoDetalle.getPaso();
            if (paso != null) {
                paso.getProcesoDetalleList().remove(procesoDetalle);
                paso = em.merge(paso);
            }
            Proceso proceso = procesoDetalle.getProceso();
            if (proceso != null) {
                proceso.getProcesoDetalleList().remove(procesoDetalle);
                proceso = em.merge(proceso);
            }
            em.remove(procesoDetalle);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ProcesoDetalle> findProcesoDetalleEntities() {
        return findProcesoDetalleEntities(true, -1, -1);
    }

    public List<ProcesoDetalle> findProcesoDetalleEntities(int maxResults, int firstResult) {
        return findProcesoDetalleEntities(false, maxResults, firstResult);
    }

    private List<ProcesoDetalle> findProcesoDetalleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ProcesoDetalle.class));
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

    public ProcesoDetalle findProcesoDetalle(ProcesoDetallePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ProcesoDetalle.class, id);
        } finally {
            em.close();
        }
    }

    public int getProcesoDetalleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ProcesoDetalle> rt = cq.from(ProcesoDetalle.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
