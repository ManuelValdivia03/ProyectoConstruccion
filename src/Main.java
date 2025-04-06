import dataacces.AccountDAO;
import dataacces.UserDAO;
import dataacces.util.PasswordUtils;
import logic.Account;
import logic.User;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            AccountDAO accountDAO = new AccountDAO();

            User user = new User(2, "Lucas Rojo Verde", "2285243318");
            UserDAO userDAO = new UserDAO();
            userDAO.addUser(user);


            // Crear nueva cuenta
            Account newAccount = new Account(2, "Password", "lucasrojoverde@gmail.com");
            boolean created = accountDAO.addAccount(newAccount);
            System.out.println("Cuenta creada: " + created);

            // Verificar credenciales
            boolean valid = accountDAO.verifyCredentials("lucasrojoverde@gmail.com", "Password");
            System.out.println("Credenciales válidas: " + valid);

            // Actualizar contraseña
            boolean updated = accountDAO.updatePassword(2, "nuevanuevaContraseñaSegura456");
            System.out.println("Contraseña actualizada: " + updated);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error en la base de datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error general: " + e.getMessage());
        }
    }
}