package br.com.clinica.controller;

import br.com.clinica.MainApp;
import javafx.fxml.FXML;

public class ServicosController {

    @FXML
    private void handleIrParaUC02() {
        MainApp.navegarPara("/br/com/clinica/view/CadastrarTutorView.fxml", "UC02 - Cadastrar Tutor");
    }

    @FXML
    private void handleIrParaUC03() {
        MainApp.navegarPara("/br/com/clinica/view/CadastrarPetView.fxml", "UC03 - Cadastrar Paciente (Animal)");
    }

    @FXML
    private void handleIrParaUC04() {
        MainApp.navegarPara("/br/com/clinica/view/AgendarConsultaView.fxml", "UC04 - Agendar Consulta");
    }

    @FXML
    private void handleIrParaUC05() {
        MainApp.navegarPara("/br/com/clinica/view/AlterarConsultaView.fxml", "UC05 - Alterar Consulta");
    }

    @FXML
    private void handleIrParaUC06() {
        MainApp.navegarPara("/br/com/clinica/view/CancelarConsultaView.fxml", "UC06 - Cancelar Consulta");
    }

    @FXML
    private void handleIrParaUC07() {
        MainApp.navegarPara("/br/com/clinica/view/RegistrarAtendimentoView.fxml", "UC07 – Registrar Atendimento Médico");
    }

    @FXML
    private void handleLogout() {
        MainApp.navegarPara("/br/com/clinica/view/LoginView.fxml", "Login - Clínica Veterinária");
    }
}
