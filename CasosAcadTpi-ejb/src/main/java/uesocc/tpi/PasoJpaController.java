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
import uesocc.tpi.lib.TipoPaso;
import uesocc.tpi.lib.ProcesoDetalle;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uesocc.tpi.exceptions.IllegalOrphanException;
import uesocc.tpi.exceptions.NonexistentEntityException;
import uesocc.tpi.exceptions.PreexistingEntityException;
import uesocc.tpi.lib.Paso;
import uesocc.tpi.lib.PasoPK;
import uesocc.tpi.lib.PasoRequisito;

/**
 *
 * @author Kira Luis
 */
public class PasoJpaController implements Serializable {

    public PasoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Paso paso) throws PreexistingEntityException, Exception {
        if (paso.getPasoPK() == null) {
            paso.setPasoPK(new PasoPK());
        }
        if (paso.getProcesoDetalleList() == null) {
            paso.setProcesoDetalleList(new ArrayList<ProcesoDetalle>());
        }
        if (paso.getPasoRequisitoList() == null) {
            paso.setPasoRequisitoList(new ArrayList<PasoRequisito>());
        }
        paso.getPasoPK().setIdTipoPaso(paso.getTipoPaso().getIdTipoPaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TipoPaso tipoPaso = paso.getTipoPaso();
            if (tipoPaso != null) {
                tipoPaso = em.getReference(tipoPaso.getClass(), tipoPaso.getIdTipoPaso());
                paso.setTipoPaso(tipoPaso);
            }
            List<ProcesoDetalle> attachedProcesoDetalleList = new ArrayList<ProcesoDetalle>();
            for (ProcesoDetalle procesoDetalleListProcesoDetalleToAttach : paso.getProcesoDetalleList()) {
                procesoDetalleListProcesoDetalleToAttach = em.getReference(procesoDetalleListProcesoDetalleToAttach.getClass(), procesoDetalleListProcesoDetalleToAttach.getProcesoDetallePK());
                attachedProcesoDetalleList.add(procesoDetalleListProcesoDetalleToAttach);
            }
            paso.setProcesoDetalleList(attachedProcesoDetalleList);
            List<PasoRequisito> attachedPasoRequisitoList = new ArrayList<PasoRequisito>();
            for (PasoRequisito pasoRequisitoListPasoRequisitoToAttach : paso.getPasoRequisitoList()) {
                pasoRequisitoListPasoRequisitoToAttach = em.getReference(pasoRequisitoListPasoRequisitoToAttach.getClass(), pasoRequisitoListPasoRequisitoToAttach.getPasoRequisitoPK());
                attachedPasoRequisitoList.add(pasoRequisitoListPasoRequisitoToAttach);
            }
            paso.setPasoRequisitoList(attachedPasoRequisitoList);
            em.persist(paso);
            if (tipoPaso != null) {
                tipoPaso.getPasoList().add(paso);
                tipoPaso = em.merge(tipoPaso);
            }
            for (ProcesoDetalle procesoDetalleListProcesoDetalle : paso.getProcesoDetalleList()) {
                Paso oldPasoOfProcesoDetalleListProcesoDetalle = procesoDetalleListProcesoDetalle.getPaso();
                procesoDetalleListProcesoDetalle.setPaso(paso);
                procesoDetalleListProcesoDetalle = em.merge(procesoDetalleListProcesoDetalle);
                if (oldPasoOfProcesoDetalleListProcesoDetalle != null) {
                    oldPasoOfProcesoDetalleListProcesoDetalle.getProcesoDetalleList().remove(procesoDetalleListProcesoDetalle);
                    oldPasoOfProcesoDetalleListProcesoDetalle = em.merge(oldPasoOfProcesoDetalleListProcesoDetalle);
                }
            }
            for (PasoRequisito pasoRequisitoListPasoRequisito : paso.getPasoRequisitoList()) {
                Paso oldPasoOfPasoRequisitoListPasoRequisito = pasoRequisitoListPasoRequisito.getPaso();
                pasoRequisitoListPasoRequisito.setPaso(paso);
                pasoRequisitoListPasoRequisito = em.merge(pasoRequisitoListPasoRequisito);
                if (oldPasoOfPasoRequisitoListPasoRequisito != null) {
                    oldPasoOfPasoRequisitoListPasoRequisito.getPasoRequisitoList().remove(pasoRequisitoListPasoRequisito);
                    oldPasoOfPasoRequisitoListPasoRequisito = em.merge(oldPasoOfPasoRequisitoListPasoRequisito);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPaso(paso.getPasoPK()) != null) {
                throw new PreexistingEntityException("Paso " + paso + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Paso paso) throws IllegalOrphanException, NonexistentEntityException, Exception {
        paso.getPasoPK().setIdTipoPaso(paso.getTipoPaso().getIdTipoPaso());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Paso persistentPaso = em.find(Paso.class, paso.getPasoPK());
            TipoPaso tipoPasoOld = persistentPaso.getTipoPaso();
            TipoPaso tipoPasoNew = paso.getTipoPaso();
            List<ProcesoDetalle> procesoDetalleListOld = persistentPaso.getProcesoDetalleList();
            List<ProcesoDetalle> procesoDetalleListNew = paso.getProcesoDetalleList();
            List<PasoRequisito> pasoRequisitoListOld = persistentPaso.getPasoRequisitoList();
            List<PasoRequisito> pasoRequisitoListNew = paso.getPasoRequisitoList();
            List<String> illegalOrphanMessages = null;
            for (ProcesoDetalle procesoDetalleListOldProcesoDetalle : procesoDetalleListOld) {
                if (!procesoDetalleListNew.contains(procesoDetalleListOldProcesoDetalle)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain ProcesoDetalle " + procesoDetalleListOldProcesoDetalle + " since its paso field is not nullable.");
                }
            }
            for (PasoRequisito pasoRequisitoListOldPasoRequisito : pasoRequisitoListOld) {
                if (!pasoRequisitoListNew.contains(pasoRequisitoListOldPasoRequisito)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PasoRequisito " + pasoRequisitoListOldPasoRequisito + " since its paso field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (tipoPasoNew != null) {
                tipoPasoNew = em.getReference(tipoPasoNew.getClass(), tipoPasoNew.getIdTipoPaso());
                paso.setTipoPaso(tipoPasoNew);
            }
            List<ProcesoDetalle> attachedProcesoDetalleListNew = new ArrayList<ProcesoDetalle>();
            for (ProcesoDetalle procesoDetalleListNewProcesoDetalleToAttach : procesoDetalleListNew) {
                procesoDetalleListNewProcesoDetalleToAttach = em.getReference(procesoDetalleListNewProcesoDetalleToAttach.getClass(), procesoDetalleListNewProcesoDetalleToAttach.getProcesoDetallePK());
                attachedProcesoDetalleListNew.add(procesoDetalleListNewProcesoDetalleToAttach);
            }
            procesoDetalleListNew = attachedProcesoDetalleListNew;
            paso.setProcesoDetalleList(procesoDetalleListNew);
            List<PasoRequisito> attachedPasoRequisitoListNew = new ArrayList<PasoRequisito>();
            for (PasoRequisito pasoRequisitoListNewPasoRequisitoToAttach : pasoRequisitoListNew) {
                pasoRequisitoListNewPasoRequisitoToAttach = em.getReference(pasoRequisitoListNewPasoRequisitoToAttach.getClass(), pasoRequisitoListNewPasoRequisitoToAttach.getPasoRequisitoPK());
                attachedPasoRequisitoListNew.add(pasoRequisitoListNewPasoRequisitoToAttach);
            }
            pasoRequisitoListNew = attachedPasoRequisitoListNew;
            paso.setPasoRequisitoList(pasoRequisitoListNew);
            paso = em.merge(paso);
            if (tipoPasoOld != null && !tipoPasoOld.equals(tipoPasoNew)) {
                tipoPasoOld.getPasoList().remove(paso);
                tipoPasoOld = em.merge(tipoPasoOld);
            }
            if (tipoPasoNew != null && !tipoPasoNew.equals(tipoPasoOld)) {
                tipoPasoNew.getPasoList().add(paso);
                tipoPasoNew = em.merge(tipoPasoNew);
            }
            for (ProcesoDetalle procesoDetalleListNewProcesoDetalle : procesoDetalleListNew) {
                if (!procesoDetalleListOld.contains(procesoDetalleListNewProcesoDetalle)) {
                    Paso oldPasoOfProcesoDetalleListNewProcesoDetalle = procesoDetalleListNewProcesoDetalle.getPaso();
                    procesoDetalleListNewProcesoDetalle.setPaso(paso);
                    procesoDetalleListNewProcesoDetalle = em.merge(procesoDetalleListNewProcesoDetalle);
                    if (oldPasoOfProcesoDetalleListNewProcesoDetalle != null && !oldPasoOfProcesoDetalleListNewProcesoDetalle.equals(paso)) {
                        oldPasoOfProcesoDetalleListNewProcesoDetalle.getProcesoDetalleList().remove(procesoDetalleListNewProcesoDetalle);
                        oldPasoOfProcesoDetalleListNewProcesoDetalle = em.merge(oldPasoOfProcesoDetalleListNewProcesoDetalle);
                    }
                }
            }
            for (PasoRequisito pasoRequisitoListNewPasoRequisito : pasoRequisitoListNew) {
                if (!pasoRequisitoListOld.contains(pasoRequisitoListNewPasoRequisito)) {
                    Paso oldPasoOfPasoRequisitoListNewPasoRequisito = pasoRequisitoListNewPasoRequisito.getPaso();
                    pasoRequisitoListNewPasoRequisito.setPaso(paso);
                    pasoRequisitoListNewPasoRequisito = em.merge(pasoRequisitoListNewPasoRequisito);
                    if (oldPasoOfPasoRequisitoListNewPasoRequisito != null && !oldPasoOfPasoRequisitoListNewPasoRequisito.equals(paso)) {
                        oldPasoOfPasoRequisitoListNewPasoRequisito.getPasoRequisitoList().remove(pasoRequisitoListNewPasoRequisito);
                        oldPasoOfPasoRequisitoListNewPasoRequisito = em.merge(oldPasoOfPasoRequisitoListNewPasoRequisito);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                PasoPK id = paso.getPasoPK();
                if (findPaso(id) == null) {
                    throw new NonexistentEntityException("The paso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(PasoPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Paso paso;
            try {
                paso = em.getReference(Paso.class, id);
                paso.getPasoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The paso with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<ProcesoDetalle> procesoDetalleListOrphanCheck = paso.getProcesoDetalleList();
            for (ProcesoDetalle procesoDetalleListOrphanCheckProcesoDetalle : procesoDetalleListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Paso (" + paso + ") cannot be destroyed since the ProcesoDetalle " + procesoDetalleListOrphanCheckProcesoDetalle + " in its procesoDetalleList field has a non-nullable paso field.");
            }
            List<PasoRequisito> pasoRequisitoListOrphanCheck = paso.getPasoRequisitoList();
            for (PasoRequisito pasoRequisitoListOrphanCheckPasoRequisito : pasoRequisitoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Paso (" + paso + ") cannot be destroyed since the PasoRequisito " + pasoRequisitoListOrphanCheckPasoRequisito + " in its pasoRequisitoList field has a non-nullable paso field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            TipoPaso tipoPaso = paso.getTipoPaso();
            if (tipoPaso != null) {
                tipoPaso.getPasoList().remove(paso);
                tipoPaso = em.merge(tipoPaso);
            }
            em.remove(paso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Paso> findPasoEntities() {
        return findPasoEntities(true, -1, -1);
    }

    public List<Paso> findPasoEntities(int maxResults, int firstResult) {
        return findPasoEntities(false, maxResults, firstResult);
    }

    private List<Paso> findPasoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Paso.class));
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

    public Paso findPaso(PasoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Paso.class, id);
        } finally {
            em.close();
        }
    }

    public int getPasoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Paso> rt = cq.from(Paso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
