package com.seguridadweb.config;

import com.seguridadweb.model.Tarea;
import com.seguridadweb.model.Usuario;
import com.seguridadweb.repository.TareaRepository;
import com.seguridadweb.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, TareaRepository tareaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tareaRepository = tareaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Inicializando datos de demostración en seguridad_db...");

            // Usuario Administrador
            Usuario admin = new Usuario();
            admin.setNombre("Administrador del Sistema");
            admin.setEmail("admin@seguridad.com");
            admin.setContrasenia(passwordEncoder.encode("admin123"));
            admin.setRol("ROLE_ADMIN");
            admin.setActivo(true);
            usuarioRepository.save(admin);

            // Usuario Estándar
            Usuario user = new Usuario();
            user.setNombre("Juan Pérez");
            user.setEmail("user@seguridad.com");
            user.setContrasenia(passwordEncoder.encode("user123"));
            user.setRol("ROLE_USER");
            user.setActivo(true);
            usuarioRepository.save(user);

            // Tareas de demostración para el usuario estándar
            Tarea tarea1 = new Tarea(
                    "Configurar Spring Security 6",
                    "Configurar apiFilterChain y webFilterChain con órdenes de precedencia 1 y 2.",
                    user,
                    true
            );
            Tarea tarea2 = new Tarea(
                    "Mitigar SQL Injection y XSS",
                    "Validar parámetros seguros en @Query y sanitizar Thymeleaf con th:text.",
                    user,
                    false
            );
            tareaRepository.save(tarea1);
            tareaRepository.save(tarea2);

            log.info("Datos inicializados correctamente.");
            log.info("Credenciales Usuario: user@seguridad.com / user123");
            log.info("Credenciales Admin: admin@seguridad.com / admin123");
        }
    }
}
