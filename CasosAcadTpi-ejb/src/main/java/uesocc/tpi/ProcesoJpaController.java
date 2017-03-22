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
import uesocc.tpi.lib.ProcesoDetalle;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Proceso;
import uesocc.tpi.lib.ProcesoPK;

/**
 *
 * @author Kira Luis
 */
public class ProcesoJpaController implements Serializable {

    public ProcesoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Proceso proceso) throws PreexistingEntityException, Exception {
        if (proceso.getProcesoPK() == null) {
            proceso.setProcesoPK(new ProcesoPK());
        }
        if (proceso.getProcesoDetalleList() == null) {
            proceso.setProcesoDetalleList(new ArrayList<ProcesoDetalle>());
        }
        proceso.getProcesoPK().setIdCaso(proceso.getCaso().getCasoPK().getIdCaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Caso caso = proceso.getCaso();
            if (caso != null) {
                caso = em.getReference(caso.getClass(), caso.getCasoPK());
                proceso.setCaso(caso);
            }
            List<ProcesoDetalle> attachedProcesoDetalleList = new ArrayList<ProcesoDetalle>();
            for (ProcesoDetalle procesoDetalleListProcesoDetalleToAttach : proceso.getProcesoDetalleList()) {
                procesoDetalleListProcesoDetalleToAttach = em.getReference(procesoDetalleListProcesoDetalleToAttach.getClass(), procesoDetalleListProcesoDetalleToAttach.getProcesoDetallePK());
                attachedProcesoDetalleList.add(procesoDetalleListProcesoDetalleToAttach);
            }
            proceso.setProcesoDetalleList(attachedProcesoDetalleList);
            em.persist(proceso);
            if (caso != null) {
                caso.getProcesoList().add(proceso);
                caso = em.merge(caso);
            }
            for (ProcesoDetalle procesoDetalleListProcesoDetalle : proceso.getProcesoDetalleList()) {
                Proceso oldProcesoOfProcesoDetalleListProcesoDetalle = procesoDetalleListProcesoDetalle.getProceso();
                procesoDetalleListProcesoDetalle.setProceso(proceso);
                procesoDetalleListProcesoDetalle = em.merge(procesoDetalleListProcesoDetalle);
                if (oldProcesoOfProcesoDetalleListProcesoDetalle != null) {
                    oldProcesoOfProcesoDetalleListProcesoDetalle.getProcesoDetalleList().remove(procesoDetalleListProcesoDetalle);
                    oldProcesoOfProcesoDetalleListProcesoDetalle = em.merge(oldProcesoOfProcesoDetalleListProcesoDetalle);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findProceso(proceso.getProcesoPK()) != null) {
                throw new PreexistingEntityException("Proceso " + proceso + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Proceso proceso) throws IllegalOrphanException, NonexistentEntityException, Exception {
        proceso.getProcesoPK().setIdCaso(proceso.getCaso().getCasoPK().getIdCaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Proceso persistentProceso = em.find(Proceso.class, proceso.getProcesoPK());
            Caso casoOld = persistentProceso.getCaso();
            Caso casoNew = proceso.getCaso();
            List<ProcesoDetalle> procesoDetalleListOld = persistentProceso.getProcesoDetalleList();
            List<ProcesoDetalle> procesoDetalleListNew = proceso.getProcesoDetalleList();
            List<String> illegalOrphanMessages = null;
            for (ProcesoDetalle procesoDetalleListOldProcesoDetalle : procesoDetalleListOld) {
                if (!procesoDetalleListNew.contains(procesoDetalleListOldProcesoDetalle)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain ProcesoDetalle " + procesoDetalleListOldProcesoDetalle + " since its proceso field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (casoNew != null) {
                casoNew = em.getReference(casoNew.getClass(), casoNew.getCasoPK());
                proceso.setCaso(casoNew);
            }
            List<ProcesoDetalle> attachedProcesoDetalleListNew = new ArrayList<ProcesoDetalle>();
            for (ProcesoDetalle procesoDetalleListNewProcesoDetalleToAttach : procesoDetalleListNew) {
                procesoDetalleListNewProcesoDetalleToAttach = em.getReference(procesoDetalleListNewProcesoDetalleToAttach.getClass(), procesoDetalleListNewProcesoDetalleToAttach.getProcesoDetallePK());
                attachedProcesoDetalleListNew.add(procesoDetalleListNewProcesoDetalleToAttach);
            }
            procesoDetalleListNew = attachedProcesoDetalleListNew;
            proceso.setProcesoDetalleList(procesoDetalleListNew);
            proceso = em.merge(proceso);
            if (casoOld != null && !casoOld.equals(casoNew)) {
                casoOld.getProcesoList().remove(proceso);
                casoOld = em.merge(casoOld);
            }
            if (casoNew != null && !casoNew.equals(casoOld)) {
                casoNew.getProcesoList().add(proceso);
                casoNew = em.merge(casoNew);
            }
            for (ProcesoDetalle procesoDetalleListNewProcesoDetalle : procesoDetalleListNew) {
                if (!procesoDetalleListOld.contains(procesoDetalleListNewProcesoDetalle)) {
                    Proceso oldProcesoOfProcesoDetalleListNewProcesoDetalle = procesoDetalleListNewProcesoDetalle.getProceso();
                    procesoDetalleListNewProcesoDetalle.setProceso(proceso);
                    procesoDetalleListNewProcesoDetalle = em.merge(procesoDetalleListNewProcesoDetalle);
                    if (oldProcesoOfProcesoDetalleListNewProcesoDetalle != null && !oldProcesoOfProcesoDetalleListNewProcesoDetalle.equals(proceso)) {
                        oldProcesoOfProcesoDetalleListNewProcesoDetalle.getProcesoDetalleList().remove(procesoDetalleListNewProcesoDetalle);
                        oldProcesoOfProcesoDetalleListNewProcesoDetalle = em.merge(oldProcesoOfProcesoDetalleListNewProcesoDetalle);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                ProcesoPK id = proceso.getProcesoPK();
                if (findProceso(id) == null) {
                    throw new NonexistentEntityException("The proceso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(ProcesoPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Proceso proceso;
            try {
                proceso = em.getReference(Proceso.class, id);
                proceso.getProcesoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The proceso with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<ProcesoDetalle> procesoDetalleListOrphanCheck = proceso.getProcesoDetalleList();
            for (ProcesoDetalle procesoDetalleListOrphanCheckProcesoDetalle : procesoDetalleListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Proceso (" + proceso + ") cannot be destroyed since the ProcesoDetalle " + procesoDetalleListOrphanCheckProcesoDetalle + " in its procesoDetalleList field has a non-nullable proceso field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Caso caso = proceso.getCaso();
            if (caso != null) {
                caso.getProcesoList().remove(proceso);
                caso = em.merge(caso);
            }
            em.remove(proceso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Proceso> findProcesoEntities() {
        return findProcesoEntities(true, -1, -1);
    }

    public List<Proceso> findProcesoEntities(int maxResults, int firstResult) {
        return findProcesoEntities(false, maxResults, firstResult);
    }

    private List<Proceso> findProcesoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Proceso.class));
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

    public Proceso findProceso(ProcesoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Proceso.class, id);
        } finally {
            em.close();
        }
    }

    public int getProcesoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Proceso> rt = cq.from(Proceso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
