package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.RepresentativeDAO;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepresentativeDAOTest {
    private static RepresentativeDAO representativeDAO;
    private static LinkedOrganizationDAO organizationDAO;
    private static Connection testConnection;
    private static List<Representative> testRepresentatives;
    private static LinkedOrganization testOrganization;

    @BeforeAll
    static void setUpAll() throws SQLException {
        representativeDAO = new RepresentativeDAO();
        organizationDAO = new LinkedOrganizationDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var connection = ConnectionDataBase.getConnection();
             var statement = connection.createStatement()) {
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

        testOrganization = new LinkedOrganization();
        testOrganization.setNameLinkedOrganization("Empresa Prueba");
        testOrganization.setCellPhoneLinkedOrganization("5551234567");
        testOrganization.setEmailLinkedOrganization("contacto@empresaprueba.com");
        testOrganization.setStatus('A');
        organizationDAO.addLinkedOrganization(testOrganization);

        testRepresentatives = List.of(
                createTestRepresentative("Juan Pérez", "juan@empresa.com", "5551111111", testOrganization),
                createTestRepresentative("María García", "maria@empresa.com", "5552222222", testOrganization)
        );
    }

    private static Representative createTestRepresentative(String name, String email, String phone,
                                                           LinkedOrganization org) throws SQLException {
        Representative rep = new Representative();
        rep.setFullName(name);
        rep.setEmail(email);
        rep.setCellPhone(phone);
        rep.setLinkedOrganization(org);
        representativeDAO.addRepresentative(rep);
        return rep;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM representante");
            statement.execute("ALTER TABLE representante AUTO_INCREMENT = 1");
        }

        testRepresentatives = List.of(
                createTestRepresentative("Juan Pérez", "juan@empresa.com", "5551111111", testOrganization),
                createTestRepresentative("María García", "maria@empresa.com", "5552222222", testOrganization)
        );
    }

    @Test
    void testAddRepresentative_Success() throws SQLException {
        Representative newRep = new Representative();
        newRep.setFullName("Nuevo Representante");
        newRep.setEmail("nuevo@empresa.com");
        newRep.setCellPhone("5559999999");
        newRep.setLinkedOrganization(testOrganization);

        int initialCount = representativeDAO.countRepresentatives();
        boolean result = representativeDAO.addRepresentative(newRep);

        assertTrue(result);
        assertEquals(initialCount + 1, representativeDAO.countRepresentatives());
        assertTrue(newRep.getIdRepresentative() > 0);

        Representative addedRep = representativeDAO.getRepresentativeById(newRep.getIdRepresentative());
        assertEquals(newRep, addedRep);
    }

    @Test
    void testAddRepresentative_NullFields_ShouldThrowException() {
        Representative invalidRep = new Representative();
        invalidRep.setFullName(null);
        invalidRep.setEmail("test@test.com");
        invalidRep.setLinkedOrganization(testOrganization);

        assertThrows(IllegalArgumentException.class,
                () -> representativeDAO.addRepresentative(invalidRep));
    }

    @Test
    void testAddRepresentative_Error() {
        Representative invalidRep = new Representative();
        invalidRep.setFullName(null);
        invalidRep.setEmail("test@test.com");
        invalidRep.setLinkedOrganization(testOrganization);

        assertThrows(IllegalArgumentException.class,
                () -> representativeDAO.addRepresentative(invalidRep));
    }

    @Test
    void testAddRepresentative_Exception() {
        assertThrows(IllegalArgumentException.class, () -> representativeDAO.addRepresentative(null));
    }

    @Test
    void testGetRepresentativeById_Exists() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        Representative foundRep = representativeDAO.getRepresentativeById(testRep.getIdRepresentative());
        assertEquals(testRep, foundRep);
    }

    @Test
    void testGetRepresentativeById_NotExists() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeById(9999);
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetRepresentativeById_Error() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeById(9999);
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetRepresentativeById_Exception() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeById(-1);
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetAllRepresentatives_WithData() throws SQLException {
        List<Representative> representatives = representativeDAO.getAllRepresentatives();
        assertEquals(testRepresentatives.size(), representatives.size());
        for (Representative testRep : testRepresentatives) {
            boolean found = representatives.stream().anyMatch(r -> r.equals(testRep));
            assertTrue(found);
        }
    }

    @Test
    void testGetAllRepresentatives_Success() throws SQLException {
        List<Representative> representatives = representativeDAO.getAllRepresentatives();
        assertEquals(testRepresentatives.size(), representatives.size());
        for (Representative testRep : testRepresentatives) {
            boolean found = representatives.stream().anyMatch(r -> r.equals(testRep));
            assertTrue(found);
        }
    }

    @Test
    void testGetAllRepresentatives_Error() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM representante");
        }
        List<Representative> representatives = representativeDAO.getAllRepresentatives();
        assertTrue(representatives.isEmpty());
    }

    @Test
    void testGetAllRepresentatives_Exception() throws SQLException {
        List<Representative> representatives = representativeDAO.getAllRepresentatives();
        assertNotNull(representatives);
    }

    @Test
    void testGetRepresentativeByEmail_Exists() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        Representative foundRep = representativeDAO.getRepresentativeByEmail(testRep.getEmail());
        assertEquals(testRep, foundRep);
    }

    @Test
    void testGetRepresentativeByEmail_NotExists() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeByEmail("noexiste@empresa.com");
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetRepresentativeByEmail_Success() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        Representative foundRep = representativeDAO.getRepresentativeByEmail(testRep.getEmail());
        assertEquals(testRep, foundRep);
    }

    @Test
    void testGetRepresentativeByEmail_Error() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeByEmail("noexiste@empresa.com");
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetRepresentativeByEmail_Exception() throws SQLException {
        Representative foundRep = representativeDAO.getRepresentativeByEmail(null);
        assertEquals(new Representative(-1, "", "", "", null), foundRep);
    }

    @Test
    void testGetRepresentativesByOrganization_Exists() throws SQLException {
        List<Representative> reps = representativeDAO.getRepresentativesByOrganization(
                testOrganization.getIdLinkedOrganization());
        assertEquals(testRepresentatives.size(), reps.size());
        for (Representative rep : reps) {
            assertEquals(testOrganization, rep.getLinkedOrganization());
        }
    }

    @Test
    void testGetRepresentativesByOrganization_NotExists() throws SQLException {
        List<Representative> reps = representativeDAO.getRepresentativesByOrganization(9999);
        assertTrue(reps.isEmpty());
    }

    @Test
    void testGetRepresentativesByOrganization_Success() throws SQLException {
        List<Representative> reps = representativeDAO.getRepresentativesByOrganization(
                testOrganization.getIdLinkedOrganization());
        assertEquals(testRepresentatives.size(), reps.size());
        for (Representative rep : reps) {
            assertEquals(testOrganization, rep.getLinkedOrganization());
        }
    }

    @Test
    void testGetRepresentativesByOrganization_Error() throws SQLException {
        List<Representative> reps = representativeDAO.getRepresentativesByOrganization(9999);
        assertTrue(reps.isEmpty());
    }

    @Test
    void testGetRepresentativesByOrganization_Exception() throws SQLException {
        List<Representative> reps = representativeDAO.getRepresentativesByOrganization(-1);
        assertTrue(reps.isEmpty());
    }

    @Test
    void testUpdateRepresentative_Success() throws SQLException {
        Representative repToUpdate = testRepresentatives.get(0);
        repToUpdate.setFullName("Nombre Actualizado");
        repToUpdate.setEmail("nuevo@email.com");
        repToUpdate.setCellPhone("5550000000");
        boolean result = representativeDAO.updateRepresentative(repToUpdate);
        assertTrue(result);
        Representative updatedRep = representativeDAO.getRepresentativeById(repToUpdate.getIdRepresentative());
        assertEquals(repToUpdate, updatedRep);
    }

    @Test
    void testUpdateRepresentative_NotExists() throws SQLException {
        Representative nonExistentRep = new Representative();
        nonExistentRep.setIdRepresentative(9999);
        nonExistentRep.setFullName("No existe");
        nonExistentRep.setEmail("noexiste@test.com");
        nonExistentRep.setLinkedOrganization(testOrganization);
        boolean result = representativeDAO.updateRepresentative(nonExistentRep);
        assertFalse(result);
    }

    @Test
    void testUpdateRepresentative_Error() throws SQLException {
        Representative nonExistentRep = new Representative();
        nonExistentRep.setIdRepresentative(9999);
        nonExistentRep.setFullName("No existe");
        nonExistentRep.setEmail("noexiste@test.com");
        nonExistentRep.setLinkedOrganization(testOrganization);
        boolean result = representativeDAO.updateRepresentative(nonExistentRep);
        assertFalse(result);
    }

    @Test
    void testUpdateRepresentative_Exception() {
        assertThrows(IllegalArgumentException.class, () -> representativeDAO.updateRepresentative(null));
    }

    @Test
    void testDeleteRepresentative_Success() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        int repId = testRep.getIdRepresentative();
        int countBefore = representativeDAO.countRepresentatives();
        boolean result = representativeDAO.deleteRepresentative(testRep);
        assertTrue(result);
        assertEquals(countBefore - 1, representativeDAO.countRepresentatives());
        assertEquals(new Representative(-1, "", "", "", null), representativeDAO.getRepresentativeById(repId));
    }

    @Test
    void testDeleteRepresentative_NotExists() throws SQLException {
        Representative nonExistentRep = new Representative();
        nonExistentRep.setIdRepresentative(9999);
        int initialCount = representativeDAO.countRepresentatives();
        boolean result = representativeDAO.deleteRepresentative(nonExistentRep);
        assertFalse(result);
        assertEquals(initialCount, representativeDAO.countRepresentatives());
    }

    @Test
    void testDeleteRepresentative_Error() throws SQLException {
        Representative nonExistentRep = new Representative();
        nonExistentRep.setIdRepresentative(9999);
        int initialCount = representativeDAO.countRepresentatives();
        boolean result = representativeDAO.deleteRepresentative(nonExistentRep);
        assertFalse(result);
        assertEquals(initialCount, representativeDAO.countRepresentatives());
    }

    @Test
    void testDeleteRepresentative_Exception() {
        assertThrows(IllegalArgumentException.class, () -> representativeDAO.deleteRepresentative(null));
    }

    @Test
    void testRepresentativeExists_True() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        assertTrue(representativeDAO.representativeExists(testRep.getEmail()));
    }

    @Test
    void testRepresentativeExists_False() throws SQLException {
        assertFalse(representativeDAO.representativeExists("noexiste@empresa.com"));
    }

    @Test
    void testRepresentativeExists_Success() throws SQLException {
        Representative testRep = testRepresentatives.get(0);
        assertTrue(representativeDAO.representativeExists(testRep.getEmail()));
    }

    @Test
    void testRepresentativeExists_Error() throws SQLException {
        assertFalse(representativeDAO.representativeExists("noexiste@empresa.com"));
    }

    @Test
    void testRepresentativeExists_Exception() throws SQLException {
        assertFalse(representativeDAO.representativeExists(null));
    }

    @Test
    void testCountRepresentatives_WithData() throws SQLException {
        int count = representativeDAO.countRepresentatives();
        assertEquals(testRepresentatives.size(), count);
        Representative extraRep = new Representative();
        extraRep.setFullName("Extra Rep");
        extraRep.setEmail("extra@empresa.com");
        extraRep.setLinkedOrganization(testOrganization);
        representativeDAO.addRepresentative(extraRep);
        assertEquals(count + 1, representativeDAO.countRepresentatives());
    }

    @Test
    void testCountRepresentatives_Success() throws SQLException {
        int count = representativeDAO.countRepresentatives();
        assertEquals(testRepresentatives.size(), count);
        Representative extraRep = new Representative();
        extraRep.setFullName("Extra Rep");
        extraRep.setEmail("extra@empresa.com");
        extraRep.setLinkedOrganization(testOrganization);
        representativeDAO.addRepresentative(extraRep);
        assertEquals(count + 1, representativeDAO.countRepresentatives());
    }

    @Test
    void testCountRepresentatives_Error() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM representante");
        }
        assertEquals(0, representativeDAO.countRepresentatives());
    }

    @Test
    void testCountRepresentatives_Exception() throws SQLException {
        int count = representativeDAO.countRepresentatives();
        assertTrue(count >= 0);
    }
}
