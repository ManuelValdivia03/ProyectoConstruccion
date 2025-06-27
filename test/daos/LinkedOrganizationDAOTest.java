package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.LinkedOrganizationDAO;
import logic.logicclasses.LinkedOrganization;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkedOrganizationDAOTest {
    private static LinkedOrganizationDAO linkedOrganizationDAO;
    private static Connection testConnection;
    private static List<LinkedOrganization> testOrganizations;

    @BeforeAll
    static void setUpAll() throws SQLException {
        linkedOrganizationDAO = new LinkedOrganizationDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE documentos_organizacion");
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

        testOrganizations = new ArrayList<>();
        testOrganizations.add(createTestOrganization("Empresa A", "5551234567", "contacto@empresaA.com", 'A'));
        testOrganizations.add(createTestOrganization("Empresa B", "5557654321", "contacto@empresaB.com", 'A'));
    }

    private static LinkedOrganization createTestOrganization(String name, String phone, String email, char status) throws SQLException {
        LinkedOrganization org = new LinkedOrganization();
        org.setNameLinkedOrganization(name);
        org.setCellPhoneLinkedOrganization(phone);
        org.setEmailLinkedOrganization(email);
        org.setStatus(status);
        linkedOrganizationDAO.addLinkedOrganization(org);
        return org;
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
            stmt.execute("DELETE FROM organizacion_vinculada");
            stmt.execute("ALTER TABLE organizacion_vinculada AUTO_INCREMENT = 1");
        }
        testOrganizations = new ArrayList<>();
        testOrganizations.add(createTestOrganization("Empresa A", "5551234567", "contacto@empresaA.com", 'A'));
        testOrganizations.add(createTestOrganization("Empresa B", "5557654321", "contacto@empresaB.com", 'A'));
    }

    @Test
    void testAddLinkedOrganization_Success() throws SQLException {
        LinkedOrganization newOrg = new LinkedOrganization();
        newOrg.setNameLinkedOrganization("Nueva Empresa");
        newOrg.setCellPhoneLinkedOrganization("5559999999");
        newOrg.setEmailLinkedOrganization("info@nuevaempresa.com");
        newOrg.setStatus('A');
        int initialCount = linkedOrganizationDAO.countLinkedOrganizations();
        boolean result = linkedOrganizationDAO.addLinkedOrganization(newOrg);
        assertTrue(result);
        assertEquals(initialCount + 1, linkedOrganizationDAO.countLinkedOrganizations());
        assertTrue(newOrg.getIdLinkedOrganization() > 0);
        LinkedOrganization addedOrg = linkedOrganizationDAO.getLinkedOrganizationByID(newOrg.getIdLinkedOrganization());
        assertEquals(newOrg, addedOrg);
    }

    @Test
    void testAddLinkedOrganization_NullName_ShouldThrowException() {
        LinkedOrganization invalidOrg = new LinkedOrganization();
        invalidOrg.setNameLinkedOrganization(null);
        invalidOrg.setCellPhoneLinkedOrganization("5551111111");
        invalidOrg.setEmailLinkedOrganization("test@test.com");
        assertThrows(SQLException.class, () -> linkedOrganizationDAO.addLinkedOrganization(invalidOrg));
    }

    @Test
    void testGetLinkedOrganizationByID_Exists() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        LinkedOrganization foundOrg = linkedOrganizationDAO.getLinkedOrganizationByID(testOrg.getIdLinkedOrganization());
        assertEquals(testOrg, foundOrg);
    }

    @Test
    void testGetLinkedOrganizationByID_NotExists() throws SQLException {
        LinkedOrganization foundOrg = linkedOrganizationDAO.getLinkedOrganizationByID(9999);
        assertEquals(new LinkedOrganization(-1, "", "", "", "", "", 'I'), foundOrg);
    }

    @Test
    void testGetAllLinkedOrganizations_WithData() throws SQLException {
        List<LinkedOrganization> organizations = linkedOrganizationDAO.getAllLinkedOrganizations();
        assertEquals(testOrganizations.size(), organizations.size());
    }

    @Test
    void testGetLinkedOrganizationByTitle_Exists() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        LinkedOrganization foundOrg = linkedOrganizationDAO.getLinkedOrganizationByTitle(testOrg.getNameLinkedOrganization());
        assertEquals(testOrg, foundOrg);
    }

    @Test
    void testGetLinkedOrganizationByTitle_NotExists() throws SQLException {
        LinkedOrganization foundOrg = linkedOrganizationDAO.getLinkedOrganizationByTitle("Empresa Inexistente");
        assertEquals(new LinkedOrganization(-1, "", "", "", "", "", 'I'), foundOrg);
    }

    @Test
    void testUpdateLinkedOrganization_Success() throws SQLException {
        LinkedOrganization orgToUpdate = testOrganizations.get(0);
        orgToUpdate.setNameLinkedOrganization("Empresa Actualizada");
        orgToUpdate.setCellPhoneLinkedOrganization("5550000000");
        orgToUpdate.setEmailLinkedOrganization("nuevo@email.com");
        orgToUpdate.setStatus('A');
        boolean result = linkedOrganizationDAO.updateLinkedOrganization(orgToUpdate);
        assertTrue(result);
        LinkedOrganization updatedOrg = linkedOrganizationDAO.getLinkedOrganizationByID(orgToUpdate.getIdLinkedOrganization());
        assertEquals(orgToUpdate, updatedOrg);
    }

    @Test
    void testUpdateLinkedOrganization_NotExists() throws SQLException {
        LinkedOrganization nonExistentOrg = new LinkedOrganization();
        nonExistentOrg.setIdLinkedOrganization(9999);
        nonExistentOrg.setNameLinkedOrganization("Empresa Inexistente");
        nonExistentOrg.setCellPhoneLinkedOrganization("5559999999");
        nonExistentOrg.setEmailLinkedOrganization("noexiste@test.com");
        nonExistentOrg.setStatus('A');
        boolean result = linkedOrganizationDAO.updateLinkedOrganization(nonExistentOrg);
        assertFalse(result);
    }

    @Test
    void testDeleteLinkedOrganization_Success() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        int orgId = testOrg.getIdLinkedOrganization();
        int countBefore = linkedOrganizationDAO.countLinkedOrganizations();
        boolean result = linkedOrganizationDAO.deleteLinkedOrganization(testOrg);
        assertTrue(result);
        assertEquals(countBefore - 1, linkedOrganizationDAO.countLinkedOrganizations());
        assertFalse(linkedOrganizationDAO.linkedOrganizationExists(testOrg.getNameLinkedOrganization()));
    }

    @Test
    void testDeleteLinkedOrganization_NotExists() throws SQLException {
        LinkedOrganization nonExistentOrg = new LinkedOrganization();
        nonExistentOrg.setIdLinkedOrganization(9999);
        int initialCount = linkedOrganizationDAO.countLinkedOrganizations();
        boolean result = linkedOrganizationDAO.deleteLinkedOrganization(nonExistentOrg);
        assertFalse(result);
        assertEquals(initialCount, linkedOrganizationDAO.countLinkedOrganizations());
    }

    @Test
    void testLinkedOrganizationExists_True() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        assertTrue(linkedOrganizationDAO.linkedOrganizationExists(testOrg.getNameLinkedOrganization()));
    }

    @Test
    void testLinkedOrganizationExists_False() throws SQLException {
        assertFalse(linkedOrganizationDAO.linkedOrganizationExists("Empresa Inexistente"));
    }

    @Test
    void testCountLinkedOrganizations_WithData() throws SQLException {
        int count = linkedOrganizationDAO.countLinkedOrganizations();
        assertEquals(testOrganizations.size(), count);
        LinkedOrganization extraOrg = new LinkedOrganization();
        extraOrg.setNameLinkedOrganization("Empresa Adicional");
        extraOrg.setCellPhoneLinkedOrganization("5558888888");
        extraOrg.setEmailLinkedOrganization("extra@empresa.com");
        extraOrg.setStatus('A');
        linkedOrganizationDAO.addLinkedOrganization(extraOrg);
        assertEquals(count + 1, linkedOrganizationDAO.countLinkedOrganizations());
    }

    @Test
    void testPhoneNumberExists_True() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        assertTrue(linkedOrganizationDAO.phoneNumberExists(testOrg.getCellPhoneLinkedOrganization()));
    }

    @Test
    void testPhoneNumberExists_False() throws SQLException {
        assertFalse(linkedOrganizationDAO.phoneNumberExists("0000000000"));
    }

    @Test
    void testEmailExists_True() throws SQLException {
        LinkedOrganization testOrg = testOrganizations.get(0);
        assertTrue(linkedOrganizationDAO.emailExists(testOrg.getEmailLinkedOrganization()));
    }

    @Test
    void testEmailExists_False() throws SQLException {
        assertFalse(linkedOrganizationDAO.emailExists("noexiste@email.com"));
    }
}
