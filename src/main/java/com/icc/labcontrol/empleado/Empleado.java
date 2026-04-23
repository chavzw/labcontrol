package com.icc.labcontrol.empleado;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa un empleado leído de admin_icc.empleados.
 * Solo usamos codigo + nombres + apellidos; no se mapea como entity
 * para no atar el backend al schema completo de admin_icc.empleados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {
    private Long codigo;
    private String nombres;
    private String apellidos;

    public String getNombreCompleto() {
        String n = nombres == null ? "" : nombres.trim();
        String a = apellidos == null ? "" : apellidos.trim();
        return (n + " " + a).trim();
    }
}
