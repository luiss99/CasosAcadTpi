package uesocc.tpi;

import uesocc.tpi.lib.Caso;
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

@Named("casoController")
@SessionScoped
public class CasoController implements Serializable {

    private Caso current;
    private DataModel items = null;
    @EJB
    private uesocc.tpi.CasoFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public CasoController() {
    }

    public Caso getSelected() {
        if (current == null) {
            current = new Caso();
            current.setCasoPK(new uesocc.tpi.lib.CasoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private CasoFacade getFacade() {
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
        current = (Caso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Caso();
        current.setCasoPK(new uesocc.tpi.lib.CasoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getCasoPK().setIdSolicitud(current.getSolicitud().getIdSolicitud());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Caso) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getCasoPK().setIdSolicitud(current.getSolicitud().getIdSolicitud());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Caso) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDeleted"));
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

    public Caso getCaso(uesocc.tpi.lib.CasoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Caso.class)
    public static class CasoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CasoController controller = (CasoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "casoController");
            return controller.getCaso(getKey(value));
        }

        uesocc.tpi.lib.CasoPK getKey(String value) {
            uesocc.tpi.lib.CasoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new uesocc.tpi.lib.CasoPK();
            key.setIdCaso(Integer.parseInt(values[0]));
            key.setIdSolicitud(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(uesocc.tpi.lib.CasoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdCaso());
            sb.append(SEPARATOR);
            sb.append(value.getIdSolicitud());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Caso) {
                Caso o = (Caso) object;
                return getStringKey(o.getCasoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Caso.class.getName());
            }
        }

    }

}
