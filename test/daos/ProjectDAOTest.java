package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ProjectDAO;
import logic.logicclasses.Project;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDAOTest {
    private static ProjectDAO projectDAO;
    private static Connection testConnection;
    private static List<Project> testProjects;

    @BeforeAll
    static void setUpAll() throws SQLException {
        projectDAO = new ProjectDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE grupo_estudiante");
            statement.execute("TRUNCATE TABLE estudiante");
            statement.execute("TRUNCATE TABLE academico");
            statement.execute("TRUNCATE TABLE coordinador");
            statement.execute("TRUNCATE TABLE representante");
            statement.execute("TRUNCATE TABLE actividad");
            statement.execute("TRUNCATE TABLE autoevaluacion");
            statement.execute("TRUNCATE TABLE cronograma_actividad");
            statement.execute("TRUNCATE TABLE cronograma_actividades");
            statement.execute("TRUNCATE TABLE evaluacion");
            statement.execute("TRUNCATE TABLE presentacion");
            statement.execute("TRUNCATE TABLE proyecto");
            statement.execute("TRUNCATE TABLE reporte");
            statement.execute("TRUNCATE TABLE grupo");
            statement.execute("TRUNCATE TABLE organizacion_vinculada");
            statement.execute("TRUNCATE TABLE cuenta");
            statement.execute("TRUNCATE TABLE usuario");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        testProjects = new ArrayList<>();
        testProjects.add(createTestProyect("Proyecto 1", "Descripción proyecto 1",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(86400)), 'A'));
        testProjects.add(createTestProyect("Proyecto 2", "Descripción proyecto 2",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(172800)), 'P'));
        assertEquals(2, projectDAO.countProyects());
    }

    private static Project createTestProyect(String title, String description,
                                             Timestamp startDate, Timestamp endDate, char status) throws SQLException {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setDateStart(startDate);
        project.setDateEnd(endDate);
        project.setStatus(status);
        projectDAO.addProyect(project);
        return project;
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
        testProjects = new ArrayList<>();
        testProjects.add(createTestProyect("Proyecto 1", "Descripción proyecto 1",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(86400)), 'A'));
        testProjects.add(createTestProyect("Proyecto 2", "Descripción proyecto 2",
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plusSeconds(172800)), 'P'));
    }

    @Test
    void testAddProyect_Success() throws SQLException {
        Project project = new Project();
        project.setTitle("Nuevo Proyecto");
        project.setDescription("Descripción del nuevo proyecto");
        project.setDateStart(Timestamp.from(Instant.now()));
        project.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        project.setStatus('A');
        project.setCapacity(10);
        project.setCurrentStudents(0);

        projectDAO.addProyect(project);

        Project fetched = projectDAO.getProyectByTitle(project.getTitle());

        assertEquals(project, fetched);
    }

    @Test
    void testAddProyect_NullProyect() {
        assertThrows(IllegalArgumentException.class, () -> {
            projectDAO.addProyect(null);
        });
    }

    @Test
    void testAddProyect_NullFields_ShouldThrowException() {
        Project invalidProject = new Project();
        invalidProject.setTitle(null);
        invalidProject.setDescription(null);
        invalidProject.setDateStart(null);
        invalidProject.setDateEnd(null);
        invalidProject.setStatus('\0');
        assertThrows(SQLException.class, () -> projectDAO.addProyect(invalidProject));
    }

    @Test
    void testUpdateProyect_Success() throws SQLException {
        Project projectToUpdate = testProjects.get(0);
        projectToUpdate.setTitle("Título actualizado");
        projectToUpdate.setDescription("Descripción actualizada");
        projectToUpdate.setDateEnd(Timestamp.from(Instant.now().plusSeconds(259200)));
        projectToUpdate.setStatus('I');

        boolean result = projectDAO.updateProyect(projectToUpdate);
        assertTrue(result);

        Project updatedProject = projectDAO.getProyectById(projectToUpdate.getIdProyect());
        assertEquals(projectToUpdate, updatedProject);
    }

    @Test
    void testUpdateProyect_NotExists() throws SQLException {
        Project nonExistentProject = new Project();
        nonExistentProject.setIdProyect(9999);
        nonExistentProject.setTitle("Proyecto inexistente");
        nonExistentProject.setDescription("Este proyecto no existe");
        nonExistentProject.setDateStart(Timestamp.from(Instant.now()));
        nonExistentProject.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        nonExistentProject.setStatus('A');

        boolean result = projectDAO.updateProyect(nonExistentProject);
        assertFalse(result);
    }

    @Test
    void testUpdateProyect_NullProyect() throws SQLException {
        assertFalse(projectDAO.updateProyect(null));
    }

    @Test
    void testDeleteProyect_Success() throws SQLException {
        Project testProject = testProjects.get(0);
        int countBefore = projectDAO.countProyects();
        boolean result = projectDAO.deleteProyect(testProject);

        assertTrue(result);
        assertEquals(countBefore - 1, projectDAO.countProyects());
        assertFalse(projectDAO.proyectExists(testProject.getTitle()));
    }

    @Test
    void testDeleteProyect_NotExists() throws SQLException {
        Project nonExistentProject = new Project();
        nonExistentProject.setIdProyect(9999);

        int initialCount = projectDAO.countProyects();
        boolean result = projectDAO.deleteProyect(nonExistentProject);

        assertFalse(result);
        assertEquals(initialCount, projectDAO.countProyects());
    }

    @Test
    void testDeleteProyect_NullProyect() throws SQLException {
        assertFalse(projectDAO.deleteProyect(null));
    }

    @Test
    void testGetAllProyects_WithData() throws SQLException {
        List<Project> projects = projectDAO.getAllProyects();
        assertEquals(testProjects.size(), projects.size());
        for (Project testProject : testProjects) {
            boolean found = projects.stream().anyMatch(p -> p.equals(testProject));
            assertTrue(found);
        }
    }

    @Test
    void testGetAllProyects_EmptyTable() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM proyecto");
        }
        List<Project> projects = projectDAO.getAllProyects();
        assertTrue(projects.isEmpty());
        setUp();
    }

    @Test
    void testGetProyectsByStatus_WithData() throws SQLException {
        List<Project> activos = projectDAO.getProyectsByStatus('A');
        assertEquals(1, activos.size());
        List<Project> pendientes = projectDAO.getProyectsByStatus('P');
        assertEquals(1, pendientes.size());
    }

    @Test
    void testGetProyectsByStatus_EmptyResult() throws SQLException {
        List<Project> cancelados = projectDAO.getProyectsByStatus('C');
        assertTrue(cancelados.isEmpty());
    }

    @Test
    void testGetProyectById_Exists() throws SQLException {
        Project testProject = testProjects.get(0);
        Project foundProject = projectDAO.getProyectById(testProject.getIdProyect());
        assertEquals(testProject, foundProject);
    }

    @Test
    void testGetProyectById_NotExists() throws SQLException {
        Project foundProject = projectDAO.getProyectById(9999);
        assertEquals(new Project(-1, "", "", null, null, 'I', 0, 0), foundProject);
    }

    @Test
    void testGetProyectById_InvalidId() throws SQLException {
        Project foundProject = projectDAO.getProyectById(-1);
        assertEquals(new Project(-1, "", "", null, null, 'I', 0, 0), foundProject);
    }

    @Test
    void testGetProyectByTitle_Exists() throws SQLException {
        Project testProject = testProjects.get(0);
        Project foundProject = projectDAO.getProyectByTitle(testProject.getTitle());
        assertEquals(testProject, foundProject);
    }

    @Test
    void testGetProyectByTitle_NotExists() throws SQLException {
        Project foundProject = projectDAO.getProyectByTitle("Título inexistente");
        assertEquals(new Project(-1, "", "", null, null, 'I', 0, 0), foundProject);
    }

    @Test
    void testGetProyectByTitle_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> projectDAO.getProyectByTitle(null));
        assertThrows(IllegalArgumentException.class, () -> projectDAO.getProyectByTitle(""));
    }

    @Test
    void testCountProyects_WithData() throws SQLException {
        int count = projectDAO.countProyects();
        assertEquals(testProjects.size(), count);

        Project extraProject = new Project();
        extraProject.setTitle("Proyecto adicional");
        extraProject.setDescription("Descripción adicional");
        extraProject.setDateStart(Timestamp.from(Instant.now()));
        extraProject.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        extraProject.setStatus('A');
        projectDAO.addProyect(extraProject);

        assertEquals(count + 1, projectDAO.countProyects());
    }

    @Test
    void testCountProyects_EmptyTable() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM proyecto");
        }
        assertEquals(0, projectDAO.countProyects());
        setUp();
    }

    @Test
    void testProyectExists_True() throws SQLException {
        Project testProject = testProjects.get(0);
        assertTrue(projectDAO.proyectExists(testProject.getTitle()));
    }

    @Test
    void testProyectExists_False() throws SQLException {
        assertFalse(projectDAO.proyectExists("Título inexistente"));
    }

    @Test
    void testProyectExists_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> projectDAO.proyectExists(null));
        assertThrows(IllegalArgumentException.class, () -> projectDAO.proyectExists(""));
    }

    @Test
    void testChangeProyectStatus_Success() throws SQLException {
        Project testProject = testProjects.get(0);
        testProject.setStatus('I');

        boolean result = projectDAO.changeProyectStatus(testProject);
        assertTrue(result);

        Project updatedProject = projectDAO.getProyectById(testProject.getIdProyect());
        assertEquals(testProject, updatedProject);
    }

    @Test
    void testChangeProyectStatus_NotExists() throws SQLException {
        Project nonExistentProject = new Project();
        nonExistentProject.setIdProyect(9999);
        nonExistentProject.setStatus('I');

        boolean result = projectDAO.changeProyectStatus(nonExistentProject);
        assertFalse(result);
    }

    @Test
    void testChangeProyectStatus_NullProyect() throws SQLException {
        assertFalse(projectDAO.changeProyectStatus(null));
    }
}
