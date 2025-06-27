package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.GroupDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.logicclasses.Group;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupDAOTest {
    private static GroupDAO groupDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<Group> testGroups;
    private static List<Student> testStudents;

    @BeforeAll
    static void setUpAll() throws SQLException {
        groupDAO = new GroupDAO();
        studentDAO = new StudentDAO();
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

        testStudents = List.of(
                createTestStudent("S001", "Estudiante 1", "5551111111"),
                createTestStudent("S002", "Estudiante 2", "5552222222"),
                createTestStudent("S003", "Estudiante 3", "5553333333")
        );

        testGroups = List.of(
                createTestGroup(1001, "Grupo 1", List.of(testStudents.get(0), testStudents.get(1))),
                createTestGroup(1002, "Grupo 2", List.of(testStudents.get(2))),
                createTestGroup(1003, "Grupo 3", List.of())
        );
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        User user = new User();
        user.setFullName(fullName);
        user.setCellphone(phone);
        user.setStatus('A');
        UserDAO userDAO = new UserDAO();
        userDAO.addUser(user);

        Student student = new Student();
        student.setIdUser(user.getIdUser());
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        student.setStatus('A');
        studentDAO.addStudent(student,0);
        return student;
    }

    private static Group createTestGroup(int nrc, String groupName, List<Student> students) throws SQLException {
        Group group = new Group();
        group.setNrc(nrc);
        group.setGroupName(groupName);

        String sql = "INSERT INTO grupo (nrc, nombre) VALUES (?, ?)";
        try (PreparedStatement ps = testConnection.prepareStatement(sql)) {
            ps.setInt(1, nrc);
            ps.setString(2, groupName);
            ps.executeUpdate();
        }

        for (Student student : students) {
            String assignSql = "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)";
            try (PreparedStatement ps = testConnection.prepareStatement(assignSql)) {
                ps.setInt(1, nrc);
                ps.setInt(2, student.getIdUser());
                ps.executeUpdate();
            }
        }

        return group;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM grupo");
        }

        for (Group group : testGroups) {
            createTestGroup(group.getNrc(), group.getGroupName(), group.getStudents());
        }
    }

    @Test
    void testAddGroup_Success() throws SQLException {
        Group newGroup = new Group();
        newGroup.setNrc(1004);
        newGroup.setGroupName("Nuevo Grupo");

        int initialCount = groupDAO.countGroups();
        boolean result = groupDAO.addGroup(newGroup);

        assertTrue(result);
        assertEquals(initialCount + 1, groupDAO.countGroups());
        Group addedGroup = groupDAO.getGroupByNrc(1004);
        assertEquals("Nuevo Grupo", addedGroup.getGroupName());
    }

    @Test
    void testAddGroup_DuplicateNrc_ShouldThrowException() {
        Group duplicateGroup = new Group();
        duplicateGroup.setNrc(1001);
        duplicateGroup.setGroupName("Grupo Duplicado");
        assertThrows(SQLException.class, () -> groupDAO.addGroup(duplicateGroup));
    }

    @Test
    void testAddGroup_NullGroup() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> groupDAO.addGroup(null));
    }

    @Test
    void testAddGroup_NullName() throws SQLException {
        Group invalidGroup = new Group();
        invalidGroup.setNrc(1005);
        invalidGroup.setGroupName(null);
        assertThrows(IllegalArgumentException.class, () -> groupDAO.addGroup(invalidGroup));
    }

    @Test
    void testGetAllGroups_WithData() throws SQLException {
        List<Group> groups = groupDAO.getAllGroups();
        assertEquals(testGroups.size(), groups.size());
    }

    @Test
    void testGetAllGroups_EmptyTable() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM grupo");
        }
        List<Group> groups = groupDAO.getAllGroups();
        assertTrue(groups.isEmpty());
        setUp();
    }

    @Test
    void testGetGroupByNrc_Exists() throws SQLException {
        Group testGroup = testGroups.get(0);
        Group foundGroup = groupDAO.getGroupByNrc(testGroup.getNrc());
        assertEquals(testGroup.getGroupName(), foundGroup.getGroupName());
    }

    @Test
    void testGetGroupByNrc_NotExists() throws SQLException {
        Group foundGroup = groupDAO.getGroupByNrc(9999);
        assertEquals(-1, foundGroup.getNrc());
    }

    @Test
    void testGetGroupByNrc_InvalidNrc() throws SQLException {
        Group foundGroup = groupDAO.getGroupByNrc(-1);
        assertEquals(-1, foundGroup.getNrc());
    }

    @Test
    void testGetGroupByName_Exists() throws SQLException {
        Group testGroup = testGroups.get(0);
        Group foundGroup = groupDAO.getGroupByName(testGroup.getGroupName());
        assertEquals(testGroup.getNrc(), foundGroup.getNrc());
    }

    @Test
    void testGetGroupByName_NotExists() throws SQLException {
        Group foundGroup = groupDAO.getGroupByName("NOEXISTE");
        assertEquals(-1, foundGroup.getNrc());
    }

    @Test
    void testGetGroupByName_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> groupDAO.getGroupByName(null));
        assertThrows(IllegalArgumentException.class, () -> groupDAO.getGroupByName(""));
    }

    @Test
    void testUpdateGroup_Success() throws SQLException {
        Group groupToUpdate = groupDAO.getGroupByNrc(1001);
        groupToUpdate.setGroupName("Nombre Actualizado");
        boolean result = groupDAO.updateGroup(groupToUpdate);
        assertTrue(result);
        Group updatedGroup = groupDAO.getGroupByNrc(1001);
        assertEquals("Nombre Actualizado", updatedGroup.getGroupName());
    }

    @Test
    void testUpdateGroup_NotExists() throws SQLException {
        Group nonExistentGroup = new Group();
        nonExistentGroup.setNrc(9999);
        nonExistentGroup.setGroupName("No existe");
        boolean result = groupDAO.updateGroup(nonExistentGroup);
        assertFalse(result);
    }

    @Test
    void testUpdateGroup_NullGroup() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> groupDAO.updateGroup(null));
    }

    @Test
    void testUpdateGroup_NullName() throws SQLException {
        Group invalidGroup = new Group();
        invalidGroup.setNrc(1006);
        invalidGroup.setGroupName(null);
        assertThrows(IllegalArgumentException.class, () -> groupDAO.updateGroup(invalidGroup));
    }

    @Test
    void testDeleteGroup_Success() throws SQLException {
        Group groupToDelete = groupDAO.getGroupByNrc(1003);
        int initialCount = groupDAO.countGroups();
        boolean result = groupDAO.deleteGroup(groupToDelete);
        assertTrue(result);
        assertEquals(initialCount - 1, groupDAO.countGroups());
        assertEquals(-1, groupDAO.getGroupByNrc(1003).getNrc());
    }

    @Test
    void testDeleteGroup_WithStudents_ShouldThrowException() throws SQLException {
        UserDAO userDAO = new UserDAO();
        User user = new User(0, "Rojo Azul", "1234455670", "", 'A');
        userDAO.addUser(user);
        Student student = new Student();
        student.setIdUser(user.getIdUser());
        student.setEnrollment("1234");
        student.setFullName("Rojo Azul");
        student.setStatus('A');
        student.setCellphone("123445567");
        studentDAO.addStudent(student,0);
        Group groupToDelete = new Group(10020, "Hoy", new ArrayList<>());
        groupDAO.addGroup(groupToDelete);
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)")) {
            ps.setInt(1, groupToDelete.getNrc());
            ps.setInt(2, student.getIdUser());
            ps.executeUpdate();
        }
        assertThrows(SQLException.class, () -> groupDAO.deleteGroup(groupToDelete));
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM grupo_estudiante WHERE nrc = ? AND id_usuario = ?")) {
            ps.setInt(1, groupToDelete.getNrc());
            ps.setInt(2, student.getIdUser());
            ps.executeUpdate();
        }
        studentDAO.deleteStudent(student.getIdUser());
        userDAO.deleteUser(user.getIdUser());
        groupDAO.deleteGroup(groupToDelete);
    }

    @Test
    void testDeleteGroup_NotExists() throws SQLException {
        Group nonExistentGroup = new Group();
        nonExistentGroup.setNrc(9999);
        int initialCount = groupDAO.countGroups();
        boolean result = groupDAO.deleteGroup(nonExistentGroup);
        assertFalse(result);
        assertEquals(initialCount, groupDAO.countGroups());
    }

    @Test
    void testGroupExists_True() throws SQLException {
        assertTrue(groupDAO.groupExists(1001));
    }

    @Test
    void testGroupExists_False() throws SQLException {
        assertFalse(groupDAO.groupExists(9999));
    }

    @Test
    void testGroupExists_InvalidNrc() throws SQLException {
        assertFalse(groupDAO.groupExists(-1));
    }

    @Test
    void testCountGroups_WithData() throws SQLException {
        int count = groupDAO.countGroups();
        assertEquals(testGroups.size(), count);
        Group extraGroup = new Group();
        extraGroup.setNrc(1004);
        extraGroup.setGroupName("Extra Grupo");
        groupDAO.addGroup(extraGroup);
        assertEquals(count + 1, groupDAO.countGroups());
    }

    @Test
    void testCountGroups_EmptyTable() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo");
        }
        assertEquals(0, groupDAO.countGroups());
        setUp();
    }
}
