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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Solicitud;

/**
 *
 * @author Kira Luis
 */
public class SolicitudJpaController implements Serializable {

    public SolicitudJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Solicitud solicitud) throws PreexistingEntityException, Exception {
        if (solicitud.getCasoList() == null) {
            solicitud.setCasoList(new ArrayList<Caso>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Caso> attachedCasoList = new ArrayList<Caso>();
            for (Caso casoListCasoToAttach : solicitud.getCasoList()) {
                casoListCasoToAttach = em.getReference(casoListCasoToAttach.getClass(), casoListCasoToAttach.getCasoPK());
                attachedCasoList.add(casoListCasoToAttach);
            }
            solicitud.setCasoList(attachedCasoList);
            em.persist(solicitud);
            for (Caso casoListCaso : solicitud.getCasoList()) {
                Solicitud oldSolicitudOfCasoListCaso = casoListCaso.getSolicitud();
                casoListCaso.setSolicitud(solicitud);
                casoListCaso = em.merge(casoListCaso);
                if (oldSolicitudOfCasoListCaso != null) {
                    oldSolicitudOfCasoListCaso.getCasoList().remove(casoListCaso);
                    oldSolicitudOfCasoListCaso = em.merge(oldSolicitudOfCasoListCaso);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findSolicitud(solicitud.getIdSolicitud()) != null) {
                throw new PreexistingEntityException("Solicitud " + solicitud + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Solicitud solicitud) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Solicitud persistentSolicitud = em.find(Solicitud.class, solicitud.getIdSolicitud());
            List<Caso> casoListOld = persistentSolicitud.getCasoList();
            List<Caso> casoListNew = solicitud.getCasoList();
            List<String> illegalOrphanMessages = null;
            for (Caso casoListOldCaso : casoListOld) {
                if (!casoListNew.contains(casoListOldCaso)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Caso " + casoListOldCaso + " since its solicitud field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Caso> attachedCasoListNew = new ArrayList<Caso>();
            for (Caso casoListNewCasoToAttach : casoListNew) {
                casoListNewCasoToAttach = em.getReference(casoListNewCasoToAttach.getClass(), casoListNewCasoToAttach.getCasoPK());
                attachedCasoListNew.add(casoListNewCasoToAttach);
            }
            casoListNew = attachedCasoListNew;
            solicitud.setCasoList(casoListNew);
            solicitud = em.merge(solicitud);
            for (Caso casoListNewCaso : casoListNew) {
                if (!casoListOld.contains(casoListNewCaso)) {
                    Solicitud oldSolicitudOfCasoListNewCaso = casoListNewCaso.getSolicitud();
                    casoListNewCaso.setSolicitud(solicitud);
                    casoListNewCaso = em.merge(casoListNewCaso);
                    if (oldSolicitudOfCasoListNewCaso != null && !oldSolicitudOfCasoListNewCaso.equals(solicitud)) {
                        oldSolicitudOfCasoListNewCaso.getCasoList().remove(casoListNewCaso);
                        oldSolicitudOfCasoListNewCaso = em.merge(oldSolicitudOfCasoListNewCaso);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = solicitud.getIdSolicitud();
                if (findSolicitud(id) == null) {
                    throw new NonexistentEntityException("The solicitud with id " + id + " no longer exists.");
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
            Solicitud solicitud;
            try {
                solicitud = em.getReference(Solicitud.class, id);
                solicitud.getIdSolicitud();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The solicitud with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Caso> casoListOrphanCheck = solicitud.getCasoList();
            for (Caso casoListOrphanCheckCaso : casoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Solicitud (" + solicitud + ") cannot be destroyed since the Caso " + casoListOrphanCheckCaso + " in its casoList field has a non-nullable solicitud field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(solicitud);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Solicitud> findSolicitudEntities() {
        return findSolicitudEntities(true, -1, -1);
    }

    public List<Solicitud> findSolicitudEntities(int maxResults, int firstResult) {
        return findSolicitudEntities(false, maxResults, firstResult);
    }

    private List<Solicitud> findSolicitudEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Solicitud.class));
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

    public Solicitud findSolicitud(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Solicitud.class, id);
        } finally {
            em.close();
        }
    }

    public int getSolicitudCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Solicitud> rt = cq.from(Solicitud.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
