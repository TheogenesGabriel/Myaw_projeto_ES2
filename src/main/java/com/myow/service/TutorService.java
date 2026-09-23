package com.myow.service;

import com.myow.dao.TutorDAO;
import com.myow.model.Tutor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TutorService {
    private final TutorDAO tutorDAO = new TutorDAO();

    public List<Tutor> listarTodos() {
        return tutorDAO.listarTodos();
    }

    public Optional<Tutor> buscarPorId(String id) {
        return tutorDAO.buscarPorId(id);
    }

    public List<Tutor> filtrar(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        String cleanTerm = termo.trim().toLowerCase();
        String numericTerm = cleanTerm.replaceAll("\\D", "");

        return tutorDAO.listarTodos().stream().filter(t -> {
            boolean matchNome = t.getNomeCompleto().toLowerCase().contains(cleanTerm);
            boolean matchCpf = t.getCpf().replaceAll("\\D", "").contains(numericTerm);
            boolean matchTel = t.getTelefone().replaceAll("\\D", "").contains(numericTerm);
            return matchNome || matchCpf || matchTel;
        }).collect(Collectors.toList());
    }

    public OperationResult<Tutor> cadastrar(String nome, String cpf, String telefone, String email, String endereco) {
        if (nome == null || nome.trim().isEmpty() ||
            cpf == null || cpf.trim().isEmpty() ||
            telefone == null || telefone.trim().isEmpty() ||
            endereco == null || endereco.trim().isEmpty()) {
            return OperationResult.error("Preencha todos os campos obrigatórios (Nome, CPF, Telefone e Endereço).");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        boolean cpfDuplicado = tutorDAO.listarTodos().stream()
                .anyMatch(t -> t.getCpf().replaceAll("\\D", "").equals(cpfLimpo));

        if (cpfDuplicado) {
            return OperationResult.error("Já existe um tutor cadastrado com este número de CPF.");
        }

        String id = IdGenerator.nextId("tut");
        String dataCadastro = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Tutor novoTutor = new Tutor(id, nome.trim(), cpf.trim(), telefone.trim(), email != null ? email.trim() : "", endereco.trim(), dataCadastro);

        boolean salvo = tutorDAO.salvar(novoTutor);
        if (!salvo) {
            return OperationResult.error("Erro ao persistir tutor no banco de dados.");
        }

        return OperationResult.ok("Tutor cadastrado com sucesso!", novoTutor);
    }

    public OperationResult<Tutor> atualizar(Tutor tutor) {
        if (tutor == null || tutor.getId() == null) {
            return OperationResult.error("Tutor inválido.");
        }
        boolean atualizado = tutorDAO.atualizar(tutor);
        if (!atualizado) {
            return OperationResult.error("Erro ao atualizar dados do tutor no banco de dados.");
        }
        return OperationResult.ok("Tutor atualizado com sucesso!", tutor);
    }
}
