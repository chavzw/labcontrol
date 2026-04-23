package com.icc.labcontrol.usuario;

/**
 * Fila consolidada para el listado: si un mismo 'codigo' aparece en varias filas
 * (p. ej. una con tipo=1 Realiza y otra con tipo=2 Recibe), se devuelven como
 * una sola fila con tipo=3 (Ambos) via BIT_OR. 'ids' trae los IDs originales
 * para poder eliminarlos todos a la vez.
 */
public record UsuarioListItem(
        Long id,
        String ids,
        Long codigo,
        String username,
        Integer status,
        Integer tipo,
        String usuarios,
        Integer codigo2
) {}
