package com.seguridadweb.service;

import com.seguridadweb.model.Tarea;
import com.seguridadweb.model.Usuario;
import com.seguridadweb.repository.TareaRepository;
import com.seguridadweb.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;

    public TareaService(TareaRepository tareaRepository, UsuarioRepository usuarioRepository) {
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Tarea> obtenerTareasPorEmail(String email) {
        return tareaRepository.findByPropietarioEmail(email);
    }

    public List<Tarea> buscarTareasPorTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return tareaRepository.findAll();
        }
        return tareaRepository.buscarPorTitulo(titulo.trim());
    }

    public List<Tarea> buscarTareasPorEmailYTitulo(String email, String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return tareaRepository.findByPropietarioEmail(email);
        }
        return tareaRepository.buscarPorEmailYTitulo(email, titulo.trim());
    }

    @Transactional
    public Tarea crearTarea(Tarea tarea, String emailPropietario) {
        Usuario propietario = usuarioRepository.findByEmail(emailPropietario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + emailPropietario));
        tarea.setPropietario(propietario);
        return tareaRepository.save(tarea);
    }

    @Transactional
    public Tarea alternarEstado(Long tareaId, String emailPropietario) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + tareaId));
        if (!tarea.getPropietario().getEmail().equalsIgnoreCase(emailPropietario)) {
            throw new SecurityException("No tiene permisos para modificar esta tarea");
        }
        tarea.setCompletada(!tarea.isCompletada());
        return tareaRepository.save(tarea);
    }

    @Transactional
    public void eliminarTarea(Long tareaId, String emailPropietario) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + tareaId));
        if (!tarea.getPropietario().getEmail().equalsIgnoreCase(emailPropietario)) {
            throw new SecurityException("No tiene permisos para eliminar esta tarea");
        }
        tareaRepository.delete(tarea);
    }
}
