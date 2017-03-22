package uesocc.tpi;

import uesocc.tpi.lib.Proceso;
import uesocc.tpi.util.JsfUtil;
import uesocc.tpi.util.PaginationHelper;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("procesoController")
@SessionScoped
public class ProcesoController implements Serializable {

    private Proceso current;
    private DataModel items = null;
    @EJB
    private uesocc.tpi.ProcesoFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public ProcesoController() {
    }

    public Proceso getSelected() {
        if (current == null) {
            current = new Proceso();
            current.setProcesoPK(new uesocc.tpi.lib.ProcesoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private ProcesoFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Proceso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Proceso();
        current.setProcesoPK(new uesocc.tpi.lib.ProcesoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getProcesoPK().setIdCaso(current.getCaso().getCasoPK().getIdCaso());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProcesoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Proceso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getProcesoPK().setIdCaso(current.getCaso().getCasoPK().getIdCaso());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProcesoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Proceso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProcesoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Proceso getProceso(uesocc.tpi.lib.ProcesoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Proceso.class)
    public static class ProcesoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ProcesoController controller = (ProcesoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "procesoController");
            return controller.getProceso(getKey(value));
        }

        uesocc.tpi.lib.ProcesoPK getKey(String value) {
            uesocc.tpi.lib.ProcesoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new uesocc.tpi.lib.ProcesoPK();
            key.setIdProceso(Integer.parseInt(values[0]));
            key.setIdCaso(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(uesocc.tpi.lib.ProcesoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdProceso());
            sb.append(SEPARATOR);
            sb.append(value.getIdCaso());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Proceso) {
                Proceso o = (Proceso) object;
                return getStringKey(o.getProcesoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Proceso.class.getName());
            }
        }

    }

}
