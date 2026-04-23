package com.icc.labcontrol.proceso;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProcesoService {

    private final JdbcTemplate jdbcTemplate;

    public ProcesoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final Map<Integer, String> STATUS_LABEL = Map.of(
            1, "En proceso",
            2, "Rechazado",
            3, "Terminado"
    );

    private static final RowMapper<ProcesoListItem> MAPPER = (rs, rowNum) -> {
        int status = rs.getInt("status");
        long atendido = rs.getLong("atendido");
        boolean atendidoNull = rs.wasNull();
        return new ProcesoListItem(
                rs.getLong("id"),
                rs.getLong("idin"),
                rs.getLong("iduser"),
                rs.getString("usuario"),
                status,
                STATUS_LABEL.getOrDefault(status, "Estado " + status),
                rs.getString("hora"),
                (Integer) rs.getObject("idpc"),
                rs.getString("maquina"),
                rs.getString("comentario"),
                atendidoNull ? null : atendido,
                rs.getString("atendido_usuario"),
                rs.getString("horafin"),
                rs.getString("comfin"),
                rs.getString("comre"),
                rs.getString("fechas")
        );
    };

    /**
     * Lista paginada con joins a usuarios y maquinas. Filtros opcionales:
     *   status (1/2/3)
     *   q: texto libre que busca en comentario, comfin, hora, fechas y nombres de usuario
     */
    public PagedResult<ProcesoListItem> listar(Integer status, String q, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(200, Math.max(1, size));
        int offset = p * s;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        java.util.List<Object> args = new java.util.ArrayList<>();

        if (status != null) {
            where.append(" AND pr.status = ? ");
            args.add(status);
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim() + "%";
            where.append(" AND ( pr.comentario LIKE ? OR pr.comfin LIKE ? OR pr.comre LIKE ? " +
                    " OR pr.hora LIKE ? OR pr.fechas LIKE ? " +
                    " OR u.username LIKE ? OR u.usuarios LIKE ? " +
                    " OR ua.username LIKE ? OR ua.usuarios LIKE ? " +
                    " OR m.maquina LIKE ? ) ");
            for (int i = 0; i < 10; i++) args.add(like);
        }

        String baseFrom = " FROM icc_labcontrol.procesos pr " +
                " LEFT JOIN icc_labcontrol.usuarios u  ON u.id  = pr.iduser " +
                " LEFT JOIN icc_labcontrol.usuarios ua ON ua.id = pr.atendido " +
                " LEFT JOIN icc_labcontrol.maquinas m  ON m.idm = pr.idpc ";

        String sqlCount = "SELECT COUNT(*) " + baseFrom + where;
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class, args.toArray());
        if (total == null) total = 0L;

        String sqlList = "SELECT pr.id, pr.idin, pr.iduser, " +
                " COALESCE(u.usuarios, u.username) AS usuario, " +
                " pr.status, pr.hora, pr.idpc, m.maquina AS maquina, " +
                " pr.comentario, pr.atendido, " +
                " COALESCE(ua.usuarios, ua.username) AS atendido_usuario, " +
                " pr.horafin, pr.comfin, pr.comre, pr.fechas " +
                baseFrom + where +
                " ORDER BY pr.id DESC LIMIT ? OFFSET ? ";

        java.util.List<Object> listArgs = new java.util.ArrayList<>(args);
        listArgs.add(s);
        listArgs.add(offset);
        List<ProcesoListItem> items = jdbcTemplate.query(sqlList, MAPPER, listArgs.toArray());

        return PagedResult.of(items, total, p, s);
    }

    public record PagedResult<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
        public static <T> PagedResult<T> of(List<T> content, long totalElements, int page, int size) {
            int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
            return new PagedResult<>(content, totalElements, totalPages, page, size);
        }
    }
}
