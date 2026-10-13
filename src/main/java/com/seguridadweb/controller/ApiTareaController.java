package com.seguridadweb.controller;

import com.seguridadweb.dto.TareaDto;
import com.seguridadweb.model.Tarea;
import com.seguridadweb.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
public class ApiTareaController {

    private final TareaService tareaService;

    public ApiTareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    /**
     * Endpoint GET /api/tareas
     * Retorna exclusivamente las tareas del usuario validado mediante token JWT.
     */
    @GetMapping
    public ResponseEntity<List<TareaDto>> obtenerMisTareas(Authentication authentication) {
        // El subject del JWT ha sido establecido en el SecurityContext como nombre del Principal
        String emailAutenticado = authentication.getName();

        List<Tarea> tareas = tareaService.obtenerTareasPorEmail(emailAutenticado);

        List<TareaDto> dtos = tareas.stream()
                .map(t -> new TareaDto(
                        t.getId(),
                        t.getTitulo(),
                        t.getDescripcion(),
                        t.isCompletada(),
                        t.getPropietario().getEmail()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint opcional POST /api/tareas para crear tareas desde clientes REST (Postman/Curl)
     */
    @PostMapping
    public ResponseEntity<TareaDto> crearTarea(@Valid @RequestBody TareaDto tareaDto, Authentication authentication) {
        String emailAutenticado = authentication.getName();

        Tarea nuevaTarea = new Tarea();
        nuevaTarea.setTitulo(tareaDto.getTitulo());
        nuevaTarea.setDescripcion(tareaDto.getDescripcion());
        nuevaTarea.setCompletada(tareaDto.isCompletada());

        Tarea guardada = tareaService.crearTarea(nuevaTarea, emailAutenticado);

        TareaDto respuesta = new TareaDto(
                guardada.getId(),
                guardada.getTitulo(),
                guardada.getDescripcion(),
                guardada.isCompletada(),
                guardada.getPropietario().getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
