package uesocc.tpi;

import uesocc.tpi.lib.PasoRequisito;
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

@Named("pasoRequisitoController")
@SessionScoped
public class PasoRequisitoController implements Serializable {

    private PasoRequisito current;
    private DataModel items = null;
    @EJB
    private uesocc.tpi.PasoRequisitoFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public PasoRequisitoController() {
    }

    public PasoRequisito getSelected() {
        if (current == null) {
            current = new PasoRequisito();
            current.setPasoRequisitoPK(new uesocc.tpi.lib.PasoRequisitoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private PasoRequisitoFacade getFacade() {
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
        current = (PasoRequisito) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new PasoRequisito();
        current.setPasoRequisitoPK(new uesocc.tpi.lib.PasoRequisitoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getPasoRequisitoPK().setIdPaso(current.getPaso().getPasoPK().getIdPaso());
            current.getPasoRequisitoPK().setIdRequisito(current.getRequisito().getRequisitoPK().getIdRequisito());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PasoRequisitoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (PasoRequisito) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getPasoRequisitoPK().setIdPaso(current.getPaso().getPasoPK().getIdPaso());
            current.getPasoRequisitoPK().setIdRequisito(current.getRequisito().getRequisitoPK().getIdRequisito());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PasoRequisitoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (PasoRequisito) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PasoRequisitoDeleted"));
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

    public PasoRequisito getPasoRequisito(uesocc.tpi.lib.PasoRequisitoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = PasoRequisito.class)
    public static class PasoRequisitoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PasoRequisitoController controller = (PasoRequisitoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "pasoRequisitoController");
            return controller.getPasoRequisito(getKey(value));
        }

        uesocc.tpi.lib.PasoRequisitoPK getKey(String value) {
            uesocc.tpi.lib.PasoRequisitoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new uesocc.tpi.lib.PasoRequisitoPK();
            key.setIdPaso(Integer.parseInt(values[0]));
            key.setIdRequisito(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(uesocc.tpi.lib.PasoRequisitoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdPaso());
            sb.append(SEPARATOR);
            sb.append(value.getIdRequisito());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof PasoRequisito) {
                PasoRequisito o = (PasoRequisito) object;
                return getStringKey(o.getPasoRequisitoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + PasoRequisito.class.getName());
            }
        }

    }

}
