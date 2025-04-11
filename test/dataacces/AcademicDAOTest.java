package dataacces;

import logic.Academic;
import logic.enums.AcademicType;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcademicDAOTest {
    private static AcademicDAO academicDAO;
    private static Connection testConnection;
    private static List<Academic> testAcademics;

    @BeforeAll
    static void setUpAll() throws SQLException {
        academicDAO = new AcademicDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM cuenta");
            statement.execute("DELETE FROM coordinador");
            statement.execute("ALTER TABLE coordinador AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM academico");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE cuenta AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");

        }

        testAcademics = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Academic academic = new Academic(
                    0,
                    "Académico " + i,
                    "55500000" + i,
                    'A',
                    "COD" + i,
                    i % 2 == 0 ? AcademicType.Evaluador : AcademicType.EE
            );
            academicDAO.addAcademic(academic);
            testAcademics.add(academic);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM academico WHERE numero_personal NOT LIKE 'COD%'");
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    void testAddAcademic_Success() throws SQLException {
        Academic newAcademic = new Academic(
                0,
                "Nuevo Académico",
                "5559876543",
                'A',
                "NEW001",
                AcademicType.Evaluador
        );

        int initialCount = academicDAO.countAcademics();
        boolean result = academicDAO.addAcademic(newAcademic);

        assertTrue(result);
        assertTrue(newAcademic.getIdUser() > 0, "El ID debe ser mayor que 0");
        assertEquals(initialCount + 1, academicDAO.countAcademics());
    }

    @Test
    void testAddAcademic_DuplicateStaffNumber() throws SQLException {
        Academic duplicateAcademic = new Academic(
                0,
                "Duplicado",
                "5551111111",
                'A',
                testAcademics.get(0).getStaffNumber(),
                AcademicType.Evaluador
        );

        assertThrows(SQLException.class, () -> academicDAO.addAcademic(duplicateAcademic));
    }

    @Test
    void testGetAllAcademics() throws SQLException {
        List<Academic> academics = academicDAO.getAllAcademics();
        System.out.println(academics.size());
        System.out.println(testAcademics.size());
        assertEquals(testAcademics.size(), academics.size());
    }

    @Test
    void testGetAcademicByStaffNumber() throws SQLException {
        Academic testAcademic = testAcademics.get(0);
        Academic found = academicDAO.getAcademicByStaffNumber(testAcademic.getStaffNumber());

        assertNotNull(found);
        assertEquals(testAcademic.getFullName(), found.getFullName());
    }

    @Test
    void testUpdateAcademic() throws SQLException {
        Academic toUpdate = testAcademics.get(0);
        toUpdate.setFullName("Nombre Actualizado");
        toUpdate.setCellphone("5559999999");
        toUpdate.setAcademicType(AcademicType.EE);

        boolean result = academicDAO.updateAcademic(toUpdate);
        assertTrue(result);

        Academic updated = academicDAO.getAcademicByStaffNumber(toUpdate.getStaffNumber());
        assertEquals("Nombre Actualizado", updated.getFullName());
        assertEquals("5559999999", updated.getCellPhone());
        assertEquals(AcademicType.EE, updated.getAcademicType());
    }

    @Test
    void testDeleteAcademic() throws SQLException {
        Academic toDelete = testAcademics.get(1);
        int initialCount = academicDAO.countAcademics();

        boolean result = academicDAO.deleteAcademic(toDelete);
        assertTrue(result);
        assertEquals(initialCount - 1, academicDAO.countAcademics());
        assertNull(academicDAO.getAcademicByStaffNumber(toDelete.getStaffNumber()));
        testAcademics.remove(1);
    }

    @Test
    void testGetAcademicsByType() throws SQLException {
        List<Academic> evaluadores = academicDAO.getAllAcademicsByType(AcademicType.Evaluador);
        long expectedCount = testAcademics.stream()
                .filter(a -> a.getAcademicType() == AcademicType.Evaluador)
                .count();

        assertEquals(expectedCount, evaluadores.size());
    }

    @Test
    void testChangeAcademicType() throws SQLException {
        Academic academic = testAcademics.get(2);
        AcademicType newType = academic.getAcademicType() == AcademicType.Evaluador
                ? AcademicType.EE
                : AcademicType.Evaluador;

        academic.setAcademicType(newType);
        boolean result = academicDAO.changeAcademicType(academic);
        assertTrue(result);

        Academic updated = academicDAO.getAcademicByStaffNumber(academic.getStaffNumber());
        assertEquals(newType, updated.getAcademicType());
    }

    @Test
    void testStaffNumberExists() throws SQLException {
        assertTrue(academicDAO.staffNumberExists(testAcademics.get(0).getStaffNumber()));
        assertFalse(academicDAO.staffNumberExists("NOEXISTE"));
    }

    @Test
    void testAcademicExists() throws SQLException {
        assertTrue(academicDAO.academicExists(testAcademics.get(0).getStaffNumber()));
        assertFalse(academicDAO.academicExists("NOEXISTE"));
    }

    @Test
    void testCountAcademics() throws SQLException {
        assertEquals(testAcademics.size(), academicDAO.countAcademics());
    }
}