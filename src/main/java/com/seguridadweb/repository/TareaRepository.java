package com.seguridadweb.repository;

import com.seguridadweb.model.Tarea;
import com.seguridadweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByPropietario(Usuario propietario);

    List<Tarea> findByPropietarioEmail(String email);

    // =========================================================================
    // CONSULTA SEGURA: PARAMETRIZACIÓN CON @Query
    // =========================================================================
    // La anotación @Query con :titulo utiliza internamente sentencias preparadas
    // (PreparedStatement) a través de Hibernate/JPA. Los datos suministrados por el
    // usuario se tratan estrictamente como valores literales y no como código SQL ejecutable.
    @Query("SELECT t FROM Tarea t WHERE t.titulo LIKE CONCAT('%', :titulo, '%')")
    List<Tarea> buscarPorTitulo(@Param("titulo") String titulo);

    @Query("SELECT t FROM Tarea t WHERE t.propietario.email = :email AND t.titulo LIKE CONCAT('%', :titulo, '%')")
    List<Tarea> buscarPorEmailYTitulo(@Param("email") String email, @Param("titulo") String titulo);

    /*
     * =========================================================================
     * VULNERABILIDAD DOCUMENTADA - SQL INJECTION (COMENTADA CON FINES DIDÁCTICOS)
     * =========================================================================
     * ADVERTENCIA DE SEGURIDAD (CWE-89 / OWASP Top 10 A03:2021-Injection):
     * La siguiente firma representa una implementación vulnerable típica donde se
     * concatenan valores de entrada directamente dentro de la cadena SQL:
     *
     * // @Query(value = "SELECT * FROM tareas WHERE titulo = '" + titulo + "'", nativeQuery = true)
     * // List<Tarea> buscarPorTituloInseguro(String titulo);
     *
     * IMPACTO DE LA VULNERABILIDAD:
     * Si un atacante ingresa una carga maliciosa (payload) como:
     *   titulo = "' OR '1'='1"
     * La consulta ejecutada en el motor de base de datos se transforma en:
     *   SELECT * FROM tareas WHERE titulo = '' OR '1'='1'
     * Lo cual evalúa la condición como VERDADERA para cada fila, exponiendo todas
     * las tareas de todos los usuarios e ignorando el aislamiento de datos.
     *
     * Incluso permitiría inyecciones más destructivas como:
     *   titulo = "'; DROP TABLE tareas; --"
     *
     * MITIGACIÓN:
     * Usar siempre consultas parametrizadas con :parametro o los métodos derivados
     * de Spring Data JPA (e.g., findByTituloContainingIgnoreCase).
     * =========================================================================
     */
}
