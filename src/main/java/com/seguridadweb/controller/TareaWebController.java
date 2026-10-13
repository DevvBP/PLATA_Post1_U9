package com.seguridadweb.controller;

import com.seguridadweb.model.Tarea;
import com.seguridadweb.model.Usuario;
import com.seguridadweb.service.TareaService;
import com.seguridadweb.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TareaWebController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    public TareaWebController(TareaService tareaService, UsuarioService usuarioService) {
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String buscar,
            Authentication authentication,
            Model model
    ) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Uso de consulta parametrizada segura en el repositorio
        List<Tarea> tareas = (buscar != null && !buscar.isBlank())
                ? tareaService.buscarTareasPorEmailYTitulo(email, buscar)
                : tareaService.obtenerTareasPorEmail(email);

        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        model.addAttribute("buscar", buscar != null ? buscar : "");
        if (!model.containsAttribute("nuevaTarea")) {
            model.addAttribute("nuevaTarea", new Tarea());
        }

        return "dashboard";
    }

    @PostMapping("/tareas/crear")
    public String crearTarea(
            @Valid @ModelAttribute("nuevaTarea") Tarea nuevaTarea,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.nuevaTarea", bindingResult);
            redirectAttributes.addFlashAttribute("nuevaTarea", nuevaTarea);
            redirectAttributes.addFlashAttribute("errorModal", "Revise los campos del formulario");
            return "redirect:/dashboard";
        }

        String email = authentication.getName();
        tareaService.crearTarea(nuevaTarea, email);
        redirectAttributes.addFlashAttribute("mensajeExito", "Tarea creada exitosamente");
        return "redirect:/dashboard";
    }

    @PostMapping("/tareas/{id}/toggle")
    public String alternarCompletada(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            tareaService.alternarEstado(id, authentication.getName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/tareas/{id}/eliminar")
    public String eliminarTarea(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            tareaService.eliminarTarea(id, authentication.getName());
            redirectAttributes.addFlashAttribute("mensajeExito", "Tarea eliminada correctamente");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }
}
