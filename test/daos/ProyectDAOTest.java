package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ProyectDAO;
import logic.logicclasses.Proyect;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectDAOTest {
    private static ProyectDAO proyectDAO;
    private static Connection testConnection;
    private static List<Proyect> testProyects;

    @BeforeAll
    static void setUpAll() throws SQLException {
        proyectDAO = new ProyectDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Limpiar completamente la base de datos
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM proyecto");
            statement.execute("ALTER TABLE proyecto AUTO_INCREMENT = 1");

            // Crear tabla de proyectos si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS proyecto (" +
                    "id_proyecto INT AUTO_INCREMENT PRIMARY KEY, " +
                    "titulo VARCHAR(100) NOT NULL, " +
                    "descripcion TEXT, " +
                    "fecha_inicial TIMESTAMP NOT NULL, " +
                    "fecha_terminal TIMESTAMP, " +
                    "estado CHAR(1) DEFAULT 'A')");
        }

        // Crear proyectos de prueba
        testProyects = new ArrayList<>();
        testProyects.add(createTestProyect("Proyecto 1", "Descripción proyecto 1",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(86400)), 'A'));
        testProyects.add(createTestProyect("Proyecto 2", "Descripción proyecto 2",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(172800)), 'P'));

        // Verificaciones iniciales
        assertEquals(2, proyectDAO.countProyects());
    }

    private static Proyect createTestProyect(String title, String description,
                                             Timestamp startDate, Timestamp endDate, char status) throws SQLException {
        Proyect proyect = new Proyect();
        proyect.setTitle(title);
        proyect.setDescription(description);
        proyect.setDateStart(startDate);
        proyect.setDateEnd(endDate);
        proyect.setStatus(status);
        proyectDAO.addProyect(proyect);
        return proyect;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM proyecto");
            stmt.execute("ALTER TABLE proyecto AUTO_INCREMENT = 1");
        }

        testProyects = new ArrayList<>();
        testProyects.add(createTestProyect("Proyecto 1", "Descripción proyecto 1",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(86400)), 'A'));
        testProyects.add(createTestProyect("Proyecto 2", "Descripción proyecto 2",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(172800)), 'P'));
    }

    @Test
    void testAddProyect_Success() throws SQLException {
        Proyect newProyect = new Proyect();
        newProyect.setTitle("Nuevo Proyecto");
        newProyect.setDescription("Descripción del nuevo proyecto");
        newProyect.setDateStart(Timestamp.from(Instant.now()));
        newProyect.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        newProyect.setStatus('A');

        int initialCount = proyectDAO.countProyects();
        boolean result = proyectDAO.addProyect(newProyect);

        assertTrue(result);
        assertEquals(initialCount + 1, proyectDAO.countProyects());
        assertTrue(newProyect.getIdProyect() > 0);

        Proyect addedProyect = proyectDAO.getProyectById(newProyect.getIdProyect());
        assertNotNull(addedProyect);
        assertEquals("Nuevo Proyecto", addedProyect.getTitle());
        assertEquals("Descripción del nuevo proyecto", addedProyect.getDescription());
    }

    @Test
    void testAddProyect_NullFields_ShouldThrowException() {
        Proyect invalidProyect = new Proyect();
        invalidProyect.setTitle(null);
        invalidProyect.setDescription(null);
        invalidProyect.setDateStart(null);
        invalidProyect.setDateEnd(null);
        invalidProyect.setStatus('\0');

        assertThrows(SQLException.class,
                () -> proyectDAO.addProyect(invalidProyect));
    }

    @Test
    void testGetProyectById_Exists() throws SQLException {
        Proyect testProyect = testProyects.get(0);
        Proyect foundProyect = proyectDAO.getProyectById(testProyect.getIdProyect());

        assertNotNull(foundProyect);
        assertEquals(testProyect.getTitle(), foundProyect.getTitle());
        assertEquals(testProyect.getDescription(), foundProyect.getDescription());

        // Comparar solo la parte de fecha (año, mes, día)
        LocalDate expectedStart = testProyect.getDateStart().toLocalDateTime().toLocalDate();
        LocalDate actualStart = foundProyect.getDateStart().toLocalDateTime().toLocalDate();
        assertEquals(expectedStart, actualStart);

        LocalDate expectedEnd = testProyect.getDateEnd().toLocalDateTime().toLocalDate();
        LocalDate actualEnd = foundProyect.getDateEnd().toLocalDateTime().toLocalDate();
        assertEquals(expectedEnd, actualEnd);

        assertEquals(testProyect.getStatus(), foundProyect.getStatus());
    }

    @Test
    void testGetProyectById_NotExists() throws SQLException {
        Proyect foundProyect = proyectDAO.getProyectById(9999);
        assertNull(foundProyect);
    }

    @Test
    void testGetAllProyects_WithData() throws SQLException {
        List<Proyect> proyects = proyectDAO.getAllProyects();
        assertEquals(testProyects.size(), proyects.size());

        for (Proyect testProyect : testProyects) {
            boolean found = proyects.stream()
                    .anyMatch(p -> p.getIdProyect() == testProyect.getIdProyect());
            assertTrue(found, "No se encontró el proyecto esperado");
        }
    }

    @Test
    void testGetProyectByTitle_Exists() throws SQLException {
        Proyect testProyect = testProyects.get(0);
        Proyect foundProyect = proyectDAO.getProyectByTitle(testProyect.getTitle());

        assertNotNull(foundProyect);
        assertEquals(testProyect.getIdProyect(), foundProyect.getIdProyect());
        assertEquals(testProyect.getTitle(), foundProyect.getTitle());
    }

    @Test
    void testGetProyectByTitle_NotExists() throws SQLException {
        Proyect foundProyect = proyectDAO.getProyectByTitle("Título inexistente");
        assertNull(foundProyect);
    }

    @Test
    void testUpdateProyect_Success() throws SQLException {
        Proyect proyectToUpdate = testProyects.get(0);
        proyectToUpdate.setTitle("Título actualizado");
        proyectToUpdate.setDescription("Descripción actualizada");
        proyectToUpdate.setDateEnd(Timestamp.from(Instant.now().plusSeconds(259200)));
        proyectToUpdate.setStatus('I');

        boolean result = proyectDAO.updateProyect(proyectToUpdate);
        assertTrue(result);

        Proyect updatedProyect = proyectDAO.getProyectById(proyectToUpdate.getIdProyect());
        assertEquals("Título actualizado", updatedProyect.getTitle());
        assertEquals("Descripción actualizada", updatedProyect.getDescription());
        assertEquals('I', updatedProyect.getStatus());
    }

    @Test
    void testUpdateProyect_NotExists() throws SQLException {
        Proyect nonExistentProyect = new Proyect();
        nonExistentProyect.setIdProyect(9999);
        nonExistentProyect.setTitle("Proyecto inexistente");
        nonExistentProyect.setDescription("Este proyecto no existe");
        nonExistentProyect.setDateStart(Timestamp.from(Instant.now()));
        nonExistentProyect.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        nonExistentProyect.setStatus('A');

        boolean result = proyectDAO.updateProyect(nonExistentProyect);
        assertFalse(result);
    }

    @Test
    void testDeleteProyect_Success() throws SQLException {
        Proyect testProyect = testProyects.get(0);
        int proyectId = testProyect.getIdProyect();

        int countBefore = proyectDAO.countProyects();
        boolean result = proyectDAO.deleteProyect(testProyect);

        assertTrue(result);
        assertEquals(countBefore - 1, proyectDAO.countProyects());
        assertFalse(proyectDAO.proyectExists(testProyect.getTitle()));
    }

    @Test
    void testDeleteProyect_NotExists() throws SQLException {
        Proyect nonExistentProyect = new Proyect();
        nonExistentProyect.setIdProyect(9999);

        int initialCount = proyectDAO.countProyects();
        boolean result = proyectDAO.deleteProyect(nonExistentProyect);

        assertFalse(result);
        assertEquals(initialCount, proyectDAO.countProyects());
    }

    @Test
    void testProyectExists_True() throws SQLException {
        Proyect testProyect = testProyects.get(0);
        assertTrue(proyectDAO.proyectExists(testProyect.getTitle()));
    }

    @Test
    void testProyectExists_False() throws SQLException {
        assertFalse(proyectDAO.proyectExists("Título inexistente"));
    }

    @Test
    void testCountProyects_WithData() throws SQLException {
        int count = proyectDAO.countProyects();
        assertEquals(testProyects.size(), count);

        Proyect extraProyect = new Proyect();
        extraProyect.setTitle("Proyecto adicional");
        extraProyect.setDescription("Descripción adicional");
        extraProyect.setDateStart(Timestamp.from(Instant.now()));
        extraProyect.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        extraProyect.setStatus('A');
        proyectDAO.addProyect(extraProyect);

        assertEquals(count + 1, proyectDAO.countProyects());
    }

    @Test
    void testChangeProyectStatus_Success() throws SQLException {
        Proyect testProyect = testProyects.get(0);
        testProyect.setStatus('I');

        boolean result = proyectDAO.changeProyectStatus(testProyect);
        assertTrue(result);

        Proyect updatedProyect = proyectDAO.getProyectById(testProyect.getIdProyect());
        assertEquals('I', updatedProyect.getStatus());
    }

    @Test
    void testChangeProyectStatus_NotExists() throws SQLException {
        Proyect nonExistentProyect = new Proyect();
        nonExistentProyect.setIdProyect(9999);
        nonExistentProyect.setStatus('I');

        boolean result = proyectDAO.changeProyectStatus(nonExistentProyect);
        assertFalse(result);
    }
}