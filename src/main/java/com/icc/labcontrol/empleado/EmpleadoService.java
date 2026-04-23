package com.icc.labcontrol.empleado;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmpleadoService {

    private final JdbcTemplate jdbcTemplate;

    public EmpleadoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Empleado> ROW_MAPPER = (rs, rowNum) -> new Empleado(
            rs.getLong("codigo"),
            rs.getString("nombres"),
            rs.getString("apellidos")
    );

    public Optional<Empleado> buscarPorCodigo(Long codigo) {
        String sql = "SELECT codigo, nombres, apellidos " +
                "FROM admin_icc.empleados " +
                "WHERE codigo = ? LIMIT 1";
        return jdbcTemplate.query(sql, ROW_MAPPER, codigo).stream().findFirst();
    }
}
