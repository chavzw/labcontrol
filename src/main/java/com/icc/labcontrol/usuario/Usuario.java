package com.icc.labcontrol.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios", schema = "icc_labcontrol")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false)
    private Long codigo;

    @Column(name = "username", nullable = false, length = 80)
    private String username;

    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * Bitmask: 1 = Realiza, 2 = Recibe, 3 = Ambos (1 + 2)
     */
    @Column(name = "tipo", nullable = false)
    private Integer tipo;

    @Column(name = "usuarios", length = 200)
    private String usuarios;

    @Column(name = "codigo2")
    private Integer codigo2;
}
