package logic.services;

import logic.exceptions.InvalidCellPhoneException;
import logic.exceptions.InvalidCredentialsException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedEnrollmentException;
import logic.exceptions.RepeatedNameLinkedOrganizationException;
import logic.exceptions.RepeatedProyectException;
import logic.exceptions.RepeatedStaffNumberException;
import logic.exceptions.UserNotFoundException;
import java.sql.SQLException;

public class ExceptionManager {

    public static String handleException(Exception exception) {
        String userMessage;

        if (exception instanceof SQLException) {
            userMessage = handleSQLException((SQLException) exception);
        } else if (exception instanceof UserNotFoundException) {
            userMessage = "Usuario no encontrado.";
        } else if (exception instanceof RepeatedStaffNumberException) {
            userMessage = "El número de personal ya está registrado.";
        } else if (exception instanceof RepeatedProyectException) {
            userMessage = "Ya existe un proyecto con ese nombre.";
        } else if (exception instanceof RepeatedNameLinkedOrganizationException) {
            userMessage = "Ya existe una organización vinculada con ese nombre.";
        } else if (exception instanceof RepeatedEmailException) {
            userMessage = "El correo electrónico ya está registrado.";
        } else if (exception instanceof RepeatedCellPhoneException) {
            userMessage = "El número de teléfono ya está registrado.";
        } else if (exception instanceof InvalidCredentialsException) {
            userMessage = "Credenciales inválidas.";
        } else if (exception instanceof InvalidCellPhoneException) {
            userMessage = "El número de teléfono es inválido.";
        } else if (exception instanceof RepeatedEnrollmentException) {
            userMessage = "La matrícula ya está registrada.";
        } else {
            userMessage = "Error inesperado: " + exception.getMessage();
        }
        return userMessage;
    }

    private static String handleSQLException(SQLException exception) {
        String userMessage;
        switch (exception.getSQLState()) {
            case "23000":
                userMessage = "Violación de restricción de unicidad.";
                break;
            case "28000":
                userMessage = "Error de autenticación en la base de datos.";
                break;
            case "42000":
                userMessage = "Error de sintaxis SQL o permisos insuficientes.";
                break;
            case "08001":
                userMessage = "No se puede conectar al servidor de base de datos.";
                break;
            default:
                userMessage = "Error de base de datos: " + exception.getMessage();
        }
        return userMessage;
    }
}
