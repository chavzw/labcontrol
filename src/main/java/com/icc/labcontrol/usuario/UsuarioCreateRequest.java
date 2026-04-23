package com.icc.labcontrol.usuario;

/**
 * Payload del formulario. 'tipo' llega como bitmask:
 *   1 = Realiza
 *   2 = Recibe
 *   3 = Ambos (1 | 2)
 */
public record UsuarioCreateRequest(
        Long codigo,
        String username,
        Integer status,
        Integer tipo
) {}
