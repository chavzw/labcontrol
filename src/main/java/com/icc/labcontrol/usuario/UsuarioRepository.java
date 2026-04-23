package com.icc.labcontrol.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsername(String username);
    boolean existsByCodigo(Long codigo);
    boolean existsByCodigoAndTipo(Long codigo, Integer tipo);

    List<Usuario> findByCodigo(Long codigo);
    Optional<Usuario> findByCodigoAndTipo(Long codigo, Integer tipo);

    /**
     * Búsqueda opcional por código exacto, username o nombre completo (campo 'usuarios').
     * Si q es null/vacío, devuelve todos.
     */
    @Query(value =
            "SELECT u FROM Usuario u " +
            "WHERE (:q = '' " +
            "    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "    OR LOWER(u.usuarios) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "    OR u.codigo = :codigoExacto)")
    Page<Usuario> buscar(@Param("q") String q,
                         @Param("codigoExacto") Long codigoExacto,
                         Pageable pageable);
}
