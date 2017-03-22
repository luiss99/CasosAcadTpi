package uesocc.tpi;

import uesocc.tpi.lib.CasoDetalleRequisito;
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

@Named("casoDetalleRequisitoController")
@SessionScoped
public class CasoDetalleRequisitoController implements Serializable {

    private CasoDetalleRequisito current;
    private DataModel items = null;
    @EJB
    private uesocc.tpi.CasoDetalleRequisitoFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public CasoDetalleRequisitoController() {
    }

    public CasoDetalleRequisito getSelected() {
        if (current == null) {
            current = new CasoDetalleRequisito();
            current.setCasoDetalleRequisitoPK(new uesocc.tpi.lib.CasoDetalleRequisitoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private CasoDetalleRequisitoFacade getFacade() {
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
        current = (CasoDetalleRequisito) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new CasoDetalleRequisito();
        current.setCasoDetalleRequisitoPK(new uesocc.tpi.lib.CasoDetalleRequisitoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getCasoDetalleRequisitoPK().setIdCasoDetalle(current.getCasoDetalle().getCasoDetallePK().getIdCasoDetalle());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleRequisitoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (CasoDetalleRequisito) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getCasoDetalleRequisitoPK().setIdCasoDetalle(current.getCasoDetalle().getCasoDetallePK().getIdCasoDetalle());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleRequisitoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (CasoDetalleRequisito) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CasoDetalleRequisitoDeleted"));
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

    public CasoDetalleRequisito getCasoDetalleRequisito(uesocc.tpi.lib.CasoDetalleRequisitoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = CasoDetalleRequisito.class)
    public static class CasoDetalleRequisitoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CasoDetalleRequisitoController controller = (CasoDetalleRequisitoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "casoDetalleRequisitoController");
            return controller.getCasoDetalleRequisito(getKey(value));
        }

        uesocc.tpi.lib.CasoDetalleRequisitoPK getKey(String value) {
            uesocc.tpi.lib.CasoDetalleRequisitoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new uesocc.tpi.lib.CasoDetalleRequisitoPK();
            key.setIdCasoDetalleRequisito(Integer.parseInt(values[0]));
            key.setIdCasoDetalle(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(uesocc.tpi.lib.CasoDetalleRequisitoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdCasoDetalleRequisito());
            sb.append(SEPARATOR);
            sb.append(value.getIdCasoDetalle());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CasoDetalleRequisito) {
                CasoDetalleRequisito o = (CasoDetalleRequisito) object;
                return getStringKey(o.getCasoDetalleRequisitoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CasoDetalleRequisito.class.getName());
            }
        }

    }

}
