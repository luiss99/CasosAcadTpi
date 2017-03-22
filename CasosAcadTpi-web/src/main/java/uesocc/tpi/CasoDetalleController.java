package uesocc.tpi;

import uesocc.tpi.lib.CasoDetalle;
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

@Named("casoDetalleController")
@SessionScoped
public class CasoDetalleController implements Serializable {

    private CasoDetalle current;
    private DataModel items = null;
    @EJB
    private uesocc.tpi.CasoDetalleFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public CasoDetalleController() {
    }

    public CasoDetalle getSelected() {
        if (current == null) {
            current = new CasoDetalle();
            current.setCasoDetallePK(new uesocc.tpi.lib.CasoDetallePK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private CasoDetalleFacade getFacade() {
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
        current = (CasoDetalle) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new CasoDetalle();
        current.setCasoDetallePK(new uesocc.tpi.lib.CasoDetallePK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getCasoDetallePK().setIdCaso(current.getCaso().getCasoPK().getIdCaso());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (CasoDetalle) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getCasoDetallePK().setIdCaso(current.getCaso().getCasoPK().getIdCaso());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (CasoDetalle) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleDeleted"));
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

    public CasoDetalle getCasoDetalle(uesocc.tpi.lib.CasoDetallePK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = CasoDetalle.class)
    public static class CasoDetalleControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CasoDetalleController controller = (CasoDetalleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "casoDetalleController");
            return controller.getCasoDetalle(getKey(value));
        }

        uesocc.tpi.lib.CasoDetallePK getKey(String value) {
            uesocc.tpi.lib.CasoDetallePK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new uesocc.tpi.lib.CasoDetallePK();
            key.setIdCasoDetalle(Integer.parseInt(values[0]));
            key.setIdCaso(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(uesocc.tpi.lib.CasoDetallePK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdCasoDetalle());
            sb.append(SEPARATOR);
            sb.append(value.getIdCaso());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CasoDetalle) {
                CasoDetalle o = (CasoDetalle) object;
                return getStringKey(o.getCasoDetallePK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CasoDetalle.class.getName());
            }
        }

    }

}
