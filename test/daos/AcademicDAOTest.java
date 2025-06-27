package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.AcademicDAO;
import logic.daos.UserDAO;
import logic.enums.AcademicType;
import logic.exceptions.RepeatedStaffNumberException;
import logic.logicclasses.Academic;
import logic.logicclasses.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcademicDAOTest {
    private static AcademicDAO academicDAO;
    private static UserDAO userDAO;
    private static List<Academic> testAcademics;

    @BeforeAll
    static void setUpAll() throws SQLException {
        academicDAO = new AcademicDAO();
        userDAO = new UserDAO();

        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE grupo_estudiante");
            statement.execute("TRUNCATE TABLE grupo_academico");
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

        testAcademics = List.of(
                createTestAcademic("Académico 1", "11111", AcademicType.Evaluador),
                createTestAcademic("Académico 2", "22222", AcademicType.EE),
                createTestAcademic("Académico 3", "33333", AcademicType.Evaluador)
        );
    }

    private static Academic createTestAcademic(String name, String staffNumber, AcademicType type) throws SQLException {
        User user = new User();
        user.setFullName(name);
        user.setCellphone("55500" + staffNumber);
        user.setStatus('A');
        userDAO.addUser(user);

        Academic academic = new Academic(
                user.getIdUser(),
                user.getFullName(),
                user.getCellPhone(),
                user.getPhoneExtension(),
                user.getStatus(),
                staffNumber,
                type
        );
        try {
            academicDAO.addAcademic(academic);
        } catch (RepeatedStaffNumberException ignore) {}
        return academic;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM academico WHERE id_usuario > 3");
            stmt.execute("DELETE FROM usuario WHERE id_usuario > 3");
        }
    }

    @Test
    void testAddAcademic_Success() throws SQLException, RepeatedStaffNumberException {
        User newUser = new User();
        newUser.setFullName("Nuevo Académico");
        newUser.setCellphone("5554444444");
        newUser.setStatus('A');
        userDAO.addUser(newUser);

        Academic newAcademic = new Academic(
                newUser.getIdUser(),
                newUser.getFullName(),
                newUser.getCellPhone(),
                newUser.getPhoneExtension(),
                newUser.getStatus(),
                "444444",
                AcademicType.Evaluador
        );

        academicDAO.addAcademic(newAcademic);
        Academic retrieved = academicDAO.getAcademicByStaffNumber("444444");
        assertEquals(newAcademic, retrieved);
    }

    @Test
    void testAddAcademic_NullAcademic() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.addAcademic(null));
    }

    @Test
    void testAddAcademic_DuplicateStaffNumber_ShouldThrowException() throws SQLException {
        Academic duplicateAcademic = new Academic(
                -1,
                "Nombre",
                "5555555555",
                "",
                'A',
                "11111",
                AcademicType.Evaluador
        );
        assertThrows(RepeatedStaffNumberException.class, () -> academicDAO.addAcademic(duplicateAcademic));
    }

    @Test
    void testGetAllAcademics_WithData() throws SQLException {
        List<Academic> academics = academicDAO.getAllAcademics();
        assertFalse(academics.isEmpty());
    }

    @Test
    void testGetAllAcademics_EmptyTable() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM academico");
        }
        List<Academic> academics = academicDAO.getAllAcademics();
        assertTrue(academics.isEmpty());
        setUpAll();
    }

    @Test
    void testGetAcademicByStaffNumber_Exists() throws SQLException {
        Academic testAcademic = testAcademics.get(0);
        Academic found = academicDAO.getAcademicByStaffNumber(testAcademic.getStaffNumber());
        assertEquals(testAcademic, found);
    }

    @Test
    void testGetAcademicByStaffNumber_NotExists() throws SQLException {
        Academic found = academicDAO.getAcademicByStaffNumber("999999");
        assertEquals(new Academic(-1, "", "", "",'I', "", AcademicType.NONE), found);
    }

    @Test
    void testGetAcademicByStaffNumber_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.getAcademicByStaffNumber(null));
        assertThrows(IllegalArgumentException.class, () -> academicDAO.getAcademicByStaffNumber(""));
    }

    @Test
    void testUpdateAcademic_Success() throws SQLException {
        Academic toUpdate = testAcademics.get(1);
        toUpdate.setAcademicType(AcademicType.Evaluador);

        academicDAO.updateAcademic(toUpdate);
        Academic updated = academicDAO.getAcademicByStaffNumber(toUpdate.getStaffNumber());
        assertEquals(toUpdate, updated);
    }

    @Test
    void testUpdateAcademic_NullAcademic() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.updateAcademic(null));
    }

    @Test
    void testUpdateAcademic_NotExists() throws SQLException {
        Academic fake = new Academic(9999, "Fake", "5550000000", "", 'A', "99999", AcademicType.EE);
        assertFalse(academicDAO.updateAcademic(fake));
    }

    @Test
    void testDeleteAcademic_Success() throws SQLException {
        Academic toDelete = testAcademics.get(2);
        academicDAO.deleteAcademic(toDelete);
        Academic deleted = academicDAO.getAcademicByStaffNumber(toDelete.getStaffNumber());
        assertEquals(new Academic(-1, "", "", "",'I', "", AcademicType.NONE), deleted);
    }

    @Test
    void testDeleteAcademic_NullAcademic() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.deleteAcademic(null));
    }

    @Test
    void testDeleteAcademic_NotExists() throws SQLException {
        Academic fake = new Academic(9999, "Fake", "5550000000", "" ,'A', "99999", AcademicType.EE);
        assertFalse(academicDAO.deleteAcademic(fake));
    }

    @Test
    void testGetAllAcademicsByType_Correct() throws SQLException {
        List<Academic> evaluadores = academicDAO.getAllAcademicsByType(AcademicType.Evaluador);
        assertEquals(2, evaluadores.size());
    }

    @Test
    void testGetAllAcademicsByType_EmptyResult() throws SQLException {
        List<Academic> none = academicDAO.getAllAcademicsByType(AcademicType.NONE);
        assertTrue(none.isEmpty());
    }

    @Test
    void testGetAllAcademicsByType_NullType() throws SQLException {
        List<Academic> academics = academicDAO.getAllAcademicsByType(null);
        assertTrue(academics.isEmpty());
    }

    @Test
    void testGetAcademicById_Exists() throws SQLException {
        Academic testAcademic = testAcademics.get(0);
        Academic found = academicDAO.getAcademicById(testAcademic.getIdUser());
        assertEquals(testAcademic, found);
    }

    @Test
    void testGetAcademicById_NotExists() throws SQLException {
        Academic found = academicDAO.getAcademicById(9999);
        assertEquals(new Academic(-1, "", "", "",'I', "", AcademicType.NONE), found);
    }

    @Test
    void testAcademicExists_True() throws SQLException {
        Academic testAcademic = testAcademics.get(0);
        assertTrue(academicDAO.academicExists(testAcademic.getStaffNumber()));
    }

    @Test
    void testAcademicExists_False() throws SQLException {
        assertFalse(academicDAO.academicExists("00000"));
    }

    @Test
    void testAcademicExists_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.academicExists(null));
        assertThrows(IllegalArgumentException.class, () -> academicDAO.academicExists(""));
    }

    @Test
    void testCountAcademics_WithData() throws SQLException {
        int count = academicDAO.countAcademics();
        assertEquals(3, count);
    }

    @Test
    void testCountAcademics_EmptyTable() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM academico");
        }
        assertEquals(0, academicDAO.countAcademics());
        setUpAll();
    }

    @Test
    void testChangeAcademicType_Success() throws SQLException {
        Academic academic = testAcademics.get(0);
        academic.setAcademicType(AcademicType.EE);

        academicDAO.changeAcademicType(academic);
        Academic updated = academicDAO.getAcademicByStaffNumber(academic.getStaffNumber());
        assertEquals(academic, updated);
    }

    @Test
    void testChangeAcademicType_NullAcademic() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.changeAcademicType(null));
    }

    @Test
    void testChangeAcademicType_NotExists() throws SQLException {
        Academic fake = new Academic(9999, "Fake", "5550000000", "",'A', "99999", AcademicType.EE);
        assertFalse(academicDAO.changeAcademicType(fake));
    }

    @Test
    void testStaffNumberExists_True() throws Exception {
        Academic testAcademic = testAcademics.get(0);
        assertTrue(academicDAO.staffNumberExists(testAcademic.getStaffNumber()));
    }

    @Test
    void testStaffNumberExists_False() throws Exception {
        assertFalse(academicDAO.staffNumberExists("00000"));
    }

    @Test
    void testStaffNumberExists_NullOrEmpty() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> academicDAO.staffNumberExists(null));
        assertThrows(IllegalArgumentException.class, () -> academicDAO.staffNumberExists(""));
    }

    @Test
    void testGetAcademicsByStatusFromView_Correct() throws SQLException {
        List<Academic> academics = academicDAO.getAcademicsByStatusFromView('A');
        assertNotNull(academics);
    }

    @Test
    void testGetAcademicsByStatusFromView_NoResults() throws SQLException {
        List<Academic> academics = academicDAO.getAcademicsByStatusFromView('Z');
        assertTrue(academics.isEmpty());
    }
}
