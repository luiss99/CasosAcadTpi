/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import java.util.List;
import javax.ejb.Local;
import uesocc.tpi.lib.ProcesoDetalle;

/**
 *
 * @author Kira Luis
 */
@Local
public interface ProcesoDetalleFacadeLocal {

    void create(ProcesoDetalle procesoDetalle);

    void edit(ProcesoDetalle procesoDetalle);

    void remove(ProcesoDetalle procesoDetalle);

    ProcesoDetalle find(Object id);

    List<ProcesoDetalle> findAll();

    List<ProcesoDetalle> findRange(int[] range);

    int count();
    
}
