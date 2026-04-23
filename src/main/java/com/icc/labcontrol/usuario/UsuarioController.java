package com.icc.labcontrol.usuario;

import com.icc.labcontrol.empleado.Empleado;
import com.icc.labcontrol.empleado.EmpleadoService;
import com.icc.labcontrol.proceso.ProcesoService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final EmpleadoService empleadoService;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             UsuarioService usuarioService,
                             EmpleadoService empleadoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public ProcesoService.PagedResult<UsuarioListItem> listar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        String qClean = q == null ? "" : q.trim();
        Long codigoExacto = null;
        try { codigoExacto = Long.parseLong(qClean); } catch (NumberFormatException ignored) {}
        return usuarioService.listarConsolidado(qClean, codigoExacto, page, size);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioCreateRequest req) {
        // ---- Validaciones básicas ----
        if (req.codigo() == null) {
            return ResponseEntity.badRequest().body(error("CODIGO_REQUERIDO", "El código es obligatorio."));
        }
        if (req.username() == null || req.username().isBlank()) {
            return ResponseEntity.badRequest().body(error("USERNAME_REQUERIDO", "El username es obligatorio."));
        }
        if (req.status() == null || (req.status() != 0 && req.status() != 1)) {
            return ResponseEntity.badRequest().body(error("STATUS_INVALIDO", "status debe ser 0 (Inactivo) o 1 (Activo)."));
        }
        if (req.tipo() == null || req.tipo() < 1 || req.tipo() > 3) {
            return ResponseEntity.badRequest().body(error("TIPO_INVALIDO", "tipo debe ser 1 (Realiza), 2 (Recibe) o 3 (Ambos)."));
        }

        // ---- Empleado debe existir ----
        Optional<Empleado> opt = empleadoService.buscarPorCodigo(req.codigo());
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(error("EMPLEADO_NO_ENCONTRADO",
                    "No existe el empleado con código " + req.codigo()));
        }
        Empleado emp = opt.get();

        // ---- Descomponer el tipo en filas individuales (1, 2 o ambos) ----
        java.util.List<Integer> tiposACrear = new java.util.ArrayList<>();
        if ((req.tipo() & 1) != 0) tiposACrear.add(1); // Realiza
        if ((req.tipo() & 2) != 0) tiposACrear.add(2); // Recibe

        // ---- Evitar duplicar la combinación (codigo, tipo) ----
        java.util.List<Usuario> creados = new java.util.ArrayList<>();
        java.util.List<Integer> omitidos = new java.util.ArrayList<>();

        for (Integer t : tiposACrear) {
            if (usuarioRepository.existsByCodigoAndTipo(req.codigo(), t)) {
                omitidos.add(t);
                continue;
            }
            Usuario u = new Usuario();
            u.setCodigo(req.codigo());
            u.setUsername(req.username().trim());
            u.setStatus(req.status());
            u.setTipo(t);
            u.setUsuarios(emp.getNombreCompleto());
            u.setCodigo2(req.codigo().intValue());
            creados.add(usuarioRepository.save(u));
        }

        if (creados.isEmpty()) {
            return ResponseEntity.status(409).body(error("YA_REGISTRADO",
                    "Este empleado ya estaba registrado con ese(s) tipo(s): " + omitidos));
        }

        return ResponseEntity.status(201).body(Map.of(
                "creados", creados,
                "omitidos", omitidos,
                "message", creados.size() == 1
                        ? "Usuario creado."
                        : "Se crearon " + creados.size() + " filas (Realiza y Recibe)."
        ));
    }

    /** Devuelve la vista consolidada (un solo objeto) del usuario por su código. */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable long codigo) {
        List<Usuario> filas = usuarioRepository.findByCodigo(codigo);
        if (filas.isEmpty()) {
            return ResponseEntity.status(404).body(error("NO_ENCONTRADO",
                    "No hay usuarios con código " + codigo));
        }
        int tipoBitmask = 0;
        String username = null;
        Integer status = null;
        String usuarios = null;
        Integer codigo2 = null;
        Long id = null;
        StringBuilder ids = new StringBuilder();
        for (Usuario u : filas) {
            if (u.getTipo() != null) tipoBitmask |= u.getTipo();
            if (username == null) username = u.getUsername();
            if (status == null) status = u.getStatus();
            if (usuarios == null) usuarios = u.getUsuarios();
            if (codigo2 == null) codigo2 = u.getCodigo2();
            if (id == null || (u.getId() != null && u.getId() < id)) id = u.getId();
            if (ids.length() > 0) ids.append(',');
            ids.append(u.getId());
        }
        return ResponseEntity.ok(new UsuarioListItem(
                id, ids.toString(), codigo, username, status, tipoBitmask, usuarios, codigo2
        ));
    }

    /**
     * Edita un usuario consolidado por codigo. Actualiza username/status en todas
     * las filas con ese codigo, y ajusta qué filas existen según el bitmask 'tipo':
     *   - Si 'tipo' tiene bit 1 (Realiza): se asegura que exista una fila con tipo=1
     *   - Si 'tipo' tiene bit 2 (Recibe): se asegura que exista una fila con tipo=2
     *   - Si algún bit NO está, se elimina la fila de ese tipo si existía.
     */
    @PutMapping("/codigo/{codigo}")
    @Transactional
    public ResponseEntity<?> editarPorCodigo(@PathVariable long codigo, @RequestBody UsuarioUpdateRequest req) {
        if (req.username() == null || req.username().isBlank()) {
            return ResponseEntity.badRequest().body(error("USERNAME_REQUERIDO", "El username es obligatorio."));
        }
        if (req.status() == null || (req.status() != 0 && req.status() != 1)) {
            return ResponseEntity.badRequest().body(error("STATUS_INVALIDO", "status debe ser 0 (Inactivo) o 1 (Activo)."));
        }
        if (req.tipo() == null || req.tipo() < 1 || req.tipo() > 3) {
            return ResponseEntity.badRequest().body(error("TIPO_INVALIDO", "tipo debe ser 1 (Realiza), 2 (Recibe) o 3 (Ambos)."));
        }

        List<Usuario> existentes = usuarioRepository.findByCodigo(codigo);
        if (existentes.isEmpty()) {
            return ResponseEntity.status(404).body(error("NO_ENCONTRADO",
                    "No hay usuarios con código " + codigo));
        }

        // Nombre completo original (para mantener la columna 'usuarios')
        String nombreCompleto = existentes.get(0).getUsuarios();
        Integer codigo2 = existentes.get(0).getCodigo2();

        Set<Integer> tiposActuales = new HashSet<>();
        for (Usuario u : existentes) tiposActuales.add(u.getTipo());

        Set<Integer> tiposDeseados = new HashSet<>();
        if ((req.tipo() & 1) != 0) tiposDeseados.add(1);
        if ((req.tipo() & 2) != 0) tiposDeseados.add(2);

        List<Usuario> actualizados = new ArrayList<>();
        List<Integer> creados = new ArrayList<>();
        List<Integer> borrados = new ArrayList<>();

        // 1) Actualizar o eliminar los existentes
        for (Usuario u : existentes) {
            if (tiposDeseados.contains(u.getTipo())) {
                u.setUsername(req.username().trim());
                u.setStatus(req.status());
                actualizados.add(usuarioRepository.save(u));
            } else {
                usuarioRepository.delete(u);
                borrados.add(u.getTipo());
            }
        }

        // 2) Crear las filas que falten
        for (Integer t : tiposDeseados) {
            if (!tiposActuales.contains(t)) {
                Usuario u = new Usuario();
                u.setCodigo(codigo);
                u.setUsername(req.username().trim());
                u.setStatus(req.status());
                u.setTipo(t);
                u.setUsuarios(nombreCompleto);
                u.setCodigo2(codigo2 != null ? codigo2 : (int) codigo);
                actualizados.add(usuarioRepository.save(u));
                creados.add(t);
            }
        }

        return ResponseEntity.ok(Map.of(
                "codigo", codigo,
                "actualizados", actualizados,
                "creados", creados,
                "borrados", borrados,
                "message", "Usuario actualizado."
        ));
    }

    /** Elimina todas las filas con ese codigo (útil cuando hay duplicados consolidados). */
    @DeleteMapping("/codigo/{codigo}")
    public ResponseEntity<?> eliminarPorCodigo(@PathVariable long codigo) {
        int borradas = usuarioService.eliminarPorCodigo(codigo);
        if (borradas == 0) {
            return ResponseEntity.status(404).body(error("NO_ENCONTRADO",
                    "No hay usuarios con código " + codigo));
        }
        return ResponseEntity.ok(Map.of("deleted", borradas, "codigo", codigo));
    }

    /** Elimina una fila específica por su id interno. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body(error("NO_ENCONTRADO",
                    "Usuario id " + id + " no existe"));
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", 1, "id", id));
    }

    private static Map<String, String> error(String code, String msg) {
        return Map.of("code", code, "message", msg);
    }
}
