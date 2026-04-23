package com.icc.labcontrol.usuario;

/**
 * Payload para actualizar un usuario (consolidado por codigo).
 * 'tipo' llega como bitmask:
 *   1 = Realiza
 *   2 = Recibe
 *   3 = Ambos (1 | 2)
 * Si se pasa 3, se asegurará que existan dos filas (tipo=1 y tipo=2).
 * Si sólo tiene tipo=1 o tipo=2, se eliminarán las filas del otro tipo.
 */
public record UsuarioUpdateRequest(
        String username,
        Integer status,
        Integer tipo
) {}
