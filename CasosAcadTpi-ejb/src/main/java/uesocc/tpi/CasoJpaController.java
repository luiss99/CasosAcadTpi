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
import uesocc.tpi.lib.Solicitud;
import uesocc.tpi.lib.Proceso;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Caso;
import uesocc.tpi.lib.CasoDetalle;
import uesocc.tpi.lib.CasoPK;

/**
 *
 * @author Kira Luis
 */
public class CasoJpaController implements Serializable {

    public CasoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Caso caso) throws PreexistingEntityException, Exception {
        if (caso.getCasoPK() == null) {
            caso.setCasoPK(new CasoPK());
        }
        if (caso.getProcesoList() == null) {
            caso.setProcesoList(new ArrayList<Proceso>());
        }
        if (caso.getCasoDetalleList() == null) {
            caso.setCasoDetalleList(new ArrayList<CasoDetalle>());
        }
        caso.getCasoPK().setIdSolicitud(caso.getSolicitud().getIdSolicitud());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Solicitud solicitud = caso.getSolicitud();
            if (solicitud != null) {
                solicitud = em.getReference(solicitud.getClass(), solicitud.getIdSolicitud());
                caso.setSolicitud(solicitud);
            }
            List<Proceso> attachedProcesoList = new ArrayList<Proceso>();
            for (Proceso procesoListProcesoToAttach : caso.getProcesoList()) {
                procesoListProcesoToAttach = em.getReference(procesoListProcesoToAttach.getClass(), procesoListProcesoToAttach.getProcesoPK());
                attachedProcesoList.add(procesoListProcesoToAttach);
            }
            caso.setProcesoList(attachedProcesoList);
            List<CasoDetalle> attachedCasoDetalleList = new ArrayList<CasoDetalle>();
            for (CasoDetalle casoDetalleListCasoDetalleToAttach : caso.getCasoDetalleList()) {
                casoDetalleListCasoDetalleToAttach = em.getReference(casoDetalleListCasoDetalleToAttach.getClass(), casoDetalleListCasoDetalleToAttach.getCasoDetallePK());
                attachedCasoDetalleList.add(casoDetalleListCasoDetalleToAttach);
            }
            caso.setCasoDetalleList(attachedCasoDetalleList);
            em.persist(caso);
            if (solicitud != null) {
                solicitud.getCasoList().add(caso);
                solicitud = em.merge(solicitud);
            }
            for (Proceso procesoListProceso : caso.getProcesoList()) {
                Caso oldCasoOfProcesoListProceso = procesoListProceso.getCaso();
                procesoListProceso.setCaso(caso);
                procesoListProceso = em.merge(procesoListProceso);
                if (oldCasoOfProcesoListProceso != null) {
                    oldCasoOfProcesoListProceso.getProcesoList().remove(procesoListProceso);
                    oldCasoOfProcesoListProceso = em.merge(oldCasoOfProcesoListProceso);
                }
            }
            for (CasoDetalle casoDetalleListCasoDetalle : caso.getCasoDetalleList()) {
                Caso oldCasoOfCasoDetalleListCasoDetalle = casoDetalleListCasoDetalle.getCaso();
                casoDetalleListCasoDetalle.setCaso(caso);
                casoDetalleListCasoDetalle = em.merge(casoDetalleListCasoDetalle);
                if (oldCasoOfCasoDetalleListCasoDetalle != null) {
                    oldCasoOfCasoDetalleListCasoDetalle.getCasoDetalleList().remove(casoDetalleListCasoDetalle);
                    oldCasoOfCasoDetalleListCasoDetalle = em.merge(oldCasoOfCasoDetalleListCasoDetalle);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findCaso(caso.getCasoPK()) != null) {
                throw new PreexistingEntityException("Caso " + caso + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Caso caso) throws IllegalOrphanException, NonexistentEntityException, Exception {
        caso.getCasoPK().setIdSolicitud(caso.getSolicitud().getIdSolicitud());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Caso persistentCaso = em.find(Caso.class, caso.getCasoPK());
            Solicitud solicitudOld = persistentCaso.getSolicitud();
            Solicitud solicitudNew = caso.getSolicitud();
            List<Proceso> procesoListOld = persistentCaso.getProcesoList();
            List<Proceso> procesoListNew = caso.getProcesoList();
            List<CasoDetalle> casoDetalleListOld = persistentCaso.getCasoDetalleList();
            List<CasoDetalle> casoDetalleListNew = caso.getCasoDetalleList();
            List<String> illegalOrphanMessages = null;
            for (Proceso procesoListOldProceso : procesoListOld) {
                if (!procesoListNew.contains(procesoListOldProceso)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Proceso " + procesoListOldProceso + " since its caso field is not nullable.");
                }
            }
            for (CasoDetalle casoDetalleListOldCasoDetalle : casoDetalleListOld) {
                if (!casoDetalleListNew.contains(casoDetalleListOldCasoDetalle)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain CasoDetalle " + casoDetalleListOldCasoDetalle + " since its caso field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (solicitudNew != null) {
                solicitudNew = em.getReference(solicitudNew.getClass(), solicitudNew.getIdSolicitud());
                caso.setSolicitud(solicitudNew);
            }
            List<Proceso> attachedProcesoListNew = new ArrayList<Proceso>();
            for (Proceso procesoListNewProcesoToAttach : procesoListNew) {
                procesoListNewProcesoToAttach = em.getReference(procesoListNewProcesoToAttach.getClass(), procesoListNewProcesoToAttach.getProcesoPK());
                attachedProcesoListNew.add(procesoListNewProcesoToAttach);
            }
            procesoListNew = attachedProcesoListNew;
            caso.setProcesoList(procesoListNew);
            List<CasoDetalle> attachedCasoDetalleListNew = new ArrayList<CasoDetalle>();
            for (CasoDetalle casoDetalleListNewCasoDetalleToAttach : casoDetalleListNew) {
                casoDetalleListNewCasoDetalleToAttach = em.getReference(casoDetalleListNewCasoDetalleToAttach.getClass(), casoDetalleListNewCasoDetalleToAttach.getCasoDetallePK());
                attachedCasoDetalleListNew.add(casoDetalleListNewCasoDetalleToAttach);
            }
            casoDetalleListNew = attachedCasoDetalleListNew;
            caso.setCasoDetalleList(casoDetalleListNew);
            caso = em.merge(caso);
            if (solicitudOld != null && !solicitudOld.equals(solicitudNew)) {
                solicitudOld.getCasoList().remove(caso);
                solicitudOld = em.merge(solicitudOld);
            }
            if (solicitudNew != null && !solicitudNew.equals(solicitudOld)) {
                solicitudNew.getCasoList().add(caso);
                solicitudNew = em.merge(solicitudNew);
            }
            for (Proceso procesoListNewProceso : procesoListNew) {
                if (!procesoListOld.contains(procesoListNewProceso)) {
                    Caso oldCasoOfProcesoListNewProceso = procesoListNewProceso.getCaso();
                    procesoListNewProceso.setCaso(caso);
                    procesoListNewProceso = em.merge(procesoListNewProceso);
                    if (oldCasoOfProcesoListNewProceso != null && !oldCasoOfProcesoListNewProceso.equals(caso)) {
                        oldCasoOfProcesoListNewProceso.getProcesoList().remove(procesoListNewProceso);
                        oldCasoOfProcesoListNewProceso = em.merge(oldCasoOfProcesoListNewProceso);
                    }
                }
            }
            for (CasoDetalle casoDetalleListNewCasoDetalle : casoDetalleListNew) {
                if (!casoDetalleListOld.contains(casoDetalleListNewCasoDetalle)) {
                    Caso oldCasoOfCasoDetalleListNewCasoDetalle = casoDetalleListNewCasoDetalle.getCaso();
                    casoDetalleListNewCasoDetalle.setCaso(caso);
                    casoDetalleListNewCasoDetalle = em.merge(casoDetalleListNewCasoDetalle);
                    if (oldCasoOfCasoDetalleListNewCasoDetalle != null && !oldCasoOfCasoDetalleListNewCasoDetalle.equals(caso)) {
                        oldCasoOfCasoDetalleListNewCasoDetalle.getCasoDetalleList().remove(casoDetalleListNewCasoDetalle);
                        oldCasoOfCasoDetalleListNewCasoDetalle = em.merge(oldCasoOfCasoDetalleListNewCasoDetalle);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                CasoPK id = caso.getCasoPK();
                if (findCaso(id) == null) {
                    throw new NonexistentEntityException("The caso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(CasoPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Caso caso;
            try {
                caso = em.getReference(Caso.class, id);
                caso.getCasoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The caso with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Proceso> procesoListOrphanCheck = caso.getProcesoList();
            for (Proceso procesoListOrphanCheckProceso : procesoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Caso (" + caso + ") cannot be destroyed since the Proceso " + procesoListOrphanCheckProceso + " in its procesoList field has a non-nullable caso field.");
            }
            List<CasoDetalle> casoDetalleListOrphanCheck = caso.getCasoDetalleList();
            for (CasoDetalle casoDetalleListOrphanCheckCasoDetalle : casoDetalleListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Caso (" + caso + ") cannot be destroyed since the CasoDetalle " + casoDetalleListOrphanCheckCasoDetalle + " in its casoDetalleList field has a non-nullable caso field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Solicitud solicitud = caso.getSolicitud();
            if (solicitud != null) {
                solicitud.getCasoList().remove(caso);
                solicitud = em.merge(solicitud);
            }
            em.remove(caso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Caso> findCasoEntities() {
        return findCasoEntities(true, -1, -1);
    }

    public List<Caso> findCasoEntities(int maxResults, int firstResult) {
        return findCasoEntities(false, maxResults, firstResult);
    }

    private List<Caso> findCasoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Caso.class));
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

    public Caso findCaso(CasoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Caso.class, id);
        } finally {
            em.close();
        }
    }

    public int getCasoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Caso> rt = cq.from(Caso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
