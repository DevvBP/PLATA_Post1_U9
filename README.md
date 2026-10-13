# Seguridad Web - Laboratorio Spring Security 6

Proyecto universitario que demuestra la **coexistencia armónica de autenticación basada en Sesiones Web (Thymeleaf)** y **autenticación sin estado basada en JWT (API REST)** dentro de la misma aplicación Spring Boot 3.2.x con Spring Security 6 y Java 17.

---

## 🚀 Tecnologías y Dependencias

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.2.5
- **Manejador de Construcción:** Maven con Maven Wrapper (`./mvnw`)
- **Seguridad:** Spring Security 6 (`spring-boot-starter-security`, `thymeleaf-extras-springsecurity6`)
- **JWT:** JJWT 0.12.5 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Persistencia:** Spring Data JPA con Hibernate y MySQL Driver
- **Validaciones:** Jakarta Validation (`spring-boot-starter-validation`)
- **Motor de Plantillas:** Thymeleaf

---

## 🏗️ Arquitectura de Seguridad Coexistente

Spring Security 6 permite registrar múltiples cadenas de filtros de seguridad (`SecurityFilterChain`) ordenadas por precedencia mediante `@Order`:

```
                                  [ Solicitud HTTP ]
                                          |
                                          v
                         +----------------------------------+
                         |  Filtro de Seguridad Principal   |
                         +----------------------------------+
                                          |
                +-------------------------+-------------------------+
                |                                                   |
      Ruta: /api/**                                           Ruta: /**
                |                                                   |
                v                                                   v
+-------------------------------+                   +-------------------------------+
|    apiFilterChain (@Order 1)  |                   |    webFilterChain (@Order 2)  |
+-------------------------------+                   +-------------------------------+
| • Stateless (Sin JSESSIONID)  |                   | • Concurrencia con Sesión     |
| • CSRF Desactivado (API REST) |                   | • CSRF Activado (Navegador)   |
| • Filtro JwtAuthFilter        |                   | • Form Login (/login)         |
| • Login público /api/auth/**  |                   | • Logout con eliminación cookie|
| • Resto requiere Bearer Token |                   | • Rutas /admin/** (ROLE_ADMIN)|
+-------------------------------+                   +-------------------------------+
```

---

## 🗄️ Configuración de Base de Datos

En `src/main/resources/application.properties`, configure las credenciales de su servidor MySQL local:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/seguridad_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

Al iniciar por primera vez, `DataInitializer` creará automáticamente los siguientes usuarios de prueba:

| Usuario | Correo Electrónico | Contraseña | Rol | Acceso |
| :--- | :--- | :--- | :--- | :--- |
| **Estándar** | `user@seguridad.com` | `user123` | `ROLE_USER` | Dashboard, Tareas |
| **Administrador** | `admin@seguridad.com` | `admin123` | `ROLE_ADMIN` | Dashboard, Tareas, `/admin/panel` |

---

## 🛡️ Aspectos de Seguridad y Mitigación de Vulnerabilidades

### 1. Prevención de SQL Injection (CWE-89)
En `TareaRepository.java`:
- **Implementación Segura:**
  ```java
  @Query("SELECT t FROM Tarea t WHERE t.titulo LIKE CONCAT('%', :titulo, '%')")
  List<Tarea> buscarPorTitulo(@Param("titulo") String titulo);
  ```
  La parametrización vincula el valor mediante `PreparedStatement`, garantizando que cualquier entrada sea tratada como un literal de datos y no como código SQL ejecutable.
- **Implementación Insegura (Documentada y comentada en el código):**
  Concatenar entradas como `"SELECT * FROM tareas WHERE titulo = '" + titulo + "'"` permitiría inyecciones como `' OR '1'='1`.

### 2. Prevención de Cross-Site Scripting - XSS (CWE-79)
En `src/main/resources/templates/dashboard.html`:
- **Implementación Segura:**
  ```html
  <strong class="user-name-safe" th:text="${usuario.nombre}">Nombre de Usuario</strong>
  ```
  `th:text` escapa automáticamente entidades HTML (`<`, `>`, `&`, `"`, `'`), neutralizando scripts como `<script>alert('XSS')</script>`.
- **Implementación Insegura (Comentada en la plantilla para fines didácticos):**
  El uso de `th:utext` renderizaría etiquetas HTML/JS sin escapar, permitiendo Stored XSS.

---

## 🧪 Pruebas de la API REST (JWT)

### 1. Obtener Token JWT (`POST /api/auth/login`)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@seguridad.com",
    "contrasenia": "user123"
  }'
```
**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "email": "user@seguridad.com",
  "rol": "ROLE_USER"
}
```

### 2. Consultar Tareas con Token (`GET /api/tareas`)
```bash
curl -X GET http://localhost:8080/api/tareas \
  -H "Authorization: Bearer <TOKEN_OBTENIDO>"
```

### 3. Crear Tarea vía API (`POST /api/tareas`)
```bash
curl -X POST http://localhost:8080/api/tareas \
  -H "Authorization: Bearer <TOKEN_OBTENIDO>" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Nueva tarea desde API REST",
    "descripcion": "Probando seguridad JWT Stateless",
    "completada": false
  }'
```

---

## 💻 Ejecución del Proyecto

1. Asegúrate de tener una instancia de MySQL en ejecución en el puerto 3306.
2. Compilar y ejecutar:
   ```bash
   mvn clean spring-boot:run
   ```
   o con el wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Abrir el navegador en: `http://localhost:8080/login`

---

## 📸 Evidencias de Ejecución del Laboratorio

Todas las capturas de pantalla de prueba y validación de seguridad se encuentran documentadas en la carpeta [`evidencias/`](evidencias/):

| Evidencia | Descripción | Captura |
| :--- | :--- | :--- |
| **01. Login Web** | Formulario de sesión web con CSRF activo | [Ver Captura](evidencias/Evidencia1.png) |
| **02. Dashboard** | Panel de tareas bajo sesión de usuario | [Ver Captura](evidencias/PanelTareas2.png) |
| **03. Prevención XSS** | Escape seguro con `th:text` en DevTools | [Ver Captura](evidencias/Evidencia3.png) |
| **04. Mitigación SQLi** | Búsqueda segura con `@Query` parametrizado | [Ver Captura](evidencias/TareasAsignadas4.png) |
| **05. Panel Admin** | Control de acceso restringido `ROLE_ADMIN` | [Ver Captura](evidencias/PanelAdm5.png) |
| **06. Token JWT** | Generación de token JWT en `/api/auth/login` | [Ver Captura](evidencias/PruebaPost1.png) |
| **07. API Protegida** | Consulta `/api/tareas` con Bearer Token | [Ver Captura](evidencias/Preubas2.png) |

Para ver el reporte completo con imágenes desplegadas, consulte [evidencias/README.md](evidencias/README.md).