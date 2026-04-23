package com.icc.labcontrol.proceso;

/**
 * DTO para la bitácora: proceso enriquecido con nombres desde usuarios y maquinas.
 */
public record ProcesoListItem(
        Long id,
        Long idin,
        Long iduser,
        String usuario,
        Integer status,
        String statusLabel,
        String hora,
        Integer idpc,
        String maquina,
        String comentario,
        Long atendido,
        String atendidoUsuario,
        String horafin,
        String comfin,
        String comre,
        String fechas
) {}
