package com.icc.labcontrol.proceso;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mapea icc_labcontrol.procesos
 *   status: 1 = en proceso, 2 = rechazado, 3 = terminado
 *   idpc:   FK a icc_labcontrol.maquinas.idm
 *   iduser / atendido: usuarios del sistema
 *   hora / horafin / fechas: strings libres con formato "DD-MM-YYYY (h:mm:ss am/pm)"
 */
@Entity
@Table(name = "procesos", schema = "icc_labcontrol")
@Getter
@Setter
@NoArgsConstructor
public class Proceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "idin", nullable = false)
    private Long idin;

    @Column(name = "iduser", nullable = false)
    private Long iduser;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "hora", nullable = false, length = 60)
    private String hora;

    @Column(name = "idpc", nullable = false)
    private Integer idpc;

    @Column(name = "comentario", nullable = false, length = 250)
    private String comentario;

    @Column(name = "atendido")
    private Long atendido;

    @Column(name = "horafin", length = 60)
    private String horafin;

    @Column(name = "comfin", length = 250)
    private String comfin;

    @Column(name = "comre", length = 250)
    private String comre;

    @Column(name = "fechas", length = 100)
    private String fechas;
}
