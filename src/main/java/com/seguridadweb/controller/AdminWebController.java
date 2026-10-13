package com.seguridadweb.controller;

import com.seguridadweb.model.Usuario;
import com.seguridadweb.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final UsuarioService usuarioService;

    public AdminWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint protegido por hasRole('ADMIN') en SecurityConfig (@Order(2))
     */
    @GetMapping("/panel")
    public String panelAdministrador(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "admin";
    }
}
