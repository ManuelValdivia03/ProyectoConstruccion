package dataacces;

import logic.Group;
import logic.Student;
import logic.User;
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

        // Limpiar y preparar la base de datos
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM academico");
            statement.execute("DELETE FROM estudiante");
            statement.execute("DELETE FROM coordinador");
            statement.execute("ALTER TABLE coordinador AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM grupo");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");

            // Crear tablas necesarias
            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20), " +
                    "estado CHAR(1) DEFAULT 'A')");

            statement.execute("CREATE TABLE IF NOT EXISTS estudiante (" +
                    "id_usuario INT PRIMARY KEY, " +
                    "matricula VARCHAR(50) NOT NULL UNIQUE, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS grupo (" +
                    "nrc INT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS grupo_estudiante (" +
                    "nrc INT, " +
                    "id_usuario INT, " +
                    "PRIMARY KEY (nrc, id_usuario), " +
                    "FOREIGN KEY (nrc) REFERENCES grupo(nrc), " +
                    "FOREIGN KEY (id_usuario) REFERENCES estudiante(id_usuario))");
        }

        // Crear estudiantes de prueba
        testStudents = List.of(
                createTestStudent("S001", "Estudiante 1", "5551111111"),
                createTestStudent("S002", "Estudiante 2", "5552222222"),
                createTestStudent("S003", "Estudiante 3", "5553333333")
        );

        // Crear grupos de prueba
        testGroups = List.of(
                createTestGroup(1001, "Grupo 1", List.of(testStudents.get(0), testStudents.get(1))),
                createTestGroup(1002, "Grupo 2", List.of(testStudents.get(2))),
                createTestGroup(1003, "Grupo 3", List.of())
        );
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        Student student = new Student();
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        studentDAO.addStudent(student);
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
        // Limpiar tablas antes de cada prueba
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM grupo");
        }

        // Reinsertar datos de prueba
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
        assertNotNull(addedGroup);
        assertEquals("Nuevo Grupo", addedGroup.getGroupName());
    }

    @Test
    void testAddGroup_DuplicateNrc_ShouldFail() {
        Group duplicateGroup = new Group();
        duplicateGroup.setNrc(1001); // NRC ya existente
        duplicateGroup.setGroupName("Grupo Duplicado");

        assertThrows(SQLException.class, () -> groupDAO.addGroup(duplicateGroup));
    }

    @Test
    void testAddGroup_NullName_ShouldThrowException() {
        Group invalidGroup = new Group();
        invalidGroup.setNrc(1005);
        invalidGroup.setGroupName(null);

        assertThrows(SQLException.class, () -> groupDAO.addGroup(invalidGroup));
    }

    @Test
    void testGetAllGroups_WithData() throws SQLException {
        List<Group> groups = groupDAO.getAllGroups();
        assertEquals(testGroups.size(), groups.size());

        for (Group testGroup : testGroups) {
            boolean found = groups.stream()
                    .anyMatch(g -> g.getNrc() == testGroup.getNrc());
            assertTrue(found, "No se encontró el grupo con NRC: " + testGroup.getNrc());
        }
    }

    @Test
    void testGetGroupByNrc_Exists() throws SQLException {
        Group testGroup = testGroups.get(0);
        Group foundGroup = groupDAO.getGroupByNrc(testGroup.getNrc());

        assertNotNull(foundGroup);
        assertEquals(testGroup.getGroupName(), foundGroup.getGroupName());
        assertEquals(testGroup.getStudents().size(), foundGroup.getStudents().size());
    }

    @Test
    void testGetGroupByNrc_NotExists() throws SQLException {
        Group foundGroup = groupDAO.getGroupByNrc(9999);
        assertNull(foundGroup);
    }

    @Test
    void testGetGroupByName_Exists() throws SQLException {
        Group testGroup = testGroups.get(0);
        Group foundGroup = groupDAO.getGroupByName(testGroup.getGroupName());

        assertNotNull(foundGroup);
        assertEquals(testGroup.getNrc(), foundGroup.getNrc());
        assertEquals(testGroup.getStudents().size(), foundGroup.getStudents().size());
    }

    @Test
    void testGetGroupByName_NotExists() throws SQLException {
        Group foundGroup = groupDAO.getGroupByName("NOEXISTE");
        assertNull(foundGroup);
    }

    @Test
    void testUpdateGroup_Success() throws SQLException {
        Group groupToUpdate = groupDAO.getGroupByNrc(1001);
        groupToUpdate.setGroupName("Nombre Actualizado");

        boolean result = groupDAO.updateGroup(groupToUpdate);
        assertTrue(result);

        Group updatedGroup = groupDAO.getGroupByNrc(1001);
        assertNotNull(updatedGroup);
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
    void testDeleteGroup_Success() throws SQLException {
        // Eliminar un grupo sin estudiantes
        Group groupToDelete = groupDAO.getGroupByNrc(1003); // Grupo 3 no tiene estudiantes
        int initialCount = groupDAO.countGroups();

        boolean result = groupDAO.deleteGroup(groupToDelete);
        System.out.println(groupToDelete.getStudents().size());
        assertTrue(result);
        assertEquals(initialCount - 1, groupDAO.countGroups());
        assertNull(groupDAO.getGroupByNrc(1003));
    }

    @Test
    void testDeleteGroup_WithStudents_ShouldFail() throws SQLException {
        // 1. Crear y guardar el usuario
        UserDAO userDAO = new UserDAO();
        User user = new User(0, "Rojo Azul", "123445567", 'A');
        boolean userAdded = userDAO.addUser(user);
        assertTrue(userAdded, "El usuario debería haberse creado correctamente");

        // 2. Crear el estudiante con el id_usuario correcto
        Student student = new Student();
        student.setIdUser(user.getIdUser());
        student.setEnrollment("1234");
        student.setFullName("Rojo Azul");
        student.setStatus('A');
        student.setCellphone("123445567");

        // 3. Agregar el estudiante a la base de datos
        boolean studentAdded = studentDAO.addStudent(student);
        assertTrue(studentAdded, "El estudiante debería haberse creado correctamente");

        // 4. Crear y guardar el grupo
        Group groupToDelete = new Group(10020, "Hoy", new ArrayList<>());
        boolean groupAdded = groupDAO.addGroup(groupToDelete);
        assertTrue(groupAdded, "El grupo debería haberse creado correctamente");

        // 5. Asignar el estudiante al grupo (versión corregida)
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)")) {
            ps.setInt(1, groupToDelete.getNrc());
            ps.setInt(2, student.getIdUser());
            int rowsAffected = ps.executeUpdate();
            assertEquals(1, rowsAffected, "Debería haberse asignado 1 estudiante al grupo");
        }

        // 6. Verificar que el grupo tiene estudiantes
        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(groupToDelete.getNrc());
        assertFalse(studentsInGroup.isEmpty(),
                "El grupo debería tener estudiantes. Estudiantes encontrados: " + studentsInGroup.size());

        // 7. Intentar eliminar el grupo (debería fallar)
        assertThrows(SQLException.class, () -> groupDAO.deleteGroup(groupToDelete),
                "Debería lanzar excepción al intentar eliminar grupo con estudiantes");

        // 8. Limpieza (opcional)
        // Eliminar la relación estudiante-grupo
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM grupo_estudiante WHERE nrc = ? AND id_usuario = ?")) {
            ps.setInt(1, groupToDelete.getNrc());
            ps.setInt(2, student.getIdUser());
            ps.executeUpdate();
        }
        // Eliminar el estudiante
        studentDAO.deleteStudent(student.getIdUser());
        // Eliminar el usuario
        userDAO.deleteUser(user.getIdUser());
        // Eliminar el grupo
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
    void testCountGroups_WithData() throws SQLException {
        int count = groupDAO.countGroups();
        assertEquals(testGroups.size(), count);

        Group extraGroup = new Group();
        extraGroup.setNrc(1004);
        extraGroup.setGroupName("Extra Grupo");
        groupDAO.addGroup(extraGroup);

        assertEquals(count + 1, groupDAO.countGroups());
    }
}