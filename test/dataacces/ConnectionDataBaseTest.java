package dataacces;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionDataBaseTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        System.out.println("Configurando prueba...");
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexión cerrada");
        }
    }

    @Test
    void testGetConnection_Success() throws SQLException {
        connection = ConnectionDataBase.getConnection();

        assertNotNull(connection, "La conexión no debería ser nula");
        assertFalse(connection.isClosed(), "La conexión debería estar abierta");
        assertEquals("proyectoprincipiosconstruccion", connection.getCatalog(),
                "Debería conectarse a la base de datos correcta");
    }

    @Test
    void testGetConnection_MultipleConnections() throws SQLException {
        Connection conn1 = ConnectionDataBase.getConnection();
        Connection conn2 = ConnectionDataBase.getConnection();

        assertNotSame(conn1, conn2, "Deberían ser conexiones diferentes");
        assertTrue(conn1.isValid(2), "Conexión 1 debería ser válida");
        assertTrue(conn2.isValid(2), "Conexión 2 debería ser válida");

        conn1.close();
        conn2.close();
    }

    @Test
    void testConnectionProperties() throws SQLException {
        connection = ConnectionDataBase.getConnection();

        assertTrue(connection.getAutoCommit(), "AutoCommit debería estar activado por defecto");
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection.getTransactionIsolation(),
                "Nivel de aislamiento por defecto debería ser REPEATABLE_READ");
    }

    @Test
    void testInvalidCredentials() {
        assertThrows(SQLException.class, () -> {
            Connection invalidConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/proyectoprincipiosconstruccion",
                    "usuario_invalido",
                    "contraseña_invalida"
            );
        }, "Debería lanzar SQLException con credenciales inválidas");
    }
}