package com.icc.labcontrol.usuario;

import com.icc.labcontrol.proceso.ProcesoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de usuarios con listado consolidado por codigo usando BIT_OR(tipo).
 * BIT_OR en MySQL permite combinar los bits: Realiza(1) | Recibe(2) = Ambos(3).
 */
@Service
public class UsuarioService {

    private final JdbcTemplate jdbc;

    public UsuarioService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<UsuarioListItem> MAPPER = (rs, rowNum) -> new UsuarioListItem(
            rs.getLong("id"),
            rs.getString("ids"),
            rs.getLong("codigo"),
            rs.getString("username"),
            rs.getInt("status"),
            rs.getInt("tipo"),
            rs.getString("usuarios"),
            (Integer) rs.getObject("codigo2")
    );

    public ProcesoService.PagedResult<UsuarioListItem> listarConsolidado(
            String q, Long codigoExacto, int page, int size) {

        int p = Math.max(0, page);
        int s = Math.min(200, Math.max(1, size));
        int offset = p * s;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim() + "%";
            where.append(" AND ( username LIKE ? OR usuarios LIKE ? ");
            args.add(like);
            args.add(like);
            if (codigoExacto != null && codigoExacto >= 0) {
                where.append(" OR codigo = ? ");
                args.add(codigoExacto);
            }
            where.append(" ) ");
        }

        String baseFrom = " FROM icc_labcontrol.usuarios " + where;

        String sqlCount = "SELECT COUNT(DISTINCT codigo) " + baseFrom;
        Long total = jdbc.queryForObject(sqlCount, Long.class, args.toArray());
        if (total == null) total = 0L;

        String sqlList =
                "SELECT MIN(id) AS id, " +
                "       GROUP_CONCAT(id ORDER BY id SEPARATOR ',') AS ids, " +
                "       codigo, " +
                "       MAX(username) AS username, " +
                "       MAX(status) AS status, " +
                "       BIT_OR(tipo) AS tipo, " +
                "       MAX(usuarios) AS usuarios, " +
                "       MAX(codigo2) AS codigo2 " +
                baseFrom +
                " GROUP BY codigo " +
                " ORDER BY MIN(id) DESC " +
                " LIMIT ? OFFSET ? ";

        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add(s);
        listArgs.add(offset);

        List<UsuarioListItem> items = jdbc.query(sqlList, MAPPER, listArgs.toArray());

        return ProcesoService.PagedResult.of(items, total, p, s);
    }

    /** Elimina todas las filas que tengan ese codigo (puede ser más de una). */
    @Transactional
    public int eliminarPorCodigo(long codigo) {
        return jdbc.update("DELETE FROM icc_labcontrol.usuarios WHERE codigo = ?", codigo);
    }
}
