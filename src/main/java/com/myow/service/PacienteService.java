package com.myow.service;

import com.myow.dao.PacienteDAO;
import com.myow.model.Paciente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PacienteService {
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    public List<Paciente> listarTodos() {
        return pacienteDAO.listarTodos();
    }

    public Optional<Paciente> buscarPorId(String id) {
        return pacienteDAO.buscarPorId(id);
    }

    public List<Paciente> listarPorTutor(String tutorId) {
        return pacienteDAO.listarPorTutor(tutorId);
    }

    public List<Paciente> filtrar(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        String cleanTerm = termo.trim().toLowerCase();
        return pacienteDAO.listarTodos().stream().filter(p -> {
            boolean matchNome = p.getNome().toLowerCase().contains(cleanTerm);
            boolean matchEspecie = p.getEspecie().toLowerCase().contains(cleanTerm);
            boolean matchRaca = p.getRaca().toLowerCase().contains(cleanTerm);
            return matchNome || matchEspecie || matchRaca;
        }).collect(Collectors.toList());
    }

    public boolean verificarDuplicidade(String tutorId, String nomePaciente) {
        return pacienteDAO.listarPorTutor(tutorId).stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase(nomePaciente.trim()));
    }

    public OperationResult<Paciente> cadastrar(String nome, String especie, String raca, int idade, String sexo, String tutorId, String historico, boolean forcarDuplicado) {
        if (nome == null || nome.trim().isEmpty() ||
            especie == null || especie.trim().isEmpty() ||
            raca == null || raca.trim().isEmpty() ||
            sexo == null || sexo.trim().isEmpty() ||
            tutorId == null || tutorId.trim().isEmpty() ||
            idade < 0) {
            return OperationResult.error("Preencha todos os campos obrigatórios do paciente.");
        }

        if (!forcarDuplicado && verificarDuplicidade(tutorId, nome)) {
            return OperationResult.error("DUPLICADO: Já existe um animal cadastrado com este mesmo nome para o tutor selecionado.");
        }

        String id = IdGenerator.nextId("pac");
        String dataCadastro = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Paciente novoPaciente = new Paciente(id, nome.trim(), especie.trim(), raca.trim(), idade, sexo.trim(), tutorId, historico != null ? historico.trim() : "", dataCadastro);

        boolean salvo = pacienteDAO.salvar(novoPaciente);
        if (!salvo) {
            return OperationResult.error("Erro ao persistir paciente no banco de dados.");
        }

        return OperationResult.ok("Paciente cadastrado com sucesso!", novoPaciente);
    }

    public OperationResult<Paciente> atualizar(Paciente p) {
        if (p == null || p.getId() == null) {
            return OperationResult.error("Paciente inválido.");
        }
        boolean atualizado = pacienteDAO.atualizar(p);
        if (!atualizado) {
            return OperationResult.error("Erro ao atualizar dados do paciente no banco de dados.");
        }
        return OperationResult.ok("Paciente atualizado com sucesso!", p);
    }
}
