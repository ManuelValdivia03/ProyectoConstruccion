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
        int errorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        String exceptionMessage = exception.getMessage();

        if (sqlState == null) {
            if (exceptionMessage != null && exceptionMessage.contains("Communications link failure")) {
                return "El servicio de base de datos no está disponible. Por favor, intente más tarde.";
            }
            return "Error de conexión con la base de datos: " + exceptionMessage;
        }

        if ((errorCode == 0 && "08S01".equals(sqlState)) ||
                (exceptionMessage != null && exceptionMessage.contains("Communications link failure"))) {
            userMessage = "El servicio de base de datos no está disponible. Por favor, intente más tarde.";
        }
        else if (errorCode == 1049 && "42000".equals(sqlState)) {
            userMessage = "La base de datos no existe o no está accesible.";
        }
        else if (errorCode == 1045 && "28000".equals(sqlState)) {
            userMessage = "Error de autenticación con la base de datos.";
        }
        else if (errorCode == 2003 || errorCode == 2002) {
            userMessage = "No se puede establecer conexión con el servidor de base de datos.";
        }
        else {
            switch (sqlState) {
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
                case "08S01":
                    userMessage = "No se puede conectar al servidor de base de datos.";
                    break;
                default:
                    userMessage = "Error de base de datos: " + exceptionMessage;
            }
        }
        return userMessage;
    }
}