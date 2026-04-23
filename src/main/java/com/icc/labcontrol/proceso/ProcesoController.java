package com.icc.labcontrol.proceso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/procesos")
@CrossOrigin(origins = "*")
public class ProcesoController {

    private final ProcesoService procesoService;
    private final ProcesoRepository procesoRepository;

    public ProcesoController(ProcesoService procesoService,
                             ProcesoRepository procesoRepository) {
        this.procesoService = procesoService;
        this.procesoRepository = procesoRepository;
    }

    @GetMapping
    public ProcesoService.PagedResult<ProcesoListItem> listar(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return procesoService.listar(status, q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return procesoRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("code", "NO_ENCONTRADO", "message", "Proceso " + id + " no existe.")
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProcesoUpdateRequest req) {
        Optional<Proceso> opt = procesoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(
                    Map.of("code", "NO_ENCONTRADO", "message", "Proceso " + id + " no existe."));
        }
        Proceso p = opt.get();

        if (req.idin() != null)       p.setIdin(req.idin());
        if (req.iduser() != null)     p.setIduser(req.iduser());
        if (req.status() != null) {
            if (req.status() < 1 || req.status() > 3) {
                return ResponseEntity.badRequest().body(
                        Map.of("code", "STATUS_INVALIDO",
                                "message", "status debe ser 1 (En proceso), 2 (Rechazado) o 3 (Terminado)."));
            }
            p.setStatus(req.status());
        }
        if (req.hora() != null)       p.setHora(req.hora());
        if (req.idpc() != null)       p.setIdpc(req.idpc());
        if (req.comentario() != null) p.setComentario(req.comentario());
        if (req.atendido() != null)   p.setAtendido(req.atendido());
        if (req.horafin() != null)    p.setHorafin(req.horafin());
        if (req.comfin() != null)     p.setComfin(req.comfin());
        if (req.comre() != null)      p.setComre(req.comre());
        if (req.fechas() != null)     p.setFechas(req.fechas());

        Proceso saved = procesoRepository.save(p);
        return ResponseEntity.ok(saved);
    }
}
