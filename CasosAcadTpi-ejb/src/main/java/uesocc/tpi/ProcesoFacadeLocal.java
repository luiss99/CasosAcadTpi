/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uesocc.tpi;

import java.util.List;
import javax.ejb.Local;
import uesocc.tpi.lib.Proceso;

/**
 *
 * @author Kira Luis
 */
@Local
public interface ProcesoFacadeLocal {

    void create(Proceso proceso);

    void edit(Proceso proceso);

    void remove(Proceso proceso);

    Proceso find(Object id);

    List<Proceso> findAll();

    List<Proceso> findRange(int[] range);

    int count();
    
}
