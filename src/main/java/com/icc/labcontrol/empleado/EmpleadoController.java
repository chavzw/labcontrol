package com.icc.labcontrol.empleado;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> getPorCodigo(@PathVariable Long codigo) {
        return empleadoService.buscarPorCodigo(codigo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        new ApiError("EMPLEADO_NO_ENCONTRADO",
                                "No existe ningún empleado con código " + codigo)
                ));
    }

    public record ApiError(String code, String message) {}
}
