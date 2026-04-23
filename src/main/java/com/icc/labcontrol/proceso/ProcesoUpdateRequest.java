package com.icc.labcontrol.proceso;

/**
 * Payload para editar cualquier campo de un proceso.
 * Campos null se ignoran (no se tocan) — solo se actualiza lo que venga.
 */
public record ProcesoUpdateRequest(
        Long idin,
        Long iduser,
        Integer status,
        String hora,
        Integer idpc,
        String comentario,
        Long atendido,
        String horafin,
        String comfin,
        String comre,
        String fechas
) {}
