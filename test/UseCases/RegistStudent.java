package UseCases;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.daos.AccountDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Account;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import userinterface.controllers.ControllerCreateStudentWindow;
import userinterface.utilities.PasswordToggleField;
import userinterface.utilities.Validators;
import userinterface.windows.CreateStudentWindow;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class RegistStudent {

    @Mock
    private CreateStudentWindow mockView;
    @Mock private Academic mockAcademic;
    @Mock private StudentDAO mockStudentDAO;
    @Mock private UserDAO mockUserDAO;
    @Mock private AccountDAO mockAccountDAO;
    @Mock private Validators mockValidators;

    @Mock private TextField mockNameField;
    @Mock private TextField mockPhoneField;
    @Mock private TextField mockEnrollmentField;
    @Mock private TextField mockEmailField;
    @Mock private PasswordToggleField mockPasswordField;
    @Mock private Label mockResultLabel;
    @Mock private javafx.scene.control.Button mockAddButton;

    private ControllerCreateStudentWindow controller;

    @BeforeEach
    void setUp() {
        // Configurar mocks para los campos de la vista
        when(mockView.getNameField()).thenReturn(mockNameField);
        when(mockView.getPhoneField()).thenReturn(mockPhoneField);
        when(mockView.getEnrollmentField()).thenReturn(mockEnrollmentField);
        when(mockView.getEmailField()).thenReturn(mockEmailField);
        when(mockView.getPasswordField()).thenReturn(mockPasswordField);
        when(mockView.getResultLabel()).thenReturn(mockResultLabel);
        when(mockView.getAddButton()).thenReturn(mockAddButton);
        when(mockView.getCancelButton()).thenReturn(mockAddButton); // Usamos el mismo para simplificar

        // Configurar academic mock
        when(mockAcademic.getIdUser()).thenReturn(1);

        controller = new ControllerCreateStudentWindow(mockView, mockAcademic) {
            protected StudentDAO getStudentDAO() {
                return mockStudentDAO;
            }

            protected UserDAO getUserDAO() {
                return mockUserDAO;
            }

            protected AccountDAO getAccountDAO() {
                return mockAccountDAO;
            }

            protected Validators getValidators() {
                return mockValidators;
            }
        };
    }

    @Test
    void testSuccessfulStudentRegistration() throws SQLException {
        // Configurar datos de prueba
        when(mockNameField.getText()).thenReturn("Juan Pérez");
        when(mockPhoneField.getText()).thenReturn("2281234567");
        when(mockEnrollmentField.getText()).thenReturn("S21015678");
        when(mockEmailField.getText()).thenReturn("juan@estudiantes.uv.mx");
        when(mockView.getPassword()).thenReturn("password123");

        // Configurar validaciones
        when(mockValidators.validateCellPhone(anyString())).thenReturn(true);
        when(mockValidators.validateEnrollment(anyString())).thenReturn(true);
        when(mockValidators.validateEmail(anyString())).thenReturn(true);
        when(mockValidators.validatePassword(anyString())).thenReturn(true);

        // Configurar verificaciones de duplicados
        when(mockUserDAO.cellPhoneExists(anyString())).thenReturn(false);
        when(mockStudentDAO.enrollmentExists(anyString())).thenReturn(false);
        when(mockAccountDAO.accountExists(anyString())).thenReturn(false);

        // Configurar operaciones de inserción
        when(mockUserDAO.addUser(any(User.class))).thenReturn(true);
        when(mockStudentDAO.addStudent(any(Student.class), anyInt())).thenReturn(true);
        when(mockAccountDAO.addAccount(any(Account.class))).thenReturn(true);

        // Ejecutar
        controller.handle(new ActionEvent(mockAddButton, null));

        // Verificar
        verify(mockUserDAO).addUser(any(User.class));
        verify(mockStudentDAO).addStudent(any(Student.class), eq(1));
        verify(mockAccountDAO).addAccount(any(Account.class));
        verify(mockResultLabel).setText(contains("éxito"));
    }

    @Test
    void testDuplicateEnrollmentRegistration() throws SQLException {
        // Configurar datos
        when(mockNameField.getText()).thenReturn("María López");
        when(mockPhoneField.getText()).thenReturn("2289876543");
        when(mockEnrollmentField.getText()).thenReturn("S21015678");
        when(mockEmailField.getText()).thenReturn("maria@estudiantes.uv.mx");
        when(mockView.getPassword()).thenReturn("password123");

        // Configurar validaciones
        when(mockValidators.validateCellPhone(anyString())).thenReturn(true);
        when(mockValidators.validateEnrollment(anyString())).thenReturn(true);
        when(mockValidators.validateEmail(anyString())).thenReturn(true);
        when(mockValidators.validatePassword(anyString())).thenReturn(true);

        // Configurar para que falle por matrícula duplicada
        when(mockUserDAO.cellPhoneExists(anyString())).thenReturn(false);
        when(mockStudentDAO.enrollmentExists(anyString())).thenReturn(true);

        // Ejecutar
        controller.handle(new ActionEvent(mockAddButton, null));

        // Verificar
        verify(mockStudentDAO, never()).addStudent(any(Student.class), anyInt());
        verify(mockResultLabel).setText(contains("matrícula"));
    }

    @Test
    void testInvalidFieldsRegistration() throws SQLException {
        // Configurar datos inválidos
        when(mockNameField.getText()).thenReturn("");
        when(mockPhoneField.getText()).thenReturn("123");
        when(mockEnrollmentField.getText()).thenReturn("12345");
        when(mockEmailField.getText()).thenReturn("correo_invalido");
        when(mockView.getPassword()).thenReturn("123");

        // Configurar validaciones para fallar
        when(mockValidators.validateCellPhone("123")).thenReturn(false);
        when(mockValidators.validateEnrollment("12345")).thenReturn(false);
        when(mockValidators.validateEmail("correo_invalido")).thenReturn(false);
        when(mockValidators.validatePassword("123")).thenReturn(false);

        // Ejecutar
        controller.handle(new ActionEvent(mockAddButton, null));

        verify(mockStudentDAO, never()).addStudent(any(Student.class), anyInt());
        verify(mockUserDAO, never()).addUser(any(User.class));
        verify(mockAccountDAO, never()).addAccount(any(Account.class));
    }

    @Test
    void testDuplicateEmailRegistration() throws SQLException {
        // Configurar datos
        when(mockNameField.getText()).thenReturn("Pedro Ramírez");
        when(mockPhoneField.getText()).thenReturn("2282345678");
        when(mockEnrollmentField.getText()).thenReturn("S21015679");
        when(mockEmailField.getText()).thenReturn("pedro@estudiantes.uv.mx");
        when(mockView.getPassword()).thenReturn("password123");

        // Configurar validaciones
        when(mockValidators.validateCellPhone(anyString())).thenReturn(true);
        when(mockValidators.validateEnrollment(anyString())).thenReturn(true);
        when(mockValidators.validateEmail(anyString())).thenReturn(true);
        when(mockValidators.validatePassword(anyString())).thenReturn(true);

        // Configurar para que falle por email duplicado
        when(mockUserDAO.cellPhoneExists(anyString())).thenReturn(false);
        when(mockStudentDAO.enrollmentExists(anyString())).thenReturn(false);
        when(mockAccountDAO.accountExists(anyString())).thenReturn(true);

        // Ejecutar
        controller.handle(new ActionEvent(mockAddButton, null));

        // Verificar
        verify(mockStudentDAO, never()).addStudent(any(Student.class), anyInt());
        verify(mockUserDAO, never()).addUser(any(User.class));
        verify(mockAccountDAO, never()).addAccount(any(Account.class));
        verify(mockResultLabel).setText(contains("email ya está registrado"));
    }

    @Test
    void testDuplicatePhoneRegistration() throws SQLException {
        // Configurar datos
        when(mockNameField.getText()).thenReturn("Ana García");
        when(mockPhoneField.getText()).thenReturn("2283456789");
        when(mockEnrollmentField.getText()).thenReturn("S21015680");
        when(mockEmailField.getText()).thenReturn("ana@estudiantes.uv.mx");
        when(mockView.getPassword()).thenReturn("password123");

        // Configurar validaciones
        when(mockValidators.validateCellPhone(anyString())).thenReturn(true);
        when(mockValidators.validateEnrollment(anyString())).thenReturn(true);
        when(mockValidators.validateEmail(anyString())).thenReturn(true);
        when(mockValidators.validatePassword(anyString())).thenReturn(true);

        // Configurar para que falle por teléfono duplicado
        when(mockUserDAO.cellPhoneExists(anyString())).thenReturn(true);

        // Ejecutar
        controller.handle(new ActionEvent(mockAddButton, null));

        // Verificar
        verify(mockStudentDAO, never()).addStudent(any(Student.class), anyInt());
        verify(mockUserDAO, never()).addUser(any(User.class));
        verify(mockAccountDAO, never()).addAccount(any(Account.class));
        verify(mockResultLabel).setText(contains("teléfono ya está registrado"));
    }
}
